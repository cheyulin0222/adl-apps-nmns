package com.arplanet.adlappnmns.res;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ProcessResult {
    private List<String> successDates;
    private List<String> failedDates;
}
