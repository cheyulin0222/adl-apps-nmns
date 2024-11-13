package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.AseessmentLogDTO;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserQuizSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service("assessmentLogService")
@RequiredArgsConstructor
public class AssessmentLogService extends NmnsServiceBase<AseessmentLogDTO>{

    private final NmnsUserQuizSessionRepository nmnsUserQuizSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(AseessmentLogDTO data) {
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
    public List<AseessmentLogDTO> findByDate(String date) {
        try {
            date = date.replace("-", "");
            return nmnsUserQuizSessionRepository.findAssessmentLog(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
