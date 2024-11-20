package com.arplanet.adlappnmns.dto;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.arplanet.adlappnmns.enums.ProcessType.SESSION_INFO_LOG;

@Data
@Builder
public class ProcessContext {
    private Map<String, List<?>> typeListMap;

    @SuppressWarnings("unchecked")
    public <T> List<T> getListByTypeName(String typeName) {
        return (List<T>) typeListMap.get(typeName);
    }

    public List<LogBase<SessionInfoLogContext>> getSessionInfoList() {
        return getListByTypeName(SESSION_INFO_LOG.name());
    }

}
