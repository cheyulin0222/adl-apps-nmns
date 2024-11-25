package com.arplanet.adlappnmns.log;

import com.arplanet.adlappnmns.serializer.CustomTimestampSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

@Data
@Builder
public class LogMessage {

    private String service;

    @JsonProperty("log_level")
    private String logLevel;

    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("process_type")
    private String processType;

    @JsonProperty("task_date")
    private String taskDate;

    @JsonProperty("log_msg")
    private String logMsg;

    private Map<String, Object> payload;

    @JsonProperty("log_data")
    private Map<String, Object> logData;

    private String stage;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("error_type")
    private String errorType;

    @JsonProperty("git_ver")
    private String git_ver;

    @JsonSerialize(using = CustomTimestampSerializer.class)
    private Timestamp timestamp;


}
