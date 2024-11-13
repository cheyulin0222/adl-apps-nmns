package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "log_sn",
        "openid_sub",
        "user_id",
        "log_type",
        "material_sn",
        "start_timestamp",
        "end_timestamp",
        "expired_timestamp",
        "duration",
        "creation_timestamp",
        "update_timestamp"
})
public interface InstructionDataLogDTO {

    @JsonProperty("log_sn")
    String getLogSn();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    @JsonProperty("user_id")
    String getUserId();

    @JsonProperty("log_type")
    String getLogType();

    @JsonProperty("material_sn")
    String getMaterialSn();

    @JsonProperty("start_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getStartTimestamp();

    @JsonProperty("end_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getEndTimestamp();

    @JsonProperty("expired_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getExpiredTimestamp();

    Integer getDuration();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();

}
