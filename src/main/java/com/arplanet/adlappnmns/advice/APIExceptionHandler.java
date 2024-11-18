package com.arplanet.adlappnmns.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler({ResponseStatusException.class, MissingRequestHeaderException.class})
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;  // 預設狀態碼
        String message;

        if (ex instanceof MissingRequestHeaderException) {
            message = "API key 不能為空";
        } else {
            // ResponseStatusException
            message = ((ResponseStatusException) ex).getReason();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("path", ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI());

        return ResponseEntity.status(status).body(response);
    }


}
