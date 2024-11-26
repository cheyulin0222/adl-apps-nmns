package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.InstructionDataLogDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserUnitContentSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Slf4j
@Service("instructionDataLogService")
@RequiredArgsConstructor
public class InstructionDataLogService extends NmnsServiceBase<InstructionDataLogDTO> {

    private final NmnsUserUnitContentSessionRepository nmnsUserUnitContentSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(InstructionDataLogDTO data) {
        if (data.getLogSn() == null) throw new NmnsServiceException("log_sn 不可為 null");
        if (data.getOpenidSub() == null) throw new NmnsServiceException("openid_sub 不可為 null");
        if (data.getUserId() == null) throw new NmnsServiceException("user_id 不可為 null");
        if (data.getLogType() == null) throw new NmnsServiceException("log_type 不可為 null");
        if (data.getMaterialSn() == null) throw new NmnsServiceException("material_sn 不可為 null");
        if (data.getStartTimestamp() == null) throw new NmnsServiceException("start_timestamp 不可為 null");
        if (data.getExpiredTimestamp() == null) throw new NmnsServiceException("expired_timestamp 不可為 null");
        if (data.getDuration() == null) throw new NmnsServiceException("duration 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }

    @Override
    public List<InstructionDataLogDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsUserUnitContentSessionRepository.findInstructionDataLog(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
