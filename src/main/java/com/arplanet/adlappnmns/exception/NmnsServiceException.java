package com.arplanet.adlappnmns.exception;

public class NmnsServiceException extends RuntimeException{

    private final String errorMessage;

    public NmnsServiceException(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
