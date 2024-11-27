package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.AssessmentDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserUnitContentSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Slf4j
@Service("assessmentService")
@RequiredArgsConstructor
public class AssessmentService extends NmnsServiceBase<AssessmentDTO> {

    private final NmnsUserUnitContentSessionRepository nmnsUserUnitContentSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData (AssessmentDTO data) {
        if (data.getAssessmentSn() == null) throw new NmnsServiceException("assessment_sn 不可為 null");
        if (data.getPaperSn() == null) throw new NmnsServiceException("paper_sn 不可為 null");
        if (data.getUid() == null) throw new NmnsServiceException("uid 不可為 null");
        if (data.getUserId() == null) throw new NmnsServiceException("user_id 不可為 null");
        if (data.getScore() == null) throw new NmnsServiceException("score 不可為 null");
        if (data.getDuration() == null) throw new NmnsServiceException("duration 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }

    @Override
    public List<AssessmentDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsUserUnitContentSessionRepository.findAssessment(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
