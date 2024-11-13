package com.arplanet.adlappnmns.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CustomTimestampSerializer extends JsonSerializer<Timestamp> {

    @Override
    public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        OffsetDateTime offsetDateTime = value.toInstant().atOffset(ZoneOffset.ofHours(8));
        gen.writeString(offsetDateTime.toString());
    }
}
