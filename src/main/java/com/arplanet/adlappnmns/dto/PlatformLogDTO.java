package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "platform_log_sn",
        "uid",
        "idp",
        "openid_sub",
        "user_id",
        "login_timestamp",
        "logout_timestamp",
        "session_time",
        "platforms_tracking_prior",
        "platforms_tracking_post",
        "creation_timestamp",
        "update_timestamp"
})
@Builder
@Data
public class PlatformLogDTO {

    @JsonProperty("platform_log_sn")
    private String platformLogSn;

    private String uid;

    private String idp;

    @JsonProperty("openid_sub")
    private String openidSub;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("login_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp loginTimestamp;

    @JsonProperty("logout_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp logoutTimestamp;

    @JsonProperty("session_time")
    private Integer sessionTIme;

    @JsonProperty("platforms_tracking_prior")
    private String platformsTrackingPrior;

    @JsonProperty("platforms_tracking_post")
    private String platformsTrackingPost;

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp creationTimestamp;

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp updateTimestamp;


}
