package com.arplanet.adlappnmns.service.facade;

import com.arplanet.adlappnmns.dto.*;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.record.TypeData;
import com.arplanet.adlappnmns.record.ZipEntryData;
import com.arplanet.adlappnmns.repository.GCSRepository;
import com.arplanet.adlappnmns.repository.LocalRepository;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.arplanet.adlappnmns.utils.ServiceUtil.APPLICATION_ZIP;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirstDateService {

    private final int PACKAGE_SIZE = 5000;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


    private final GCSRepository gcsRepository;
    private final LocalRepository localRepository;
    private final ObjectMapper objectMapper;

    @Value("${local.destination.folder}")
    private String localDestinationFolder;

    @Value("${gcs.bucket.name}")
    private String destinationBucketName;

    @Value("${gcs.destination.folder}")
    private String gcsDestinationFolder;

    public void processFirstDate(String date) {

        List<String> filePathList = localRepository.listFileNames(localDestinationFolder);

        Map<ProcessType, TypeData<?>> typeDataMap = new EnumMap<>(ProcessType.class);
        typeDataMap.put(ProcessType.USER_INFO, new TypeData<>(new ConcurrentLinkedQueue<UserInfoDTO>(), new TypeReference<List<UserInfoDTO>>() {}));
        typeDataMap.put(ProcessType.MATERIAL_INFO, new TypeData<>(new ConcurrentLinkedQueue<MaterialInfoDTO>(), new TypeReference<List<MaterialInfoDTO>>() {}));
        typeDataMap.put(ProcessType.CLASS_INFO, new TypeData<>(new ConcurrentLinkedQueue<ClassInfoDTO>(), new TypeReference<List<ClassInfoDTO>>() {}));
        typeDataMap.put(ProcessType.ITEM_INFO, new TypeData<>(new ConcurrentLinkedQueue<ItemInfoDTO>(), new TypeReference<List<ItemInfoDTO>>() {}));
        typeDataMap.put(ProcessType.PAPER, new TypeData<>(new ConcurrentLinkedQueue<PaperDTO>(), new TypeReference<List<PaperDTO>>() {}));



        filePathList.parallelStream().forEach(filePath -> {
            Map<String, String> content = localRepository.readZipFile(filePath);

            content.entrySet().parallelStream().forEach(entry -> {
                String fileName = entry.getKey();
                String jsonContent = entry.getValue();

                typeDataMap.entrySet().stream()
                        .filter(typeEntry -> fileName.contains(typeEntry.getKey().getTypeName()))
                        .findFirst()
                        .ifPresent(typeEntry -> {
                            TypeData<?> typeData = typeEntry.getValue();
                            try {
                                typeData.addJsonContent(jsonContent, objectMapper);
                            } catch (JsonProcessingException e) {
                                log.error("{} JsonProcessing失敗", fileName);
                                throw new RuntimeException(e);
                            }
                        });
            });
        });

        List<ZipEntryData> allZipEntries = typeDataMap.entrySet().stream()
                .flatMap(entry -> createZipEntries(
                        List.of(entry.getValue().getQueue()),
                        date,
                        entry.getKey().getTypeName()
                ).stream())
                .collect(Collectors.toList());


        byte[] zipData = createZipFile(allZipEntries);

        String destinationPath = getDestinationPath(date, false);

        putFile(destinationPath, zipData, false);

    }

    private List<ZipEntryData> createZipEntries(List<?> dataList, String date, String typeName) {
        DefaultPrettyPrinter prettyPrinter = ServiceUtil.createPrettyPrinter();

        if (dataList.isEmpty()) {
            return List.of(createZipEntryData(createFileName(date, 0, 0, typeName), "[]"));
        }

        // 每5000筆包成一個json檔
        return IntStream.range(0, (dataList.size() + PACKAGE_SIZE - 1) / PACKAGE_SIZE)
                .mapToObj(i -> {
                    int start = i * PACKAGE_SIZE;
                    int end = Math.min(start + PACKAGE_SIZE, dataList.size());
                    List<?> subList = dataList.subList(start, end);
                    try {
                        String jsonContent = mapper.writer(prettyPrinter).writeValueAsString(subList);
                        return createZipEntryData(createFileName(date, start + 1, end, typeName), jsonContent);
                    } catch (Exception e) {
                        log.error("建立ZIP檔案的" + typeName + "Json檔失敗", e);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private ZipEntryData createZipEntryData(String fileName, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return new ZipEntryData(fileName, contentBytes);
    }

    private String createFileName(String date, int start, int end, String type) {
        return date +
                "_" +
                start + "-" + end +
                "_" +
                type +
                ".json";
    }

    private byte[] createZipFile(List<ZipEntryData> zipEntries) {
        log.info("建立ZIP檔案開始");
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipStream = new ZipOutputStream(byteArrayOutputStream)) {

            for (ZipEntryData entryData : zipEntries) {
                ZipEntry zipEntry = new ZipEntry(entryData.fileName());
                zipStream.putNextEntry(zipEntry);
                zipStream.write(entryData.content());
                zipStream.closeEntry();
            }

            zipStream.finish();

            log.info("建立ZIP檔案成功");

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            log.error("建立ZIP檔案失敗", e);
            throw new RuntimeException(e);
        }
    }

    public String getDestinationPath(String date, boolean isFirstDate) {
        if (isFirstDate) {
            return localDestinationFolder + date + ".zip";
        }
        return gcsDestinationFolder + date + ".zip";
    }

    private void putFile(String destinationPath, byte[] zipData, boolean isFirstDate) {
        if (isFirstDate) {
            localRepository.putFile(destinationPath, APPLICATION_ZIP, zipData);
        }
        gcsRepository.putFile(destinationBucketName, destinationPath, APPLICATION_ZIP, zipData);
    }





}
