package com.arplanet.adlappnmns.domain.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionInfoLogContext {

    @JsonProperty("course_id")
    private String courseId;

    @JsonProperty("group_key")
    private String groupKey;

    @JsonProperty("unit_exp_type")
    private String unitExpType;

    @JsonProperty("unit_content_id")
    private String unitContentId;

    @JsonProperty("unit_content_type")
    private String unitContentType;

    @JsonProperty("quiz_id")
    private String quizId;

    @JsonProperty("player_duration")
    private Integer playerDuration;

    @JsonProperty("player_type")
    private String playerType;

    @JsonProperty("player_url")
    private String playerUrl;

    @JsonProperty("player_provider")
    private String playerProvider;

    @JsonProperty("content_id_unit_content")
    private Long contentIdUnitContent;

    @JsonProperty("content_id_unit")
    private Long contentIdUnit;

    @JsonProperty("content_id_course")
    private Long contentIdCourse;

    @JsonProperty("course_title")
    private String courseTitle;

    @JsonProperty("unit_title")
    private String unitTitle;

    @JsonProperty("uid")
    private Long uid;

    @JsonProperty("room_id")
    private String roomId;

    @JsonProperty("question_id")
    private String questionId;

    @JsonProperty("question_title")
    private String questionTitle;

    @JsonProperty("question_type")
    private String questionType;

    @JsonProperty("question_detail")
    private String questionDetail;

    @JsonProperty("trigger_event")
    private String triggerEvent;

    @JsonProperty("check_time")
    private Integer checkTime;

    @JsonProperty("option_id")
    private String optionId;

    @JsonProperty("option_title")
    private String optionTitle;

    @JsonProperty("is_choose")
    private Boolean isChoose;

    @JsonProperty("option_is_correct")
    private Boolean optionIsCorrect;

    @JsonProperty("is_correct")
    private Boolean isCorrect;


}
