package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
public class ProcessContext {
    private final List<LogBase<SessionInfoLogContext>> sessionInfoList;
}
