package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.MaterialInfoDTO;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsContentUpdatedTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;



@Service("materialInfoService")
@Slf4j
@RequiredArgsConstructor
public class MaterialInfoService extends NmnsServiceBase<MaterialInfoDTO> {

    private final NmnsContentUpdatedTimeRepository nmnsContentUpdatedTimeRepository;
    private final Logger logger;


    @Override
    protected void validateData(MaterialInfoDTO data) {
        Objects.requireNonNull(data.getMaterialSn(), "material_sn 不可為 null");
        Objects.requireNonNull(data.getMaterialId(), "material_id 不可為 null");
        Objects.requireNonNull(data.getMaterialExpType(), "material_exp_type 不可為 null");
        Objects.requireNonNull(data.getMaterialGroups(), "material_groups 不可為 null");
        Objects.requireNonNull(data.getMaterialContentType(), "material_content_type 不可為 null");
        Objects.requireNonNull(data.getType(), "type 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    protected String getId(MaterialInfoDTO data) {
        return data.getMaterialSn();
    }


    @Override
    public List<MaterialInfoDTO> findByDate(String date) {
        try {
            date = date.replace("-", "");
            return nmnsContentUpdatedTimeRepository.findMaterialInfo(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
