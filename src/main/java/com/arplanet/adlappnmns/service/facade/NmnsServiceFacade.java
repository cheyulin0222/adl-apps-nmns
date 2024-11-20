package com.arplanet.adlappnmns.service.facade;

import com.arplanet.adlappnmns.config.NmnsBeanFactory;
import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.record.ZipEntryData;
import com.arplanet.adlappnmns.repository.GCSRepository;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.arplanet.adlappnmns.repository.nmns.NmnsS3Repository;
import com.arplanet.adlappnmns.service.NmnsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.arplanet.adlappnmns.utils.ServiceUtil.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class NmnsServiceFacade {

    private final ObjectMapper objectMapper;
    private S3Repository s3Repository;
    private final NmnsBeanFactory nmnsBeanFactory;
    private final GCSRepository gcsRepository;
    private final Logger logger;
    private final LogContext logContext;

    @Value("${aws.s3.read.bucket.name}")
    private String s3BucketName;

    @Value("${aws.s3.read.folder}")
    private String s3ReadFolder;


    @Value("${gcs.bucket.name}")
    private String gcsDestinationBucketName;

    @Value("${gcs.destination.folder}")
    private String gcsDestinationFolder;

    public void process(String date) {
        try {
            logger.info("教育大數據開始執行");
            logger.info("執行日期: " + date);

            List<ZipEntryData> zipEntries = getData(date);

            // 產生zip
            byte[] zipData = createZipFile(zipEntries);

            // 產生GCP路徑
            String destinationPath = getDestinationPath(gcsDestinationFolder, date);

            // 上傳GCP
            gcsRepository.putFile(gcsDestinationBucketName, destinationPath, APPLICATION_ZIP, zipData);

            logger.info("上傳成功");
        } catch (Exception e) {
            logger.error("上傳失敗");
            throw e;
        }
    }

    private List<ZipEntryData> getData(String date) {

        ProcessContext processContext = getContext(date);

        return Arrays.stream(ProcessType.values()).parallel()
                .flatMap(processType -> {
                    logContext.setCurrentDate(date);
                    NmnsService<?> nmnsService = nmnsBeanFactory.getNmnsService(processType.getNmnsService());

                    return nmnsService.doProcess(date, processContext).stream();
                })
                .collect(Collectors.toList());
    }
    
    private ProcessContext getContext(String date) {
        List<String> sessionInfoFileNameList = s3Repository.listFileNames(s3BucketName, s3ReadFolder + "session.info/" + date);

        // 讀取資料轉成Java物件的List
        List<LogBase<SessionInfoLogContext>> sessionInfoList = sessionInfoFileNameList.parallelStream()
                .flatMap(filePath -> {
                    logContext.setCurrentDate(date);
                    return readFile(filePath).stream();
                })
                .toList();

        return ProcessContext.builder()
                .sessionInfoList(sessionInfoList)
                .build();
    }

    private String getDestinationPath(String destinationFolder, String date) {
        return destinationFolder + date + ".zip";
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

    private List<LogBase<SessionInfoLogContext>> readFile(String filePath) {
        String content = null;
        try {
            content = s3Repository.readFile(s3BucketName, filePath);
            String[] dataList = content.split("\n");
            List<LogBase<SessionInfoLogContext>> result = new ArrayList<>();

            for (String data : dataList) {
                if (!data.trim().isEmpty()) {
                    result.add(objectMapper.readValue(data, new TypeReference<>() {}));
                }
            }
            return result;

        } catch (JsonProcessingException e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("file_path", filePath);
            payload.put("content", content);
            logger.error("至S3讀取資料，JSON解析失敗", e, payload);
            throw new RuntimeException(e);
        } catch (S3Exception e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("file_path", filePath);
            logger.error("至S3讀取資料失敗", e, payload);
            throw new RuntimeException(e);
        }
    }
}
