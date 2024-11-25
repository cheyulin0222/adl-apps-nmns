package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.arplanet.adlappnmns.dto.AssessmentLogDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserRepository;
import com.arplanet.adlappnmns.utils.DataConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.arplanet.adlappnmns.enums.ErrorType.SERVICE;
import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;
import static com.arplanet.adlappnmns.service.GamePlayingLogService.GAME_PLAYING_LOG_UNIT_CONTENT_TYPE;


@Service("assessmentLogService")
@RequiredArgsConstructor
@Slf4j
public class AssessmentLogService extends NmnsServiceBase<AssessmentLogDTO>{

    private final NmnsUserRepository nmnsUserRepository;
    private final Logger logger;
    private final DataConverter dataConverter;

    @Override
    protected void validateData(AssessmentLogDTO data) {
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
        try {
            List<LogBase<SessionInfoLogContext>> sessionInfoList = processContext.getSessionInfoList();


            List<Long> uidList = sessionInfoList.stream()
                    .filter(this::validateLog)
                    .map(logBase -> logBase.getContext().getUid())
                    .distinct()
                    .toList();

            log.info("uidList.size={}", uidList.size());

            Map<Long, Map<String, String>> userInfoMap  = nmnsUserRepository.findUserMapByUidIn(uidList);

            log.info("成功拉到uid資料");

            return sessionInfoList.stream()
                    .filter(this::validateLog)
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
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }

    private boolean validateLog(LogBase<SessionInfoLogContext> logBase) {

        SessionInfoLogContext context = logBase.getContext();

        Map<String, Object> data = dataConverter.convertToMap(logBase);
        if (context == null) {
            logger.error("至S3取得的session.info，context資料為null", data, SERVICE);
            return false;
        }
        if (context.getIsChoose() == null) {
            logger.error("至S3取得的session.info，context.is_choose資料為null", data, SERVICE);
            return false;
        }
        if (context.getUnitContentType() == null) {
            logger.error("至S3取得的session.info，context.unit_content_type資料為null", data, SERVICE);
            return false;
        }

        return !GAME_PLAYING_LOG_UNIT_CONTENT_TYPE.equals(context.getUnitContentType())
                && context.getIsChoose();
    }
}
