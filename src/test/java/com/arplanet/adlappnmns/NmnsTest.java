package com.arplanet.adlappnmns;

import com.arplanet.adlappnmns.service.ScheduledTaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=dev"
})
@Slf4j
public class NmnsTest {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Test
    void test() {
        String date = "2024-09-24";
        List<String> dateList = new ArrayList<>();
        dateList.add(date);

        scheduledTaskService.processDates(dateList);
    }


    @Test
    void testLongTerm() {
        String sDate = "2023-11-01";
        String eDate = "2024-11-04";
        List<String> dateList = generateDateList(sDate, eDate);

        scheduledTaskService.processDates(dateList);
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
