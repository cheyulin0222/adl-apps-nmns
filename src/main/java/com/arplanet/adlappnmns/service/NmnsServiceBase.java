package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.record.ZipEntryData;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.arplanet.adlappnmns.utils.DataConverter;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.arplanet.adlappnmns.utils.ServiceUtil.APPLICATION_JSON;

public abstract class NmnsServiceBase<T> implements NmnsService<T> {

    private final int PACKAGE_SIZE = 5000;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

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

    protected ProcessType processType;

    @Value("${aws.s3.write.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.write.folder}")
    private String destinationFolder;

    @PostConstruct
    private void init() {
        String beanName = applicationContext.getBeanNamesForType(this.getClass())[0];
        this.processType = ProcessType.getByNmnsService(beanName);
    }

    @Override
    public void processData(List<T> dataList) {

        String date = logContext.getCurrentDate();

        if (processType.isEnableS3Backup()) {
            dataList.parallelStream().forEach(data -> {
                logContext.setCurrentDate(date);
                Map<String, Object> dataMap = dataConverter.convertToMap(data);
                try {
                    validateData(data);

                    saveToS3(data);
                } catch (NullPointerException e) {
                    logger.error(processType.getTypeName() + "資料驗證失敗", e, dataMap);
                    throw new RuntimeException(e);
                } catch (S3Exception e) {
                    logger.error("上傳S3失敗", e, dataMap);
                    throw new RuntimeException(e);
                } catch (JsonProcessingException e) {
                    logger.error("資料轉換JSON字串失敗", e, dataMap);
                    throw new RuntimeException(e);
                } catch (UnsupportedOperationException e) {
                    logger.error("上傳到S3的Service沒有Override getId()", e, dataMap);
                    throw new RuntimeException(e);
                }
            });
        } else {
            dataList.parallelStream().forEach(data -> {
                logContext.setCurrentDate(date);
                Map<String, Object> dataMap = dataConverter.convertToMap(data);
                try {
                    validateData(data);
                } catch (NullPointerException e) {
                    logger.error("資料驗證失敗", e, dataMap);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    protected List<ZipEntryData> createZipEntries(List<T> dataList, String date, String typeName) {
        DefaultPrettyPrinter prettyPrinter = ServiceUtil.createPrettyPrinter();

        if (dataList.isEmpty()) {
            return List.of(createZipEntryData(createFileName(date, 0, 0, typeName), "[]"));
        }

        // 每5000筆包成一個json檔
        return IntStream.range(0, (dataList.size() + PACKAGE_SIZE - 1) / PACKAGE_SIZE)
                .mapToObj(i -> {
                    int start = i * PACKAGE_SIZE;
                    int end = Math.min(start + PACKAGE_SIZE, dataList.size());
                    List<?> subList = dataList.subList(start, end);
                    try {
                        String jsonContent = mapper.writer(prettyPrinter).writeValueAsString(subList);
                        return createZipEntryData(createFileName(date, start + 1, end, typeName), jsonContent);
                    } catch (Exception e) {
                        logger.error("建立ZIP檔案的" + typeName + "Json檔失敗", e);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private String createFileName(String date, int start, int end, String type) {
        return date +
                "_" +
                start + "-" + end +
                "_" +
                type +
                ".json";
    }

    private ZipEntryData createZipEntryData(String fileName, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return new ZipEntryData(fileName, contentBytes);
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
