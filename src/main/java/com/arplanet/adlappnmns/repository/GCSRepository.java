package com.arplanet.adlappnmns.repository;

import com.google.api.gax.paging.Page;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.function.ThrowingConsumer;

import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GCSRepository {

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB
    private static final int MAX_RETRIES = 3;

    private final Storage storage;

    public Page<Blob> listFiles(String bucketName, String folder) {
        return storage.list(bucketName, Storage.BlobListOption.prefix(folder));
    }

    public List<String> listFileNames(String bucketName, String folder) {
        return listFiles(bucketName, folder)
                .streamAll()
                .map(Blob::getName)
                .collect(Collectors.toList());
    }

    public List<String> listFileNamesExcludeFixedFiles(String bucketName, String rawFolder, String fixedFolder) {
        return List.of();
    }

    public String readFile(String bucketName, String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob == null) {
            return null;
        }
        return new String(blob.getContent(), StandardCharsets.UTF_8);
    }

    public void putFile(String bucketName, String filePath, String contentType, byte[] content) {
        storage.create(
                BlobInfo.newBuilder(bucketName, filePath)
                        .setContentType(contentType)
                        .build(),
                content
        );
    }

    public void streamToGCS(String bucketName, String objectName, String contentType, ThrowingConsumer<OutputStream> streamWriter) {
        int retryCount = 0;

        while(true) {
            try {
                // 初始化但尚未連線
                WriteChannel writer = openWriteChannel(bucketName, objectName, contentType);
                writer.setChunkSize(CHUNK_SIZE);

                // 以channel產生一個outputStream，觸發service，將資料寫進outputStream
                try (OutputStream gcsOutputStream = Channels.newOutputStream(writer)) {
                    streamWriter.accept(gcsOutputStream);
                    return;
                }
            } catch (Exception e) {
                handleRetry(e, ++retryCount);
            }
        }
    }

    private WriteChannel openWriteChannel(String bucketName, String objectName, String contentType) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(contentType)
                .build();

        return storage.writer(blobInfo);
    }

    private void handleRetry(Exception e, int retryCount) {
        if (retryCount > MAX_RETRIES) {
            throw new RuntimeException("上傳失敗超過 " + MAX_RETRIES + " 次", e);
        }
        log.warn("上傳失敗，準備第 {} 次重試", retryCount, e);

        try {
            // 指數退避策略
            Thread.sleep((long) Math.pow(2, retryCount) * 1000L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("重試等待被中斷", ie);
        }
    }
}
