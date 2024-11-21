package com.arplanet.adlappnmns.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TypeData<T> {

    private final ConcurrentLinkedQueue<T> queue;
    private final TypeReference<List<T>> typeReference;

    public TypeData(ConcurrentLinkedQueue<T> queue, TypeReference<List<T>> typeReference) {
        this.queue = queue;
        this.typeReference = typeReference;
    }

    public void addJsonContent(String jsonContent, ObjectMapper objectMapper) throws JsonProcessingException {
        List<T> dtos = objectMapper.readValue(jsonContent, typeReference);
        queue.addAll(dtos);
    }

    public ConcurrentLinkedQueue<T> getQueue() {
        return queue;
    }
}
