package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "assessment_sn",
        "paper_sn",
        "uid",
        "openid_sub",
        "user_id",
        "score",
        "duration",
        "creation_timestamp",
        "update_timestamp"
})
public interface AssessmentDTO {

    @JsonProperty("assessment_sn")
    String getAssessmentSn();

    @JsonProperty("paper_sn")
    String getPaperSn();

    Long getUid();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    @JsonProperty("user_id")
    String getUserId();

    Integer getScore();

    Integer getDuration();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
