package com.arplanet.adlappnmns.enums;

import lombok.Getter;

@Getter
public enum EventType {

    SCHEDULER("scheduler.task"),
    MANUAL("manual.task");

    private final String typeName;

    EventType(String typeName) {
        this.typeName = typeName;
    }
}
