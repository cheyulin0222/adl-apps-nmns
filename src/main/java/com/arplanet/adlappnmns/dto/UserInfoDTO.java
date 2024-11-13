package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "uid",
        "idp",
        "login_method",
        "user_id",
        "openid_sub",
        "name",
        "email",
        "identity",
        "city_sso_sub",
        "creation_timestamp",
        "update_timestamp"
})
public interface UserInfoDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    Long getUid();

    String getIdp();

    @JsonProperty("login_method")
    String getLoginMethod();

    @JsonProperty("user_id")
    String getUserId();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    String getName();

    @JsonProperty("email")
    String getEmail();

    @JsonProperty("identity")
    String getIdentity();

    @JsonProperty("city_sso_sub")
    String getCitySsoSub();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
