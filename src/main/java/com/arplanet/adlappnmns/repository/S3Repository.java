package com.arplanet.adlappnmns.repository;

import com.arplanet.adlappnmns.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Repository
public class S3Repository {

    @Autowired
    private S3Client s3Client;
    @Autowired
    private Logger logger;

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
}
