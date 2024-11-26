package com.arplanet.adlappnmns.service.s3;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.service.NmnsServiceBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

import static com.arplanet.adlappnmns.enums.ErrorType.SERVICE;
import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Slf4j
public abstract class NmnsS3ServiceBase<T, L> extends NmnsServiceBase<T> {

    @Value("${aws.s3.read.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.read.folder}")
    private String destinationFolder;

    @Autowired
    protected LogContext logContext;
    @Autowired
    protected Logger logger;

    @Override
    public List<T> findByDate(String date, ProcessContext processContext) {

        String serviceFolder = generateServicePath();

        List<String> filePathList = s3Repository.listFileNames(bucketName, destinationFolder + serviceFolder + "/" + date);

        // 讀取資料轉成Java物件的List
        List<LogBase<L>> logBaseList = filePathList.parallelStream()
                .flatMap(filePath -> {
                    logContext.setCurrentDate(date);
                    return readFile(filePath).stream();
                })
                .toList();

        return getData(logBaseList, date);

    }

    private List<LogBase<L>> readFile(String filePath) {
        String content;

        try {
            content = s3Repository.readFile(bucketName, filePath);
        } catch (Exception e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("file_path", filePath);
            logger.error("至S3讀取資料失敗", e, payload, SYSTEM);
            throw new RuntimeException(e);
        }

        String[] dataList = content.split("\n");
        List<LogBase<L>> result = new ArrayList<>();

        for (int i = 0; i < dataList.length; i++) {
            String data = dataList[i].trim();
            if (data.isEmpty()) {
                continue;
            }

            try {
                LogBase<L> parsedLog = objectMapper.readValue(data, getLogBaseTypeReference());
                result.add(parsedLog);

            } catch (Exception e) {
                HashMap<String, Object> payload = new HashMap<>();
                payload.put("file_path", filePath);
                payload.put("data", data);
                payload.put("line_number", i + 1);

                if (e instanceof JsonProcessingException) {
                    logger.error("JSON解析失敗", e, payload, SERVICE);
                } else {
                    logger.error("解析資料時發生未知錯誤", e, payload, SYSTEM);
                }
            }
        }
        return result;
    }

    protected abstract TypeReference<LogBase<L>> getLogBaseTypeReference();

    protected abstract  List<T> getData(List<LogBase<L>> logBaseList, String date);

    protected abstract String generateServicePath();
}
