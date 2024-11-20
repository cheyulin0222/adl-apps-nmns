package com.arplanet.adlappnmns.service.s3;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service("sessionInfoLogService")
public class SessionInfoLogService extends NmnsS3ServiceBase<LogBase<SessionInfoLogContext>, SessionInfoLogContext> {

    @Override
    protected List<LogBase<SessionInfoLogContext>> getData(Map<String, List<LogBase<SessionInfoLogContext>>> logBaseGroup, String date) {
        return logBaseGroup.getOrDefault("quiz_answer", Collections.emptyList());
    }

    @Override
    protected TypeReference<LogBase<SessionInfoLogContext>> getLogBaseTypeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    protected String getGroupingKey(LogBase<SessionInfoLogContext> logBase) {
        return logBase.getActionType();
    }



    @Override
    protected String generateServicePath() {
        return processType.getTypeName();
    }

    @Override
    protected void validateData(LogBase<SessionInfoLogContext> data) {

    }
}
