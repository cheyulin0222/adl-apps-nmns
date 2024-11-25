package com.arplanet.adlappnmns.enums;

import lombok.Getter;

@Getter
public enum ErrorType {

    SYSTEM("system.error"),
    SERVICE("service.error");

    private final String errorType;

    ErrorType(String errorType) {
        this.errorType = errorType;
    }
}
