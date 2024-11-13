package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "item_sn",
        "material_sn",
        "item_type",
        "correct_answer",
        "level",
        "publisher",
        "subject",
        "node",
        "grade_stage",
        "core_competence",
        "creation_timestamp",
        "update_timestamp"
})
public interface ItemInfoDTO {

    @JsonProperty("item_sn")
    String getItemSn();

    @JsonProperty("material_sn")
    String getMaterialSn();

    @JsonProperty("item_type")
    String getItemType();

    @JsonProperty("correct_answer")
    String getCorrectAnswer();

    @JsonProperty("level")
    String getLevel();

    String getPublisher();

    String getSubject();

    String getNode();

    @JsonProperty("grade_stage")
    String getGradeStage();

    @JsonProperty("core_competence")
    String getCoreCompetence();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
