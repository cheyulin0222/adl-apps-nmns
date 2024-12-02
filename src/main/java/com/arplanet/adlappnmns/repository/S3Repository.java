package com.arplanet.adlappnmns.repository;

import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.utils.SizeDetectingOutputStream;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Repository
@RequiredArgsConstructor
@Slf4j
public class S3Repository {

    private static final int PART_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
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

        while(retryCount <= MAX_RETRIES) {
            try {

                //
                SizeDetectingOutputStream detector = getSizeDetectingOutputStream(bucketName, objectName, contentType);

                // 寫入資料
                streamWriter.accept(detector);

                // 如果沒有超過閾值，使用一般的 putObject
                if (!detector.isExceedThreshold()) {
                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .acl(ObjectCannedACL.PRIVATE)
                            .contentType(contentType)
                            .build();

                    s3Client.putObject(request,
                            RequestBody.fromBytes(detector.getBuffer())
                    );
                }
                return;

            } catch (Exception e) {
                handleRetry(e, ++retryCount);
            }
        }
    }

    private SizeDetectingOutputStream getSizeDetectingOutputStream(String bucketName, String objectName, String contentType) {
        SizeDetectingOutputStream detector = new SizeDetectingOutputStream();

        detector.setCallback(() -> {
            log.info("檔案超過 5MB，切換到 multipart upload");
            try {
                // 建立新的管道
                PipedInputStream inputStream = new PipedInputStream(BUFFER_SIZE);
                PipedOutputStream outputStream = new PipedOutputStream(inputStream);

                // 啟動 multipart upload
                CompletableFuture.runAsync(() -> startMultipartUpload(bucketName, objectName, contentType, inputStream));

                return outputStream;  // 返回新的 output stream
            } catch (Exception e) {
                throw new RuntimeException("初始化 multipart upload 失敗", e);
            }
        });
        return detector;
    }

    private void startMultipartUpload(String bucketName, String objectName, String contentType, InputStream inputStream) {
        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .contentType(contentType)
                .build();

        String uploadId = null;

        // 用來儲存每個分段上傳的異步任務，會在上傳完成後提供分段上傳的結果
        List<CompletableFuture<CompletedPart>> uploadFutures = new ArrayList<>();
        // 追蹤當前上傳是第幾個分段
        AtomicInteger partNumberCounter = new AtomicInteger(1);
        byte[] buffer = new byte[PART_SIZE];

        try {
            // 初始化 multipart upload
            CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
            uploadId = createResponse.uploadId();
            final String finalUploadId = uploadId;

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                int partNumber = partNumberCounter.getAndIncrement();
                byte[] dataChunk = Arrays.copyOf(buffer, bytesRead); // 必須拷貝，避免異步處理時數據被覆蓋

                // 將異步上傳任務添加到uploadFutures 中
                // CompletableFuture.supplyAsync() 方法會啟動一個新的異步任務，並且在新線程中執行指定的任務
                // supplyAsync()會接受一個 Supplier，它是一個函數接口，當 supplyAsync 被調用時，會在一個獨立的線程中執行該函數，並且可以返回一個結果
                uploadFutures.add(CompletableFuture.supplyAsync(() -> uploadPart(bucketName, objectName, finalUploadId, partNumber, dataChunk)));
            }

            // 等待所有分段上傳完成
            List<CompletedPart> completedParts = uploadFutures.stream()
                    .map(CompletableFuture::join) // 等待異步結果
                    .toList();

            // 完成 multipart upload
            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build();

            s3Client.completeMultipartUpload(completeRequest);
            log.info("完成 multipart upload，objectName={}", objectName);

        } catch (Exception e) {
            log.error("multipart upload 失敗", e);
            if (uploadId != null) {
                abortMultipartUpload(bucketName, objectName, uploadId);
            }
            throw new RuntimeException("multipart upload 發生錯誤", e);
        }
    }

    private CompletedPart uploadPart(String bucketName, String objectName, String uploadId, int partNumber, byte[] dataChunk) {
        try {
            log.debug("上傳分段：partNumber={}, size={}", partNumber, dataChunk.length);
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

            // S3 中，ETag 是每個對象的唯一標識符，用於檢查對象是否發生了變化
            String eTag = s3Client.uploadPart(uploadPartRequest, RequestBody.fromBytes(dataChunk)).eTag();
            return CompletedPart.builder()
                    .partNumber(partNumber)
                    .eTag(eTag)
                    .build();

        } catch (Exception e) {
            log.error("上傳分段失敗，partNumber={}", partNumber, e);
            throw new RuntimeException("分段上傳失敗", e);
        }
    }

    private void abortMultipartUpload(String bucketName, String objectName, String uploadId) {
        try {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .uploadId(uploadId)
                    .build();
            s3Client.abortMultipartUpload(abortRequest);
            log.warn("已中止 multipart upload，uploadId={}", uploadId);
        } catch (Exception e) {
            log.error("中止 multipart upload 失敗", e);
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
