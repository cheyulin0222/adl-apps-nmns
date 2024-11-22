package com.arplanet.adlappnmns.log;

import com.arplanet.adlappnmns.enums.ProcessType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Logger {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @Value("${git.commit.id.abbrev:UNKNOWN}")
    private String gitVersion;

    private final ObjectMapper objectMapper;
    private final LogContext logContext;
    private final ConfigurableListableBeanFactory beanFactory;

    public void info(String message) {
        log(LogLevel.INFO, message, new HashMap<>(0), new HashMap<>(0));
    }

    public void info(String message, Map<String, Object> payload) {
        log(LogLevel.INFO, message, payload, new HashMap<>(0));
    }

    public void info(String message, Map<String, Object> payload, Map<String, Object> logData) {
        log(LogLevel.INFO, message, payload, logData);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message, new HashMap<>(0), new HashMap<>(0));
    }

    public void error(String message, Throwable error) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", error.getMessage());
        errorData.put("stackTrace", Arrays.toString(error.getStackTrace()));
        log(LogLevel.ERROR, message, new HashMap<>(0), errorData);
    }

    public void error(String message, Map<String, Object> payload) {
        log(LogLevel.ERROR, message, payload, new HashMap<>(0));
    }

    public void error(String message, Throwable error, Map<String, Object> payload) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", error.getMessage());
        errorData.put("stackTrace", Arrays.toString(error.getStackTrace()));
        log(LogLevel.ERROR, message, payload, errorData);
    }

    public void error(String message, Throwable error, Map<String, Object> payload, Map<String, Object> logData) {
        logData.put("error", error.getMessage());
        logData.put("stackTrace", Arrays.toString(error.getStackTrace()));
        log(LogLevel.ERROR, message, payload, logData);
    }

    private void log(LogLevel level, String message, Map<String, Object> payload, Map<String, Object> logData) {
        try {
            LogMessage logMessage = LogMessage.builder()
                    .service("adl-apps-nmns")
                    .logLevel(level.name().toLowerCase())
                    .taskId(logContext.getTaskId())
                    .processType(getProcessType())
                    .taskDate(logContext.getCurrentDate())
                    .logMsg(message)
                    .payload(payload)
                    .logData(logData)
                    .stage(activeProfile)
                    .eventType(logContext.getEventType())
                    .git_ver(gitVersion)
                    .timestamp(new Timestamp(
                            LocalDateTime.now(ZoneId.of("Asia/Taipei"))
                                    .toInstant(ZoneOffset.ofHours(8))
                                    .toEpochMilli()
                    ))
                    .build();

            String jsonLog = objectMapper.writeValueAsString(logMessage);

            switch (level) {
                case INFO -> log.info("{} | {}", message, jsonLog);
                case ERROR -> log.error("{} | {}", message, jsonLog);
                default -> log.debug("{} | {}", message, jsonLog);
            }
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON log", e);
        }
    }

    private String getProcessType() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (var element : stackTrace) {
            String className = element.getClassName();
            if (!className.equals(getClass().getName()) && !className.contains("java.lang.Thread")) {
                try {
                    Class<?> callerClass = Class.forName(className);
                    String[] beanNames = beanFactory.getBeanNamesForType(callerClass);
                    if (beanNames.length > 0) {
                        try {
                            ProcessType processType = ProcessType.getByNmnsService(beanNames[0]);
                            return processType.getTypeName();
                        } catch (Exception e) {
                            return null;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
