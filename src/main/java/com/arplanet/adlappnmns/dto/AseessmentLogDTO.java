package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "assessment_log_sn",
        "assessment_sn",
        "item_sn",
        "uid",
        "openid_sub",
        "user_id",
        "start_timestamp",
        "end_timestamp",
        "duration",
        "correctness",
        "user_answer",
        "creation_timestamp",
        "update_timestamp"
})
public interface AseessmentLogDTO {

    @JsonProperty("assessment_log_sn")
    String getAssessmentLogSn();

    @JsonProperty("assessment_sn")
    String getAssessmentSn();

    @JsonProperty("item_sn")
    String getItemSn();

    Long getUid();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    @JsonProperty("user_id")
    String getUserId();

    @JsonProperty("start_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getStartTimestamp();

    @JsonProperty("end_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getEndTimestamp();

    Integer getDuration();

    String getCorrectness();

    @JsonProperty("user_answer")
    String getUserAnswer();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}


