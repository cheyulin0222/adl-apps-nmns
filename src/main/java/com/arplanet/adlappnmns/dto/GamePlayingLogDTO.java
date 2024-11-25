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
        "log_sn",
        "uid",
        "openid_sub",
        "user_id",
        "game_sn",
        "correctness",
        "content",
        "reward",
        "score",
        "click_num",
        "play_num",
        "pass",
        "back_to_adl",
        "support_usage",
        "support_usage_num",
        "completion_after_support_time",
        "attempts_after_support_num",
        "creation_timestamp",
        "update_timestamp"
})
public class GamePlayingLogDTO {

    @JsonProperty("log_sn")
    private String logSn;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("openid_sub")
    private String openidSub;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("game_sn")
    private String gameSn;

    @JsonProperty("correctness")
    private String correctness;

    @JsonProperty("content")
    private String content;

    @JsonProperty("reward")
    private Integer reward;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("click_num")
    private Integer clickNum;

    @JsonProperty("play_num")
    private Integer playNum;

    @JsonProperty("pass")
    private Boolean pass;

    @JsonProperty("back_to_adl")
    private Boolean backToAdl;

    @JsonProperty("support_usage")
    private Boolean supportUsage;

    @JsonProperty("support_usage_num")
    private Integer supportUsageNum;

    @JsonProperty("completion_after_support_time")
    private String completionAfterSupportTime;

    @JsonProperty("attempts_after_support_num")
    private Integer attemptsAfterSupportNum;

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp creationTimestamp;

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp updateTimestamp;
}
