package com.arplanet.adlappnmns.service.s3;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.service.NmnsServiceBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.*;
import java.util.stream.Collectors;


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
        List<LogBase<L>> LogBaseList = filePathList.parallelStream()
                .flatMap(filePath -> {
                    logContext.setCurrentDate(date);
                    return readFile(filePath).stream();
                })
                .toList();

        // 依照各個service資料的ID組成一個Map
        Map<String, List<LogBase<L>>> logBaseGroup = LogBaseList.parallelStream()
                .collect(Collectors.groupingBy(logBase -> {
                    logContext.setCurrentDate(date);
                    return getGroupingKey(logBase);
                }));

        return getData(logBaseGroup, date);

    }

    private List<LogBase<L>> readFile(String filePath) {
        String content = null;
        try {
            content = s3Repository.readFile(bucketName, filePath);
            String[] dataList = content.split("\n");
            List<LogBase<L>> result = new ArrayList<>();

            for (String data : dataList) {
                if (!data.trim().isEmpty()) {
                    result.add(objectMapper.readValue(data, getLogBaseTypeReference()));
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

    protected abstract TypeReference<LogBase<L>> getLogBaseTypeReference();

    protected abstract String getGroupingKey(LogBase<L> logBase);

    protected abstract  List<T> getData(Map<String, List<LogBase<L>>> logBaseGroup, String date);

    protected abstract String generateServicePath();
}
