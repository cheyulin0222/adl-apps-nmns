package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

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
public interface GamePlayingLogDTO {

    @JsonProperty("log_sn")
    String getLogSn();

    @JsonProperty("uid")
    String getUid();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    @JsonProperty("user_id")
    String getUserId();

    @JsonProperty("game_sn")
    String getGameSn();

    @JsonProperty("correctness")
    String getCorrectness();

    @JsonProperty("content")
    String getContent();

    @JsonProperty("reward")
    Integer getReward();

    @JsonProperty("score")
    Integer getScore();

    @JsonProperty("click_num")
    Integer getClickNum();

    @JsonProperty("play_num")
    Integer getPlayNum();

    @JsonIgnore
    Integer getPass();

    @JsonIgnore
    Integer getBackToAdl();

    @JsonIgnore
    Integer getSupportUsage();

    @JsonProperty("pass")
    default Boolean isPass() {
        Integer pass = getPass();
        return pass != null && pass == 1;
    }

    @JsonProperty("back_to_adl")
    default Boolean isBackToAdl() {
        Integer backToAdl = getBackToAdl();
        return backToAdl != null && backToAdl == 1;
    }

    @JsonProperty("support_usage")
    default Boolean isSupportUsage() {
        Integer supportUsage = getSupportUsage();
        return supportUsage != null && supportUsage == 1;
    }

    @JsonProperty("support_usage_num")
    Integer getSUpportUsageNum();

    @JsonProperty("completion_after_support_time")
    String getCompletionAfterSupportTime();

    @JsonProperty("attempts_after_support_num")
    Integer getAttemptsAfterSupportNum();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
