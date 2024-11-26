package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.arplanet.adlappnmns.utils.DataConverter;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
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

    @Value("${aws.s3.write.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.write.folder}")
    private String destinationFolder;


    private final Object zipLock = new Object();

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



    @PostConstruct
    private void init() {
        String beanName = applicationContext.getBeanNamesForType(this.getClass())[0];
        this.processType = ProcessType.getByNmnsService(beanName);
    }

    @Override
    public void doProcess(String date, ProcessContext processContext, ZipOutputStream zipStream) {
        List<T> dataList = findByDate(date, processContext);
        List<T> processedList  = processData(date, dataList);

        DefaultPrettyPrinter prettyPrinter = ServiceUtil.createPrettyPrinter();
        writeProcessedDataToZip(processedList, date, zipStream, prettyPrinter);
    }

//    @Override
//    public List<ZipEntryData> doProcess(String date, ProcessContext processContext) {
//        List<T> dataList = findByDate(date, processContext);
//
//        List<T> newList = processData(date, dataList);
//
//        return createZipEntries(newList, date, processType.getTypeName());
//    }

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
                            logger.error(processType.getTypeName() + " 資料驗證失敗", e, dataMap, SERVICE);
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
                            logger.error(processType.getTypeName() + " 資料驗證失敗", e, dataMap, SERVICE);
                            return false;
                        }
                    })
                    .toList();
        }
    }

    private void writeProcessedDataToZip(List<T> processedList, String date,
                                         ZipOutputStream zipStream, DefaultPrettyPrinter prettyPrinter) {
            if (processedList.isEmpty()) {
                synchronized (zipLock) {
                    writeEmptyArrayToZip(date, zipStream);
                }
                return;
            }

            int totalSize = processedList.size();
            IntStream.range(0, (totalSize + PACKAGE_SIZE - 1) / PACKAGE_SIZE)
                    .parallel()
                    .peek(i -> logContext.setCurrentDate(date))
                    .forEach(i -> writeDataChunkToZip(
                            processedList,
                            date,
                            zipStream,
                            prettyPrinter,
                            i,
                            totalSize
                    ));


    }

    private void writeDataChunkToZip(List<T> processedList, String date,
                                     ZipOutputStream zipStream, DefaultPrettyPrinter prettyPrinter,
                                     int chunkIndex, int totalSize) {
        try {
            int start = chunkIndex * PACKAGE_SIZE;
            int end = Math.min(start + PACKAGE_SIZE, totalSize);
            List<?> subList = processedList.subList(start, end);

            String fileName = createFileName(date, start + 1, end, processType.getTypeName());

            String jsonContent = mapper.writer(prettyPrinter).writeValueAsString(subList);

            synchronized (zipLock) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipStream.putNextEntry(zipEntry);
                zipStream.write(jsonContent.getBytes(StandardCharsets.UTF_8));
                zipStream.closeEntry();
            }

        } catch (Exception e) {
            logger.error("寫入ZIP檔案的" + processType.getTypeName() + "Json檔失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }

    private void writeEmptyArrayToZip(String date, ZipOutputStream zipStream) {
        try {
            String fileName = createFileName(date, 0, 0, processType.getTypeName());
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipStream.putNextEntry(zipEntry);
            zipStream.write("[]".getBytes(StandardCharsets.UTF_8));
            zipStream.closeEntry();
        } catch (Exception e) {
            logger.error("寫入ZIP檔案的" + processType.getTypeName() + "Json檔失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
//
//    protected List<ZipEntryData> createZipEntries(List<T> dataList, String date, String typeName) {
//        DefaultPrettyPrinter prettyPrinter = ServiceUtil.createPrettyPrinter();
//
//        if (dataList.isEmpty()) {
//            return List.of(createZipEntryData(createFileName(date, 0, 0, typeName), "[]"));
//        }
//
//        // 每5000筆包成一個json檔
//        return IntStream.range(0, (dataList.size() + PACKAGE_SIZE - 1) / PACKAGE_SIZE)
//                .mapToObj(i -> {
//                    int start = i * PACKAGE_SIZE;
//                    int end = Math.min(start + PACKAGE_SIZE, dataList.size());
//                    List<?> subList = dataList.subList(start, end);
//                    try {
//                        String jsonContent = mapper.writer(prettyPrinter).writeValueAsString(subList);
//                        return createZipEntryData(createFileName(date, start + 1, end, typeName), jsonContent);
//                    } catch (Exception e) {
//                        logger.error("建立ZIP檔案的" + typeName + "Json檔失敗", e, SYSTEM);
//                        throw new RuntimeException(e);
//                    }
//                })
//                .collect(Collectors.toList());
//    }

    private String createFileName(String date, int start, int end, String type) {
        return date +
                "_" +
                start + "-" + end +
                "_" +
                type +
                ".json";
    }

//    private ZipEntryData createZipEntryData(String fileName, String content) {
//        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
//        return new ZipEntryData(fileName, contentBytes);
//    }

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
