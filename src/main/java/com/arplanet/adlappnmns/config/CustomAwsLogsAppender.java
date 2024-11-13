package com.arplanet.adlappnmns.config;

import ca.pjer.logback.AwsLogsAppender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CustomAwsLogsAppender extends AwsLogsAppender {

    @Override
    public void start() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
        String timeStamp = sdf.format(new Date());
        setLogStreamName("app-logs-" + timeStamp);
        super.start();
    }
}
