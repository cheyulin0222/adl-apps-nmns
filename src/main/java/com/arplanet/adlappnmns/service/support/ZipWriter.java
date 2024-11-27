package com.arplanet.adlappnmns.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZipWriter {

    private final Map<ZipOutputStream, Object> lockMap = new ConcurrentHashMap<>();

    public void writeEntry(String fileName, String content, ZipOutputStream zipStream) {

        Object lock = lockMap.computeIfAbsent(zipStream, k -> new Object());

        try {
            synchronized (lock) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipStream.putNextEntry(zipEntry);
                zipStream.write(content.getBytes(StandardCharsets.UTF_8));
                zipStream.closeEntry();
            }
        } catch (Exception e) {
            lockMap.remove(zipStream);
            throw new RuntimeException(e);
        }
    }

    public void removeLock(ZipOutputStream zipStream) {
        lockMap.remove(zipStream);
    }
}
