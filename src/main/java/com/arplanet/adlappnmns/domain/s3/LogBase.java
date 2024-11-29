package com.arplanet.adlappnmns.domain.s3;

import com.arplanet.adlappnmns.deserializer.UidDeserializer;
import com.arplanet.adlappnmns.deserializer.UnixTimestampDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class LogBase<T> {

    @JsonProperty("log_group")
    private String logGroup;

    @JsonProperty("log_level")
    private String logLevel;

    @JsonProperty("log_sn")
    private String logSn;

    @JsonDeserialize(using = UidDeserializer.class)
    private String uid;

    @JsonProperty("action_type")
    private String actionType;

    @JsonProperty("object_type")
    private String objectType;

    private T context;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("action_timestamp_ms")
    private Timestamp actionTimestampMs;

    @JsonProperty("event_timestamp")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    private Timestamp eventTimestamp;

    @JsonProperty("time_8601")
    private String time;
}
