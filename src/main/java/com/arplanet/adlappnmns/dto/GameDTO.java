package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;

@JsonPropertyOrder({
        "game_sn",
        "game_type",
        "material_sn",
        "publisher",
        "subject",
        "node",
        "grade_stage",
        "core_competence",
        "ranking",
        "pass_num",
        "end_timestamp",
        "creation_timestamp",
        "update_timestamp"
})
public interface GameDTO {

    @JsonProperty("game_sn")
    String getGameSn();

    @JsonProperty("game_type")
    String getGameType();

    @JsonProperty("material_sn")
    String getMaterialSn();

    String getPublisher();

    String getSubject();

    String getNode();

    @JsonProperty("grade_stage")
    String getGradeStage();

    @JsonProperty("core_competence")
    String getCoreCompetence();

    @JsonProperty("ranking")
    String getRanking();

    @JsonProperty("pass_num")
    Integer getPassNum();

    @JsonProperty("end_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getEndTimestamp();

    @JsonProperty("creation_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getCreationTimestamp();

    @JsonProperty("update_timestamp")
    @JsonSerialize(using = CustomTimestampSerializer.class)
    Timestamp getUpdateTimestamp();


}
