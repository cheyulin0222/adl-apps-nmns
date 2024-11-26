package com.arplanet.adlappnmns.log;

import com.arplanet.adlappnmns.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Component
@RequiredArgsConstructor
@Slf4j
public class LogContext {

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
            log.error("[getTaskId] Current date not set. Please call setCurrentDate() first.");
            return null;
        }
        return taskMap.computeIfAbsent(date, d -> generateId("task-" + date.replace("-", "")));
    }

    public String getEventType() {
        String date = currentDate.get();
        if (date == null) {
            log.error("[getEventType] Current date not set. Please call setCurrentDate() first.");
            return null;
        }
        return eventTypeMap.get(date);
    }

    public void setEventType(EventType eventType) {
        String date = currentDate.get();
        if (date == null) {
            log.error("[setEventType] Current date not set. Please call setCurrentDate() first.");
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
