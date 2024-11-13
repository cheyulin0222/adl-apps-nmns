package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CitySerializer;
import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "class_sn",
        "user_id",
        "openid_sub",
        "city_id",
        "organization_id",
        "organization_name",
        "academic",
        "semester",
        "grade",
        "class",
        "class_num",
        "class_id",
        "creation_timestamp",
        "update_timestamp"
})
public interface ClassInfoDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("class_sn")
    Long getClassSn();

    @JsonProperty("user_id")
    String getUserId();

    @JsonProperty("openid_sub")
    String getOpenidSub();

    @JsonProperty("city_id")
    @JsonSerialize(using = CitySerializer.class)
    String getCityId();

    @JsonProperty("organization_id")
    String getOrganizationId();

    @JsonProperty("organization_name")
    String getOrganizationName();

    @JsonProperty("academic")
    String getAcademic();

    @JsonProperty("semester")
    String getSemester();

    @JsonProperty("grade")
    String getGrade();

    @JsonProperty("class")
    String getClassNo();

    @JsonProperty("class_num")
    String getClassNum();

    @JsonProperty("class_id")
    String getClassId();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
