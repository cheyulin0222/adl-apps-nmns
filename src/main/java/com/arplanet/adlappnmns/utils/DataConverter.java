package com.arplanet.adlappnmns.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public  class DataConverter {

    private final ObjectMapper objectMapper;

    public <T> Map<String, Object> convertToMap(T data) {
        return objectMapper.convertValue(data, new TypeReference<>() {});
    }
}
