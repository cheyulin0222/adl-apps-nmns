package com.arplanet.adlappnmns.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.time.Instant;
import java.util.UUID;

public class CustomAwsLayout extends LayoutBase<ILoggingEvent> {


    @Override
    public String doLayout(ILoggingEvent event) {
        String timestamp = Instant.now().toString();
        String requestId = UUID.randomUUID().toString();
        String level = event.getLevel().toString();
        String message = event.getFormattedMessage();

        // 確保 message 是有效的 JSON
        if (message == null || message.trim().isEmpty()) {
            message = "{}";
        }

        return String.format("%s %s %s %s%n",
                timestamp,
                requestId,
                level,
                message);
    }
}
