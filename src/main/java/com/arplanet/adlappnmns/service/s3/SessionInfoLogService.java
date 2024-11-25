package com.arplanet.adlappnmns.service.s3;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("sessionInfoLogService")
@Slf4j
public class SessionInfoLogService extends NmnsS3ServiceBase<LogBase<SessionInfoLogContext>, SessionInfoLogContext> {

    public static final String SESSION_LOG_ACTION_TYPE = "quiz.answer";

    @Override
    protected List<LogBase<SessionInfoLogContext>> getData(List<LogBase<SessionInfoLogContext>> logBaseList, String date) {
        return logBaseList.stream()
                .filter(logBase -> SESSION_LOG_ACTION_TYPE.equals(logBase.getActionType()))
                .collect(Collectors.toList());
    }

    @Override
    protected TypeReference<LogBase<SessionInfoLogContext>> getLogBaseTypeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    protected String generateServicePath() {
        return processType.getTypeName();
    }

    @Override
    protected void validateData(LogBase<SessionInfoLogContext> data) {
    }
}
