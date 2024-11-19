package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.enums.EventType;
import com.arplanet.adlappnmns.log.LogContext;
import com.arplanet.adlappnmns.res.ProcessResult;
import com.arplanet.adlappnmns.service.facade.NmnsServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private static final String ERROR_TAG = "[NMNS_PROCESS_FAILED]";
    private final NmnsServiceFacade nmnsServiceFacade;
    private final LogContext logContext;

    // 排程
    @Scheduled(cron = "0 59 10 * * ?", zone = "Asia/Taipei")
    public void performTask() {
        String date = ZonedDateTime.now(ZoneId.of("Asia/Taipei"))
                .minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try {
            logContext.setCurrentDate(date);
            logContext.setEventType(EventType.SCHEDULER);
            nmnsServiceFacade.process(date);
        } catch (Exception e) {
            log.error("{} 排程處理失敗，日期: {}", ERROR_TAG, date, e);
        } finally {
            logContext.clearCurrentThread();
        }
    }

    // 手動觸發
    public ProcessResult processDates(List<String> dates) {
        List<String> successDates = Collections.synchronizedList(new ArrayList<>());
        List<String> failedDates = Collections.synchronizedList(new ArrayList<>());

        dates.parallelStream().forEach(date -> {
            try {
                logContext.setCurrentDate(date);
                logContext.setEventType(EventType.MANUAL);

                nmnsServiceFacade.process(date);

                successDates.add(date);
            } catch (Exception e) {
                failedDates.add(date);
                log.error("手動處理失敗，日期: {}", date, e);
            } finally {
                logContext.clearCurrentThread();
            }
        });

        return ProcessResult.builder()
                .successDates(successDates)
                .failedDates(failedDates)
                .build();
    }

}
