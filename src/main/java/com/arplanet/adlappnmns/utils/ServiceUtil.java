package com.arplanet.adlappnmns.utils;

import com.arplanet.adlappnmns.exception.NmnsServiceException;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServiceUtil {

    public static String DATE_PATTERN_WTTH_DASH = "yyyy-MM-dd";

    public static String APPLICATION_ZIP = "application/zip";
    public static String APPLICATION_JSON = "application/json";

    public static Integer calculateDifferenceInSeconds(Timestamp timestamp1, Timestamp timestamp2) {
        // 確保 timestamp1 不晚於 timestamp2
        if (timestamp1.after(timestamp2)) {
            Timestamp temp = timestamp1;
            timestamp1 = timestamp2;
            timestamp2 = temp;
        }

        // 計算差值
        Duration duration = Duration.between(timestamp1.toInstant(), timestamp2.toInstant());
        long differenceInSeconds = duration.getSeconds();

        // 檢查是否超出 Integer 範圍
        if (differenceInSeconds > Integer.MAX_VALUE) {
            throw new NmnsServiceException("時間差距過大無法用 Integer 表示");
        }

        // 轉換為 Integer 並返回
        return (int) differenceInSeconds;
    }

    public static Timestamp getStartDate(String dateStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN_WTTH_DASH);
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        // 設置開始時間為該日的 00:00:00
        LocalDateTime startOfDay = date.atStartOfDay();
        return Timestamp.valueOf(startOfDay);
    }

    public static Timestamp getEndDate(String dateStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN_WTTH_DASH);
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        return Timestamp.valueOf(endOfDay);
    }
}
