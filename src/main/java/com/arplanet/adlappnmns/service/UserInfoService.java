package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.UserInfoDTO;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserRepository;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service("userInfoService")
@Slf4j
@RequiredArgsConstructor
public class UserInfoService extends NmnsServiceBase<UserInfoDTO> {

    private final NmnsUserRepository nmnsUserRepository;
    private final Logger logger;

    @Override
    protected void validateData(UserInfoDTO data) {
        Objects.requireNonNull(data.getUid(), "uid 不可為 null");
        Objects.requireNonNull(data.getIdp(), "idp 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    protected String getId(UserInfoDTO data) {
        return String.valueOf(data.getUid());
    }

    @Override
    public List<UserInfoDTO> findByDate(String date) {
        try {
            Timestamp start = ServiceUtil.getStartDate(date);
            Timestamp end = ServiceUtil.getEndDate(date);
            return nmnsUserRepository.findUserInfo(start, end);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
