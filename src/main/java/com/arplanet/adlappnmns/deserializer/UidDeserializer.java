package com.arplanet.adlappnmns.deserializer;

import com.arplanet.adlappnmns.enums.ErrorType;
import com.arplanet.adlappnmns.log.Logger;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static com.arplanet.adlappnmns.enums.ErrorType.SERVICE;

@RequiredArgsConstructor
public class UidDeserializer extends JsonDeserializer<String> {

    private final Logger logger;

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Object value = p.readValueAs(Object.class);

        if (value instanceof Number) {
            return String.valueOf(value); // 將數字轉為字串
        } else if (value instanceof String) {
            return (String) value; // 如果是字串，直接返回
        } else {
            logger.error("Invalid type for uid: " + value, SERVICE);
            return null;
        }
    }
}
