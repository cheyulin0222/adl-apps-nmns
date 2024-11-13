package com.arplanet.adlappnmns.serializer;

import com.arplanet.adlappnmns.enums.City;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CitySerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        String code = City.getCodeByName(value);
        gen.writeString(code);
    }
}
