package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "playing_time_sn",
        "uid",
        "openid_sub",
        "user_id",
        "game_sn",
        "duration",
        "start_timestamp",
        "end_timestamp",
        "rank",
        "creation_timestamp",
        "update_timestamp"
})
public interface GamePlayingTimeDTO {

    @JsonProperty("playing_time_sn")
    String getPlayingTimeSn();

    String getUid();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    @JsonProperty("user_id")
    String getUserId();

    @JsonProperty("game_sn")
    String getGameSn();

    Integer getDuration();

    @JsonProperty("start_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getStartTimestamp();

    @JsonProperty("end_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getEndTimestamp();

    @JsonProperty("rank")
    Integer getPlayerRank();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();

}
