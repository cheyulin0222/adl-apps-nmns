package com.arplanet.adlappnmns.log;

import com.arplanet.adlappnmns.enums.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;

@Component
@RequiredArgsConstructor
public class LogContext {

    private final Logger logger;

    private final ConcurrentHashMap<String, String> taskMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> eventTypeMap = new ConcurrentHashMap<>();
    private final ThreadLocal<String> currentDate = new ThreadLocal<>();

    public String getCurrentDate() {
        return currentDate.get();
    }

    public void setCurrentDate(String date) {
        currentDate.set(date);
    }

    public String getTaskId() {
        String date = currentDate.get();
        if (date == null) {
            logger.error("[getTaskId] Current date not set. Please call setCurrentDate() first.", SYSTEM);
            return null;
        }
        return taskMap.computeIfAbsent(date, d -> generateId("task-" + date.replace("-", "")));
    }

    public String getEventType() {
        String date = currentDate.get();
        if (date == null) {
            logger.error("[getEventType] Current date not set. Please call setCurrentDate() first.", SYSTEM);
            return null;
        }
        return eventTypeMap.get(date);
    }

    public void setEventType(EventType eventType) {
        String date = currentDate.get();
        if (date == null) {
            logger.error("[setEventType] Current date not set. Please call setCurrentDate() first.", SYSTEM);
            return;
        }
        eventTypeMap.put(date, eventType.getTypeName());
    }

    public void clearCurrentThread() {
        String date = currentDate.get();
        if (date != null) {
            taskMap.remove(date);
            currentDate.remove();
            eventTypeMap.remove(date);
        }
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }


}
