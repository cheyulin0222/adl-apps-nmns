package com.arplanet.adlappnmns.service.s3;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.PlatformLogContext;
import com.arplanet.adlappnmns.dto.PlatformLogDTO;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.arplanet.adlappnmns.enums.ErrorType.SERVICE;
import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;

@Service("platformLogService")
public class PlatformLogService extends NmnsS3ServiceBase<PlatformLogDTO, PlatformLogContext> {

    @Override
    protected List<PlatformLogDTO> getData(List<LogBase<PlatformLogContext>> logBaseList, String date) {

        // 依照session_id組成Map
        Map<String, List<LogBase<PlatformLogContext>>> logBaseGroup = logBaseList.parallelStream()
                .collect(Collectors.groupingBy(logBase -> {
                    logContext.setCurrentDate(date);
                    return getGroupingKey(logBase);
                }));


        return logBaseGroup.entrySet().parallelStream()
                .map(entry -> {
                    logContext.setCurrentDate(date);
                    return processGroup(entry);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    @Override
    protected String generateServicePath() {
        return "edu." + processType.getTypeName();
    }


    protected PlatformLogDTO processGroup(Map.Entry<String, List<LogBase<PlatformLogContext>>> entry) {
        List<LogBase<PlatformLogContext>> logs = new ArrayList<>();
        try {
            logs = entry.getValue();

            if (logs.size() == 2 ) {
                LogBase<PlatformLogContext> login;
                LogBase<PlatformLogContext> logout;

                login = findLogByActionType(logs, "login");
                logout = findLogByActionType(logs, "logout");


                // 排除Postman沒有值的狀況
                if (login.getContext().getPlatformsTrackingPrior() == null) {
                    return null;
                }

                return buildPlatformLogDTO(login, logout.getActionTimestampMs());
            } else if (logs.size() == 1 && "login".equals(logs.get(0).getActionType())) {
                LogBase<PlatformLogContext> login = logs.get(0);

                // 排除Postman沒有值的狀況
                if (login.getContext().getPlatformsTrackingPrior() == null) {
                    return null;
                }

                Timestamp expiredTimestamp = login.getContext().getExpiredTimestamp();

                return buildPlatformLogDTO(login, expiredTimestamp);
            } else if (logs.size() > 2) {
                throw new NmnsServiceException("platform_log 資料筆數超過 2 筆");
            }

            return null;
        } catch (NmnsServiceException e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("sessionId", entry.getKey());
            payload.put("logsSize", logs.size());
            payload.put("logs", logs);
            logger.error(e.getMessage(), payload, SERVICE);
            return null;
        } catch (Exception e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("sessionId", entry.getKey());
            payload.put("logsSize", logs.size());
            payload.put("logs", logs);
            logger.error("處理platform_Log時發生錯誤", e, payload, SYSTEM);
            throw e;
        }
    }

    private PlatformLogDTO buildPlatformLogDTO(LogBase<PlatformLogContext> login, Timestamp logoutTimestamp) {

        validate(login, logoutTimestamp);

        return PlatformLogDTO.builder()
                    .platformLogSn(login.getSessionId())
                    .uid(login.getUid())
                    .idp(login.getContext().getIdp())
                    .openidSub(login.getContext().getSub())
                    .userId(login.getContext().getUserId())
                    .loginTimestamp(login.getActionTimestampMs())
                    .logoutTimestamp(logoutTimestamp)
                    .sessionTIme(ServiceUtil.calculateDifferenceInSeconds(logoutTimestamp, login.getActionTimestampMs()))
                    .platformsTrackingPrior(login.getContext().getPlatformsTrackingPrior())
                    .platformsTrackingPost(login.getContext().getPlatformsTrackingPost())
                    .creationTimestamp(login.getActionTimestampMs())
                    .updateTimestamp(logoutTimestamp)
                    .build();
    }

    private void validate(LogBase<PlatformLogContext> login, Timestamp logoutTimestamp) {
        if (login.getSessionId() == null) throw new NmnsServiceException("platform_log_sn 不可為 null");
        if (login.getUid() == null) throw new NmnsServiceException("uid 不可為 null");
        if (login.getContext() == null) throw new NmnsServiceException("context 不可為 null");
        if (login.getContext().getIdp() == null) throw new NmnsServiceException("idp 不可為 null");
        if (login.getContext().getSub() == null) throw new NmnsServiceException("openidSub 不可為 null");
        if (login.getContext().getUserId() == null) throw new NmnsServiceException("userId 不可為 null");
        if (login.getActionTimestampMs() == null) throw new NmnsServiceException("loginTimestamp 不可為 null");
        if (logoutTimestamp == null) throw new NmnsServiceException("logoutTimestamp 不可為 null");
        if (login.getContext().getPlatformsTrackingPrior() == null) throw new NmnsServiceException("platformsTrackingPrior 不可為 null");
        if (login.getContext().getPlatformsTrackingPost() == null) throw new NmnsServiceException("platformsTrackingPost 不可為 null");
    }

    @Override
    protected TypeReference<LogBase<PlatformLogContext>> getLogBaseTypeReference() {
        return new TypeReference<>() {
        };
    }



    private String getGroupingKey(LogBase<PlatformLogContext> logBase) {
        return logBase.getSessionId();
    }

    private LogBase<PlatformLogContext> findLogByActionType(List<LogBase<PlatformLogContext>> logs, String actionType) {
        return logs.stream()
                // 新加了欄位success，為了兼容新舊資料
                // 排除sucess 為 true 或 null 皆可
                .filter(logBase -> actionType.equals(logBase.getActionType())
                        && !Boolean.FALSE.equals(logBase.getContext().getSuccess()))
                .findFirst()
                .orElseThrow(() -> new NmnsServiceException("Platform_log 找不到 " + actionType + " 資料"));
    }

    @Override
    protected void validateData(PlatformLogDTO data) {}


}
