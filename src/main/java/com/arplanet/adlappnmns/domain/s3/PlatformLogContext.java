package com.arplanet.adlappnmns.domain.s3;

import com.arplanet.adlappnmns.deserializer.UnixTimestampDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PlatformLogContext {

    private Boolean success;

    @JsonProperty("user_id")
    private String userId;

    private String idp;

    private String sub;

    @JsonProperty("openid_sub")
    private String openidSub;

    @JsonProperty("platforms_tracking_prior")
    private String platformsTrackingPrior;

    @JsonProperty("platforms_tracking_post")
    private String platformsTrackingPost;

    @JsonProperty("expired_at")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    private Timestamp expiredTimestamp;
}
