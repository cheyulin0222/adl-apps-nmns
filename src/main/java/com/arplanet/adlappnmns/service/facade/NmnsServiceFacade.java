package com.arplanet.adlappnmns.service.facade;

import com.arplanet.adlappnmns.config.NmnsBeanFactory;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.record.ZipEntryData;
import com.arplanet.adlappnmns.repository.GCSRepository;
import com.arplanet.adlappnmns.service.NmnsService;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.arplanet.adlappnmns.utils.ServiceUtil.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class NmnsServiceFacade {

    private final int PACKAGE_SIZE = 5000;
    private final NmnsBeanFactory nmnsBeanFactory;
    private final GCSRepository gcsRepository;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Logger logger;
    private final LogContext logContext;


    @Value("${gcs.bucket.name}")
    private String destinationBucketName;

    @Value("${gcs.destination.folder}")
    private String destinationFolder;



    public void process(String date) {
        try {
            logger.info("教育大數據開始執行");
            logger.info("執行日期: " + date);

            // 從資料庫取得修改資料
            ConcurrentHashMap<String, List<?>> data = getData(date);

            // 資料處理並上傳S3
            List<ZipEntryData> zipEntries = processData(data, date);

            // 產生zip
            byte[] zipData = createZipFile(zipEntries);

            // 產生GCP路徑
            String destinationPath = getDestinationPath(destinationFolder, date);

            // 上傳GCP
            gcsRepository.putFile(destinationBucketName, destinationPath, APPLICATION_ZIP, zipData);

            logger.info("上傳成功");
        } catch (Exception e) {
            logger.error("上傳失敗");
            throw e;
        }
    }

    private ConcurrentHashMap<String, List<?>> getData(String date) {
        logger.info("即將至資料庫取得資料");
        ConcurrentHashMap<String, List<?>> returnData = new ConcurrentHashMap<>();

        Arrays.stream(ProcessType.values()).parallel().forEach(processType -> {
            logContext.setCurrentDate(date);
            NmnsService<?> nmnsService = nmnsBeanFactory.getNmnsService(processType.getNmnsService());

            List<?> dataList = nmnsService.findByDate(date);

            returnData.put(processType.getNmnsService(), dataList);

        });

        logger.info("至資料庫取得資料成功");

        return returnData;
    }

    private String createFileName(String date, int start, int end, String type) {
        return date +
                "_" +
                start + "-" + end +
                "_" +
                type +
                ".json";
    }

    public String getDestinationPath(String destinationFolder, String date) {
        return destinationFolder + date + ".zip";
    }

    protected ZipEntryData createZipEntryData(String fileName, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return new ZipEntryData(fileName, contentBytes);
    }

    private List<ZipEntryData> processData(ConcurrentHashMap<String, List<?>> data, String date) {
        logger.info("驗證資料並上傳S3開始");
        return data.entrySet().parallelStream()
                .flatMap(entry -> {
                    logContext.setCurrentDate(date);
                    String serviceType = entry.getKey();
                    List<?> dataList = entry.getValue();
                    String typeName = ProcessType.getByNmnsService(serviceType).getTypeName();
                    NmnsService<Object> nmnsService = nmnsBeanFactory.getNmnsService(serviceType);

                    // 驗證資料，上傳S3
                    @SuppressWarnings("unchecked")
                    List<Object> typedList = (List<Object>) dataList;
                    nmnsService.processData(typedList);

                    // 產生ZIP的json檔
                    return createZipEntries(dataList, date, typeName).stream();

                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), result -> {
                    logger.info("驗證資料並上傳S3成功");
                    return result;
                }));
    }

    private List<ZipEntryData> createZipEntries(List<?> dataList, String date, String typeName) {
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

    private byte[] createZipFile(List<ZipEntryData> zipEntries) {
        logger.info("建立ZIP檔案開始");
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipStream = new ZipOutputStream(byteArrayOutputStream)) {

            for (ZipEntryData entryData : zipEntries) {
                ZipEntry zipEntry = new ZipEntry(entryData.fileName());
                zipStream.putNextEntry(zipEntry);
                zipStream.write(entryData.content());
                zipStream.closeEntry();
            }

            zipStream.finish();

            logger.info("建立ZIP檔案成功");

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            logger.error("建立ZIP檔案失敗", e);
            throw new RuntimeException(e);
        }

    }


}
