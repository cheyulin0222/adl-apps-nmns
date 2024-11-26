package com.arplanet.adlappnmns.service.facade;

import com.arplanet.adlappnmns.config.NmnsBeanFactory;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.record.ZipEntryData;
import com.arplanet.adlappnmns.repository.GCSRepository;
import com.arplanet.adlappnmns.service.NmnsService;
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
    private final Logger logger;
    private final LogContext logContext;

    @Value("${gcs.bucket.name}")
    private String gcsDestinationBucketName;

    @Value("${gcs.destination.folder}")
    private String gcsDestinationFolder;

    public void process(String date) {
        try {
            logger.info("教育大數據開始執行");
            logger.info("執行日期: " + date);

            // 產生GCP路徑
            String destinationPath = getDestinationPath(gcsDestinationFolder, date);

            // 使用串流方式上傳到 GCS
            gcsRepository.streamToGCS(
                    gcsDestinationBucketName,
                    destinationPath,
                    APPLICATION_ZIP,
                    outputStream -> processAndWriteToStream(date, outputStream)
            );


//            List<ZipEntryData> zipEntries = getData(date);

//            // 產生zip
//            byte[] zipData = createZipFile(zipEntries);

            // 產生GCP路徑
//            String destinationPath = getDestinationPath(gcsDestinationFolder, date);

            // 上傳GCP
//            gcsRepository.putFile(gcsDestinationBucketName, destinationPath, APPLICATION_ZIP, zipData);

            logger.info("上傳成功");
        } catch (Exception e) {
            logger.error("上傳失敗", SYSTEM);
            throw e;
        }
    }

    private void processAndWriteToStream(String date, OutputStream outputStream) throws Exception {
        try (ZipOutputStream zipStream = new ZipOutputStream(outputStream)) {
            ProcessContext processContext = getContext(date);

            Arrays.stream(ProcessType.values())
                    .filter(processType -> !processType.isPreContext())
                    .forEach(processType -> {
                        logContext.setCurrentDate(date);
                        NmnsService<?> nmnsService = nmnsBeanFactory.getNmnsService(processType.getNmnsService());
                        nmnsService.doProcess(date, processContext, zipStream);
                    });

            zipStream.finish();
        }
    }

//    private List<ZipEntryData> getData(String date) {
//
//        ProcessContext processContext = getContext(date);
//
//        return Arrays.stream(ProcessType.values())
//                .filter(processType -> !processType.isPreContext())
//                .parallel()
//                .peek(processType -> logContext.setCurrentDate(date))
//                .flatMap(processType -> {
//                    NmnsService<?> nmnsService = nmnsBeanFactory.getNmnsService(processType.getNmnsService());
//                    return nmnsService.doProcess(date, processContext).stream();
//                })
//                .collect(Collectors.toList());
//    }

    private ProcessContext getContext(String date) {
        logger.info("處理 ProcessContext");
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

            return ProcessContext.builder()
                    .typeListMap(resultMap)
                    .build();
        } catch (Exception e) {
            logger.error("處理 ProcessContext 失敗", e, SYSTEM);
            throw e;
        }
    }

    private String getDestinationPath(String destinationFolder, String date) {
        return destinationFolder + date + ".zip";
    }

//    private byte[] createZipFile(List<ZipEntryData> zipEntries) {
//        logger.info("建立ZIP檔案開始");
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//             ZipOutputStream zipStream = new ZipOutputStream(byteArrayOutputStream)) {
//
//            for (ZipEntryData entryData : zipEntries) {
//                ZipEntry zipEntry = new ZipEntry(entryData.fileName());
//                zipStream.putNextEntry(zipEntry);
//                zipStream.write(entryData.content());
//                zipStream.closeEntry();
//            }
//
//            zipStream.finish();
//
//            logger.info("建立ZIP檔案成功");
//
//            return byteArrayOutputStream.toByteArray();
//
//        } catch (Exception e) {
//            logger.error("建立ZIP檔案失敗", e, SYSTEM);
//            throw new RuntimeException(e);
//        }
//    }
}
