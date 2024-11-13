package com.arplanet.adlappnmns.repository;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GCSRepository {

    private final Storage storage;

    public Page<Blob> listFiles(String bucketName, String folder) {
        return storage.list(bucketName, Storage.BlobListOption.prefix(folder));
    }

    public List<String> listFileNames(String bucketName, String folder) {
        return List.of();
    }

    public List<String> listFileNamesExcludeFixedFiles(String bucketName, String rawFolder, String fixedFolder) {
        return List.of();
    }

    public String readFile(String bucketName, String fileName) {
        return "";
    }

    public void putFile(String bucketName, String filePath, String contentType, byte[] content) {
        storage.create(
                BlobInfo.newBuilder(bucketName, filePath)
                        .setContentType(contentType)
                        .build(),
                content
        );
    }
}
