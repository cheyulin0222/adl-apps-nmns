package com.arplanet.adlappnmns.service.s3;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.PlatformLogContext;
import com.arplanet.adlappnmns.dto.PlatformLogDTO;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service("platformLogService")
public class PlatformLogService extends NmnsS3ServiceBase<PlatformLogDTO, PlatformLogContext> {

    @Override
    protected List<PlatformLogDTO> getData(Map<String, List<LogBase<PlatformLogContext>>> logBaseGroup, String date) {

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

            if (logs.size() > 1) {
                LogBase<PlatformLogContext> login = findLogByActionType(logs, "login");
                LogBase<PlatformLogContext> logout = findLogByActionType(logs, "logout");

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
            }
            return null;
        } catch (Exception e) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("sessionId", entry.getKey());
            payload.put("logsSize", logs.size());
            payload.put("logs", logs);
            logger.error("處理platform_Log時發生錯誤", e, payload);
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
        Objects.requireNonNull(login.getSessionId(), "platform_log_sn 不可為 null");
        Objects.requireNonNull(login.getUid(), "uid 不可為 null");
        Objects.requireNonNull(login.getContext(), "context 不可為 null");
        Objects.requireNonNull(login.getContext().getIdp(), "idp 不可為 null");
        Objects.requireNonNull(login.getContext().getSub(), "openidSub 不可為 null");
        Objects.requireNonNull(login.getContext().getUserId(), "userId 不可為 null");
        Objects.requireNonNull(login.getActionTimestampMs(), "loginTimestamp 不可為 null");
        Objects.requireNonNull(logoutTimestamp, "logoutTimestamp 不可為 null");
        Objects.requireNonNull(login.getContext().getPlatformsTrackingPrior(), "platformsTrackingPrior 不可為 null");
        Objects.requireNonNull(login.getContext().getPlatformsTrackingPost(), "platformsTrackingPost 不可為 null");
    }

    @Override
    protected TypeReference<LogBase<PlatformLogContext>> getLogBaseTypeReference() {
        return new TypeReference<>() {
        };
    }

    @Override
    protected String getGroupingKey(LogBase<PlatformLogContext> logBase) {
        return logBase.getSessionId();
    }

    private LogBase<PlatformLogContext> findLogByActionType(List<LogBase<PlatformLogContext>> logs, String actionType) {
        return logs.stream()
                .filter(logBase -> actionType.equals(logBase.getActionType()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No " + actionType + " event found"));
    }

    @Override
    protected void validateData(PlatformLogDTO data) {}
}
