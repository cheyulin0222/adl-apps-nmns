package com.arplanet.adlappnmns.repository;

import com.arplanet.adlappnmns.log.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.ThrowingConsumer;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Repository
@RequiredArgsConstructor
@Slf4j
public class S3Repository {

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_RETRIES = 3;

    private final S3Client s3Client;
    private final Logger logger;

    public List<S3Object> listFiles(String bucketName, String folder) {
        // 產生ListObject請求物件
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folder)
                    .build();

            // 送出請求
            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            // 回傳內容
            return response.contents();

        }catch (S3Exception e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("bucket_name", bucketName);
            payload.put("file_path", folder);
            logger.error("嘗試從s3取得檔案時發生錯誤", e, payload, SYSTEM);
            throw e;
        }
    }

    public List<String> listFileNames(String bucketName, String folder) {
        return listFiles(bucketName, folder)
                .stream()
                .map(S3Object::key)
                .toList();
    }

    public List<String> listFileNamesExcludeFixedFiles(String bucketName, String rawFolder, String fixedFolder) {
        // 從S3取得raw-fixed的檔案
        Set<String> fixedFiles = listFiles(bucketName, fixedFolder)
                .stream()
                .map(S3Object::key)
                .collect(Collectors.toSet());

        // 從S3取得raw的檔案並排除raw-fixed的檔案
        // 產生要修改檔案的list
        return listFiles(bucketName, rawFolder)
                .stream()
                .map(S3Object::key)
                .filter(key -> !fixedFiles.contains(key.replace(rawFolder, fixedFolder)))
                .toList();
    }

    public String readFile(String bucketName, String fileName) {
        // 產生讀取請求物件
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        // 讀取
        try (ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(request);
             BufferedReader reader = new BufferedReader(new InputStreamReader(responseInputStream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\n"));

        }catch (Exception e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("bucket_name", bucketName);
            payload.put("file_path", fileName);
            logger.error("嘗試從s3讀取檔案時發生錯誤", e, payload, SYSTEM);
            throw new RuntimeException(e);
        }
    }

    public void putFile(String bucketName, String destinationPath, String contentType, byte[] content) {
        // 產生上傳物件
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(destinationPath)
                .acl(ObjectCannedACL.PRIVATE)
                .contentType(contentType)
                .build();
        // 送出請求
        s3Client.putObject(request, RequestBody.fromBytes(content));
    }

    public void streamToS3(String bucketName, String objectName, String contentType, ThrowingConsumer<OutputStream> streamWriter) {
        int retryCount = 0;

        while(true) {
            try {
                // 建立管道
                PipedOutputStream outputStream = new PipedOutputStream();
                PipedInputStream inputStream = new PipedInputStream(outputStream, CHUNK_SIZE);

                // 非同步線程：等待讀取輸入流並上傳
                CompletableFuture<PutObjectResponse> future = CompletableFuture.supplyAsync(() -> {
                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .acl(ObjectCannedACL.PRIVATE)
                            .contentType(contentType)
                            .build();

                    try (inputStream) {
                        // 準備好要從 inputStream 讀取資料
                        return s3Client.putObject(request, RequestBody.fromInputStream(inputStream, -1));
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                });

                try (outputStream) {
                    // 主程式寫入資料
                    streamWriter.accept(outputStream);
                    future.join();  // 等待上傳完成
                    return;
                }
            } catch (Exception e) {
                handleRetry(e, ++retryCount);
            }
        }
    }

    private void handleRetry(Exception e, int retryCount) {
        if (retryCount > MAX_RETRIES) {
            logger.error("s3 上傳失敗超過 " + MAX_RETRIES + " 次", e, SYSTEM);
            throw new RuntimeException(e);
        }
        log.warn("上傳失敗，準備第 {} 次重試", retryCount, e);

        try {
            Thread.sleep((long) Math.pow(2, retryCount) * 1000L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("重試等待被中斷", ie);
        }
    }
}
