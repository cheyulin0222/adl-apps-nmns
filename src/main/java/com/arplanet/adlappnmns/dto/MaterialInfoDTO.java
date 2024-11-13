package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "material_sn",
        "material_id",
        "material_exp_type",
        "material_groups",
        "material_content_type",
        "type",
        "name",
        "publisher",
        "subject",
        "node",
        "grade_stage",
        "core_competence",
        "path",
        "creation_timestamp",
        "update_timestamp"
})
public interface MaterialInfoDTO {

    @JsonProperty("material_sn")
    String getMaterialSn();

    @JsonProperty("material_id")
    String getMaterialId();

    @JsonProperty("material_exp_type")
    String getMaterialExpType();

    @JsonProperty("material_groups")
    String getMaterialGroups();

    @JsonProperty("material_content_type")
    String getMaterialContentType();

    String getType();

    String getName();

    String getPublisher();

    String getSubject();

    String getNode();

    @JsonProperty("grade_stage")
    String getGradeStage();

    @JsonProperty("core_competence")
    String getCoreCompetence();

    String getPath();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
