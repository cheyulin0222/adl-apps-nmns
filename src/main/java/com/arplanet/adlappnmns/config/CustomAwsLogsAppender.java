package com.arplanet.adlappnmns.config;

import ca.pjer.logback.AwsLogsAppender;
import lombok.Data;
import lombok.EqualsAndHashCode;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomAwsLogsAppender extends AwsLogsAppender {

    private String accessKey;
    private String secretKey;


    @Override
    public void start() {

        setAccessKeyId(accessKey);
        setSecretAccessKey(secretKey);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
        String timeStamp = sdf.format(new Date());
        setLogStreamName("nmns-logs-" + timeStamp);

        super.start();
    }
}
