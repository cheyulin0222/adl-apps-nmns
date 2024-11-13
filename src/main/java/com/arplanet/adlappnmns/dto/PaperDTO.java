package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "paper_sn",
        "item_sn",
        "material_sn",
        "subject",
        "node",
        "grade_stage",
        "core_competence",
        "full_score",
        "creation_timestamp",
        "update_timestamp"
})
public interface PaperDTO {

    @JsonProperty("paper_sn")
    String getPaperSn();

    @JsonProperty("item_sn")
    String getItemSn();

    @JsonProperty("material_sn")
    String getMaterialSn();

    String getSubject();

    String getNode();

    @JsonProperty("grade_stage")
    String getGradeStage();

    @JsonProperty("core_competence")
    String getCoreCompetence();

    @JsonProperty("full_score")
    Integer getFullScore();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();
}
