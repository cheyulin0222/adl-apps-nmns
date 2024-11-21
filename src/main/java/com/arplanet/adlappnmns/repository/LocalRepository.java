package com.arplanet.adlappnmns.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Repository
@Slf4j
public class LocalRepository {

    public void putFile(String localPath, String applicationZip, byte[] content) {
        try {
            // 確保目標資料夾存在
            File directory = new File(localPath).getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 寫入檔案
            try (FileOutputStream fos = new FileOutputStream(localPath)) {
                fos.write(content);
            }

            log.info("成功寫入本地檔案: {}", localPath);

        } catch (Exception e) {
            log.error("寫入本地檔案失敗: " + localPath, e);
            throw new RuntimeException("寫入本地檔案失敗", e);
        }
    }

    public Map<String, String> readZipFile(String localPath) {
        try (FileInputStream fis = new FileInputStream(localPath);
             ZipInputStream zipStream = new ZipInputStream(fis)) {

            Map<String, String> fileContents = new HashMap<>();
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    fileContents.put(entry.getName(), readZipEntry(zipStream));
                }
                zipStream.closeEntry();
            }

            return fileContents;

        } catch (Exception e) {
            log.error("讀取本地zip檔案失敗: " + localPath, e);
            throw new RuntimeException("讀取本地zip檔案失敗", e);
        }
    }

    private String readZipEntry(ZipInputStream zipStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public List<String> listFileNames(String folder) {
        File directory = new File(folder);
        if (!directory.exists() || !directory.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
