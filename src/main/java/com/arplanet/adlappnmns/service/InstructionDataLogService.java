package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.InstructionDataLogDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
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
        Objects.requireNonNull(data.getLogSn(), "log_sn 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getLogType(), "log_type 不可為 null");
        Objects.requireNonNull(data.getMaterialSn(), "material_sn 不可為 null");
        Objects.requireNonNull(data.getStartTimestamp(), "start_timestamp 不可為 null");
        Objects.requireNonNull(data.getExpiredTimestamp(), "expired_timestamp 不可為 null");
        Objects.requireNonNull(data.getDuration(), "duration 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
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
