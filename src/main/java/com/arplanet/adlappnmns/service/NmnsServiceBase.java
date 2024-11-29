package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.arplanet.adlappnmns.service.support.ZipWriter;
import com.arplanet.adlappnmns.utils.DataConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.zip.ZipOutputStream;

import static com.arplanet.adlappnmns.enums.ErrorType.SERVICE;
import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;
import static com.arplanet.adlappnmns.utils.ServiceUtil.APPLICATION_JSON;

@Slf4j
public abstract class NmnsServiceBase<T> implements NmnsService<T> {


    private static final int PACKAGE_SIZE = 5000;
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.ALWAYS);

    public static final DefaultPrettyPrinter prettyPrinter;

    @Value("${aws.s3.write.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.static.data.write.folder}")
    private String destinationFolder;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected S3Repository s3Repository;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Logger logger;
    @Autowired
    private DataConverter dataConverter;
    @Autowired
    private LogContext logContext;
    @Autowired
    private ZipWriter zipWriter;

    protected ProcessType processType;

    static {
        prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }


    @PostConstruct
    private void init() {
        String beanName = applicationContext.getBeanNamesForType(this.getClass())[0];
        this.processType = ProcessType.getByNmnsService(beanName);
    }

    @Override
    public void doProcess(String date, ProcessContext processContext, ZipOutputStream gcsZipStream, ZipOutputStream s3ZipStream) {
        List<T> dataList = findByDate(date, processContext);
        List<T> processedList  = processData(date, dataList);

        writeProcessedDataToZip(processedList, date, gcsZipStream, s3ZipStream);
    }

    @Override
    public List<T> processData(String date, List<T> dataList) {
        if (processType.isEnableS3Backup()) {
            return dataList.parallelStream()
                    .peek(data -> logContext.setCurrentDate(date))
                    .filter(data -> {
                        Map<String, Object> dataMap = dataConverter.convertToMap(data);
                        try {
                            validateData(data);

                            saveToS3(data);
                            return true;
                        } catch (NmnsServiceException e) {
                            logger.error(processType.getTypeName() + " " + e.getMessage(), e, dataMap, SERVICE);
                            return false;
                        } catch (S3Exception e) {
                            logger.error(processType.getTypeName() + " 上傳S3失敗", e, dataMap, SYSTEM);
                            return true;
                        } catch (JsonProcessingException e) {
                            logger.error(processType.getTypeName() + " 資料轉換JSON字串失敗", e, dataMap, SYSTEM);
                            return false;
                        } catch (UnsupportedOperationException e) {
                            logger.error(processType.getTypeName() + " 上傳到S3的Service沒有Override getId()", e, dataMap, SYSTEM);
                            return true;
                        }
                    })
                    .toList();

        } else {
            return dataList.parallelStream()
                    .peek(data -> logContext.setCurrentDate(date))
                    .filter(data -> {
                        Map<String, Object> dataMap = dataConverter.convertToMap(data);
                        try {
                            validateData(data);
                            return true;
                        } catch (NmnsServiceException e) {
                            logger.error(processType.getTypeName() + " " + e.getMessage(), e, dataMap, SERVICE);
                            return false;
                        }
                    })
                    .toList();
        }
    }

    private void writeProcessedDataToZip(List<T> processedList, String date, ZipOutputStream gcsZipStream, ZipOutputStream s3ZipStream) {
        try {
            if (processedList.isEmpty()) {
                String fileName = createFileName(date, 0, 0, processType.getTypeName());
                zipWriter.writeEntry(fileName, "[]", gcsZipStream);
                zipWriter.writeEntry(fileName, "[]", s3ZipStream);
                return;
            }

            int totalSize = processedList.size();
            IntStream.range(0, (totalSize + PACKAGE_SIZE - 1) / PACKAGE_SIZE)
                    .parallel()
                    .peek(i -> logContext.setCurrentDate(date))
                    .forEach(i -> {
                        int start = i * PACKAGE_SIZE;
                        int end = Math.min(start + PACKAGE_SIZE, totalSize);
                        List<?> subList = processedList.subList(start, end);

                        String fileName = createFileName(date, start + 1, end, processType.getTypeName());

                        zipWriter.writeEntry(fileName, writeJsonToStream(subList), gcsZipStream);
                        zipWriter.writeEntry(fileName, writeJsonToStream(subList), s3ZipStream);

                    });
        } catch (Exception e) {
            logger.error(processType.getTypeName() + "寫入ZIP檔案失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }

    private String createFileName(String date, int start, int end, String type) {
        return date +
                "_" +
                start + "-" + end +
                "_" +
                type +
                ".json";
    }

    private String writeJsonToStream(List<?> subList) {
        try {
            return mapper.writer(prettyPrinter).writeValueAsString(subList);
        } catch (JsonProcessingException e) {
            logger.error(processType.getTypeName() + "Json Processing 失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }

    protected abstract void validateData(T data);

    private void saveToS3(T data) throws JsonProcessingException {
        String jsonContent = objectMapper.writeValueAsString(data);
        s3Repository.putFile(bucketName, generatePath(data), APPLICATION_JSON, jsonContent.getBytes(StandardCharsets.UTF_8));
    }

    protected String generatePath(T data) {
        // 產生S3路徑
        return destinationFolder + processType.getPathType() + "/" + getId(data) + ".json";
    }

    protected String getId(T data) {
        throw new UnsupportedOperationException("getId is not implemented for this service");
    }

}
