package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.arplanet.adlappnmns.dto.AssessmentLogDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service("assessmentLogService")
@RequiredArgsConstructor
@Slf4j
public class AssessmentLogService extends NmnsServiceBase<AssessmentLogDTO>{

    private final NmnsUserRepository nmnsUserRepository;
    private final Logger logger;

    @Override
    protected void validateData(AssessmentLogDTO data) {
        log.info("進入AssessmentLogService的validateData");

        Objects.requireNonNull(data.getAssessmentLogSn(), "assessment_log_sn 不可為 null");
        Objects.requireNonNull(data.getAssessmentSn(), "assessment_sn 不可為 null");
        Objects.requireNonNull(data.getItemSn(), "item_sn 不可為 null");
        Objects.requireNonNull(data.getUid(), "uid 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getStartTimestamp(), "start_timestamp 不可為 null");
        Objects.requireNonNull(data.getEndTimestamp(), "end_timestamp 不可為 null");
        Objects.requireNonNull(data.getDuration(), "duration 不可為 null");
        Objects.requireNonNull(data.getCorrectness(), "correctness 不可為 null");
        Objects.requireNonNull(data.getUserAnswer(), "user_answer 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    public List<AssessmentLogDTO> findByDate(String date, ProcessContext processContext) {
        log.info("進入AssessmentLogService的findByDate");
        try {
            List<LogBase<SessionInfoLogContext>> sessionInfoList = processContext.getSessionInfoList();

            List<Long> uidList = sessionInfoList.stream()
                    .filter(logBase -> !"game".equals(logBase.getContext().getUnitContentType()) && logBase.getContext().getIsChoose())
                    .map(logBase -> logBase.getContext().getUid())
                    .distinct()
                    .toList();

            Map<Long, Map<String, String>> userInfoMap  = nmnsUserRepository.findUserMapByUidIn(uidList);

            return sessionInfoList.stream()
                    .filter(logBase -> !"game".equals(logBase.getContext().getUnitContentType()) && logBase.getContext().getIsChoose())
                    .map(logBase -> {
                        Long uid = logBase.getContext().getUid();
                        Map<String, String> userInfo = userInfoMap.get(uid);

                        return AssessmentLogDTO.builder()
                                .assessmentLogSn(logBase.getLogSn())
                                .assessmentSn(logBase.getSessionId())
                                .itemSn(logBase.getContext().getQuestionId())
                                .uid(String.valueOf(uid))
                                .openidSub(userInfo != null ? userInfo.get("openidSub") : null)
                                .userId(userInfo != null ? userInfo.get("userId") : null)
                                .startTimestamp(logBase.getEventTimestamp())
                                .endTimestamp(logBase.getEventTimestamp())
                                .duration(0)
                                .correctness(String.valueOf(logBase.getContext().getCorrect()))
                                .userAnswer(logBase.getContext().getOptionId())
                                .creationTimestamp(logBase.getEventTimestamp())
                                .updateTimestamp(logBase.getEventTimestamp())
                                .build();
                    }).toList();
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
