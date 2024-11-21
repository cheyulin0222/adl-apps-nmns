package com.arplanet.adlappnmns.service.facade;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.dto.*;
import com.arplanet.adlappnmns.enums.ProcessType;
import com.arplanet.adlappnmns.repository.LocalRepository;
import com.arplanet.adlappnmns.repository.S3Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.ErrorManager;

@Service
@RequiredArgsConstructor
public class FirstDateService {


    private final LocalRepository localRepository;
    private final ObjectMapper objectMapper;

    @Value("${local.destination.folder}")
    private String localDestinationFolder;

    public void processFirstDate() {

        List<String> filePathList = localRepository.listFileNames(localDestinationFolder);

        ConcurrentLinkedQueue<UserInfoDTO> userInfoDTOList = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MaterialInfoDTO> materialInfoDTOList = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<ClassInfoDTO> classInfoDTOList = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<ItemInfoDTO> itemInfoDTOList = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<PaperDTO> paperDTOList = new ConcurrentLinkedQueue<>();


        filePathList.parallelStream().forEach(filePath -> {
            Map<String, String> content = localRepository.readZipFile(filePath);

            content.entrySet().parallelStream().forEach(entry -> {
                String fileName = entry.getKey();
                String jsonContent = entry.getValue();

                if (fileName.contains("user_info")) {
                    List<UserInfoDTO> dtos = objectMapper.readValue(jsonContent,
                            new TypeReference<List<UserInfoDTO>>() {});
                    userInfoDTOList.addAll(dtos);
                }
                else if (fileName.contains("material_info")) {
                    List<MaterialInfoDTO> dtos = objectMapper.readValue(jsonContent,
                            new TypeReference<List<MaterialInfoDTO>>() {});
                    materialInfoDTOList.addAll(dtos);
                }
                else if (fileName.contains("class_info")) {
                    List<ClassInfoDTO> dtos = objectMapper.readValue(jsonContent,
                            new TypeReference<List<ClassInfoDTO>>() {});
                    classInfoDTOList.addAll(dtos);
                }
                else if (fileName.contains("item_info")) {
                    List<ItemInfoDTO> dtos = objectMapper.readValue(jsonContent,
                            new TypeReference<List<ItemInfoDTO>>() {});
                    itemInfoDTOList.addAll(dtos);
                }
                else if (fileName.contains("paper")) {
                    List<PaperDTO> dtos = objectMapper.readValue(jsonContent,
                            new TypeReference<List<PaperDTO>>() {});
                    paperDTOList.addAll(dtos);
                }
            });

        });

    }

    private T ConcurrentLinkedQueue<?> getList(String serviceType) {
        if (serviceType.contains("user_info")) return userInfoDTOList;
    }


}
