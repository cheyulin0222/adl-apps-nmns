package com.arplanet.adlappnmns.repository;

import com.arplanet.adlappnmns.log.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.ThrowingConsumer;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Repository
@RequiredArgsConstructor
@Slf4j
public class S3Repository {

    private static final int CHUNK_SIZE = 5 * 1024 * 1024; // 5MB
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
            PipedInputStream inputStream = null;
            PipedOutputStream outputStream = null;

            try {

                // 建立讀取端，設定緩衝區大小，當inputStream 資料達到上限，outputStream 的寫入就會阻塞
                inputStream = new PipedInputStream(CHUNK_SIZE);
                // 建立寫入端，連接到讀取端
                outputStream = new PipedOutputStream(inputStream);

                // 建立最終的輸入流引用，給 lambda 使用
                final PipedInputStream finalInputStream = inputStream;

                log.info("available={}", finalInputStream.available());

                // 非同步上傳
                CompletableFuture<PutObjectResponse> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        // 在新的線程中執行上傳操作
                        PutObjectRequest request = PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectName)
                                .acl(ObjectCannedACL.PRIVATE)
                                .contentType(contentType)
                                .build();

                        // 使用輸入流建立請求體並上傳
                        log.info("available={}", finalInputStream.available());

                        return s3Client.putObject(request,
                                // RequestBody 告訴 S3 這是資料的來源（inputStream）從這裡讀取資料並上傳
                                // finalInputStream - 要上傳的資料來源
                                // finalInputStream.available() - 嘗試取得可讀取的字節數（檔案大小）
                                RequestBody.fromInputStream(finalInputStream, 0)
                        );
                    } catch (Exception e) {
                        log.error("上傳失敗", e);
                        throw new CompletionException(e);
                    }
                });

                log.info("available={}", finalInputStream.available());

                // 執行外部寫入操作，會把檔案寫入到 outputStream ， 資料會直接流到 inputStream
                streamWriter.accept(outputStream);
                log.info("available={}", finalInputStream.available());
                // 寫完後關閉輸出流
                outputStream.close();

                // 等待CompletableFuture完成
                future.join();
                return;

            } catch (Exception e) {
                handleRetry(e, ++retryCount);
            } finally {
                if (outputStream != null) try { outputStream.close(); } catch (IOException e) { }
                if (inputStream != null) try { inputStream.close(); } catch (IOException e) { }
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

//    public void streamToS3(String bucketName, String objectName, String contentType, ThrowingConsumer<OutputStream> streamWriter) {
//        int retryCount = 0;
//
//        while(retryCount <= MAX_RETRIES) {
//            PipedInputStream inputStream = null;
//            PipedOutputStream outputStream = null;
//
//            try {
//
//                // 建立讀取端，設定緩衝區大小，當inputStream 資料達到上限，outputStream 的寫入就會阻塞
//                inputStream = new PipedInputStream(CHUNK_SIZE);
//                // 建立寫入端，連接到讀取端
//                outputStream = new PipedOutputStream(inputStream);
//
//                // 建立最終的輸入流引用，給 lambda 使用
//                final PipedInputStream finalInputStream = inputStream;
//
//
//                // 非同步上傳
//                CompletableFuture<PutObjectResponse> future = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        // 在新的線程中執行上傳操作
//                        PutObjectRequest request = PutObjectRequest.builder()
//                                .bucket(bucketName)
//                                .key(objectName)
//                                .acl(ObjectCannedACL.PRIVATE)
//                                .contentType(contentType)
//                                .build();
//
//                        // 使用輸入流建立請求體並上傳
//                        return s3Client.putObject(request,
//                                // RequestBody 告訴 S3：這是資料的來源（inputStream） 請你從這裡讀取資料並上傳
//                                // finalInputStream - 要上傳的資料來源
//                                // finalInputStream.available() - 嘗試取得可讀取的字節數（檔案大小）
//                                RequestBody.fromInputStream(finalInputStream, finalInputStream.available())
//                        );
//                    } catch (Exception e) {
//                        log.error("上傳失敗", e);
//                        return null;
//                    }
//                });
//
//                // 執行外部寫入操作，會把檔案寫入到 outputStream ， 資料會直接流到 inputStream
//                streamWriter.accept(outputStream);
//                // 寫完後關閉輸出流
//                outputStream.close();
//
//                // 等待上傳完成
//                try {
//                    future.join();
//                } catch (CompletionException e) {
//                    log.error("S3 上傳失敗", e);
//                    handleRetry(e, ++retryCount);
//                }
//
//            } catch (Exception e) {
//                handleRetry(e, ++retryCount);
//            } finally {
//                if (outputStream != null) try { outputStream.close(); } catch (IOException ignored) { }
//                if (inputStream != null) try { inputStream.close(); } catch (IOException ignored) { }
//            }
//        }
//    }
//
//    private void handleRetry(Exception e, int retryCount) {
//        if (retryCount > MAX_RETRIES) {
//            log.error("s3 上傳失敗超過 " + MAX_RETRIES + " 次", e);
//            return;
//        }
//        log.warn("上傳失敗，準備第 {} 次重試", retryCount, e);
//
//        try {
//            Thread.sleep((long) Math.pow(2, retryCount) * 1000L);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//            log.error("重試等待被中斷", ie);
//        }
//    }
}
