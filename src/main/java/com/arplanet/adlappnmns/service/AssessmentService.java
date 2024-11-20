package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.AssessmentDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserUnitContentSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service("assessmentService")
@RequiredArgsConstructor
public class AssessmentService extends NmnsServiceBase<AssessmentDTO> {

    private final NmnsUserUnitContentSessionRepository nmnsUserUnitContentSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData (AssessmentDTO data) {
        Objects.requireNonNull(data.getAssessmentSn(), "assessment_sn 不可為 null");
        Objects.requireNonNull(data.getPaperSn(), "paper_sn 不可為 null");
        Objects.requireNonNull(data.getUid(), "uid 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getScore(), "score 不可為 null");
        Objects.requireNonNull(data.getDuration(), "duration 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    public List<AssessmentDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsUserUnitContentSessionRepository.findAssessment(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
