package com.arplanet.adlappnmns.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.sql.Timestamp;

public class UnixTimestampDeserializer extends JsonDeserializer<Timestamp> {


    @Override
    public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        long unixTime = p.getLongValue();
        return new Timestamp(unixTime * 1000);
    }
}
