package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
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
public class AssessmentLogDTO {

    @JsonProperty("assessment_log_sn")
    private String assessmentLogSn;

    @JsonProperty("assessment_sn")
    private String assessmentSn;

    @JsonProperty("item_sn")
    private String itemSn;

    private String uid;

    @JsonProperty("openid_sub")
    private String openidSub;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("start_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp startTimestamp;

    @JsonProperty("end_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp endTimestamp;

    private Integer duration;

    private String correctness;

    @JsonProperty("user_answer")
    private String userAnswer;

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp creationTimestamp;

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp updateTimestamp;
}
