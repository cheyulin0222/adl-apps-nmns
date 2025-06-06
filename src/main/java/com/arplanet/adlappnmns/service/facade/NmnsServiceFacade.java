package com.arplanet.adlappnmns.service.facade;

import com.arplanet.adlappnmns.config.NmnsBeanFactory;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.GCSRepository;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.arplanet.adlappnmns.service.NmnsService;
import com.arplanet.adlappnmns.service.support.ZipWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;
import static com.arplanet.adlappnmns.utils.ServiceUtil.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class NmnsServiceFacade {

    private final NmnsBeanFactory nmnsBeanFactory;
    private final GCSRepository gcsRepository;
    private final S3Repository s3Repository;
    private final Logger logger;
    private final LogContext logContext;
    private final ZipWriter zipWriter;

    @Value("${gcs.bucket.name}")
    private String gcpBucketName;

    @Value("${gcs.destination.folder}")
    private String gcpZipFolder;

    @Value("${aws.s3.write.bucket.name}")
    private String s3bucketName;

    @Value("${aws.s3.zip.write.folder}")
    private String s3ZipBackupFolder;

    public void process(String date) {
        try {
            logger.info("教育大數據開始執行");
            logger.info("執行日期: " + date);

            // 產生GCP zip路徑
            String gcpDestinationPath = getDestinationPath(gcpZipFolder, date);
            // 產生s3 zip備份路徑
            String s3DestinationPath = getDestinationPath(s3ZipBackupFolder, date);


            // 先取得GCS bucket 的 OutputStream，將 OutputStream 傳給各 Service 使用串流方式上傳到 GCS
            gcsRepository.streamToGCS(
                    gcpBucketName,
                    gcpDestinationPath,
                    APPLICATION_ZIP,
                    gcsOutputStream  ->
                            s3Repository.streamToS3(
                                    s3bucketName,
                                    s3DestinationPath,
                                    APPLICATION_ZIP,
                                    s3OutputStream -> processAndWriteToStream(date, gcsOutputStream, s3OutputStream)
                            )

            );

            logger.info("上傳成功");
        } catch (Exception e) {
            logger.error("上傳失敗", SYSTEM);
            throw e;
        }
    }

    private void processAndWriteToStream(String date, OutputStream gcsOutputStream, OutputStream s3OutputStream) throws Exception {
        try (
            ZipOutputStream gcsZipStream  = new ZipOutputStream(gcsOutputStream);
            ZipOutputStream s3ZipStream = new ZipOutputStream(s3OutputStream)
        ) {
            // 先取得各個 service 執行前需要的共同資料
            ProcessContext processContext = getContext(date);

            // 執行 service
            // 取得資料 -> 驗證資料 -> 備份 -> 上傳 GCS
            Arrays.stream(ProcessType.values())
                    .filter(processType -> !processType.isPreContext())
                    .peek(processType -> logContext.setCurrentDate(date))
                    .forEach(processType -> {
                        NmnsService<?> nmnsService = nmnsBeanFactory.getNmnsService(processType.getNmnsService());
                        nmnsService.doProcess(date, processContext, gcsZipStream, s3ZipStream);
                    });

            gcsZipStream.finish();
            s3ZipStream.finish();
            zipWriter.removeLock(gcsZipStream);
            zipWriter.removeLock(s3ZipStream);
        }
    }

    private ProcessContext getContext(String date) {
        logger.info("產生 Service 共用資料");
        try {
            Map<String, List<?>> resultMap = Arrays.stream(ProcessType.values())
                    .filter(ProcessType::isPreContext)
                    .parallel()
                    .peek(processType -> logContext.setCurrentDate(date))
                    .collect(Collectors.toMap(
                            ProcessType::name,
                            processType -> {
                                NmnsService<?> nmnsService = nmnsBeanFactory.getNmnsService(processType.getNmnsService());
                                return nmnsService.findByDate(date, null);
                            }
                    ));

            logger.info("產生 Service 共用資料成功");

            return ProcessContext.builder()
                    .typeListMap(resultMap)
                    .build();
        } catch (Exception e) {
            logger.error("產生 Service 共用資料失敗", e, SYSTEM);
            throw e;
        }
    }

    private String getDestinationPath(String destinationFolder, String date) {
        return destinationFolder + date + ".zip";
    }
}
