package com.arplanet.adlappnmns.controller;

import com.arplanet.adlappnmns.req.DateRange;
import com.arplanet.adlappnmns.res.ProcessResult;
import com.arplanet.adlappnmns.service.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/task")
@RequiredArgsConstructor
public class NmnsServiceController {

    private final ScheduledTaskService scheduledTaskService;

    @PostMapping("/execute/dates")
    public ResponseEntity<String> executeByDates(@RequestBody List<String> dates) {
        if (dates == null || dates.isEmpty()) {
            return ResponseEntity.badRequest().body("請提供至少一個日期");
        }

        ProcessResult processResult = scheduledTaskService.processDates(dates);
        if (processResult.getFailedDates().isEmpty()) {
            return ResponseEntity.ok("任務成功執行，處理的日期: " + String.join(", ", dates));
        } else {
            return ResponseEntity.ok(
                    String.format("任務部分失敗，成功日期: %s，失敗日期: %s",
                            String.join(", ", processResult.getSuccessDates()),
                            String.join(", ", processResult.getFailedDates())
                    )
            );
        }
    }

    @PostMapping("/execute/range")
    public ResponseEntity<String> executeByRange(@RequestBody DateRange dateRange) {
        List<String> dates = generateDateList(dateRange.getStartDate(), dateRange.getEndDate());


        ProcessResult processResult = scheduledTaskService.processDates(dates);

        if (processResult.getFailedDates().isEmpty()) {
            return ResponseEntity.ok("任務成功執行，處理的日期: " + String.join(", ", dates));
        } else {
            return ResponseEntity.ok(
                    String.format("任務部分失敗，成功日期: %s，失敗日期: %s",
                            String.join(", ", processResult.getSuccessDates()),
                            String.join(", ", processResult.getFailedDates())
                    )
            );
        }
    }

    private List<String> generateDateList(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        List<String> dateList = new ArrayList<>();
        while (!start.isAfter(end)) {
            dateList.add(start.format(formatter));
            start = start.plusDays(1);
        }
        return dateList;
    }
}
