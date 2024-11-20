package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ClassInfoDTO;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserSchoolClassesRepository;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;


@Service("classInfoService")
@Slf4j
@RequiredArgsConstructor
public class ClassInfoService extends SimpleNmnsServiceBase<ClassInfoDTO> {

    private final NmnsUserSchoolClassesRepository nmnsUserSchoolClassesRepository;
    private final Logger logger;

    @Override
    protected void validateData(ClassInfoDTO data) {
        Objects.requireNonNull(data.getClassSn(), "classSn 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getOrganizationId(), "organizationId 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    protected String getId(ClassInfoDTO data) {
        return String.valueOf(data.getClassSn());
    }

    @Override
    public List<ClassInfoDTO> findByDate(String date) {
        try {
            Timestamp start = ServiceUtil.getStartDate(date);
            Timestamp end = ServiceUtil.getEndDate(date);
            return nmnsUserSchoolClassesRepository.findClassInfo(start, end);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
