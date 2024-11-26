package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ClassInfoDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserSchoolClassesRepository;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("classInfoService")
@Slf4j
@RequiredArgsConstructor
public class ClassInfoService extends NmnsServiceBase<ClassInfoDTO> {

    private final NmnsUserSchoolClassesRepository nmnsUserSchoolClassesRepository;
    private final Logger logger;

    @Override
    protected void validateData(ClassInfoDTO data) {
        if (data.getClassSn() == null) throw new NmnsServiceException("classSn 不可為 null");
        if (data.getUserId() == null) throw new NmnsServiceException("user_id 不可為 null");
        if (data.getOpenidSub() == null) throw new NmnsServiceException("openid_sub 不可為 null");
        if (data.getOrganizationId() == null) throw new NmnsServiceException("organizationId 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }

    @Override
    protected String getId(ClassInfoDTO data) {
        return String.valueOf(data.getClassSn());
    }

    @Override
    public List<ClassInfoDTO> findByDate(String date, ProcessContext processContext) {
        try {
            Timestamp start = ServiceUtil.getStartDate(date);
            Timestamp end = ServiceUtil.getEndDate(date);
            return nmnsUserSchoolClassesRepository.findClassInfo(start, end);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
