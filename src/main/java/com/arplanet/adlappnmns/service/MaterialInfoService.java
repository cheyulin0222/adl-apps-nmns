package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.MaterialInfoDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsContentUpdatedTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("materialInfoService")
@Slf4j
@RequiredArgsConstructor
public class MaterialInfoService extends NmnsServiceBase<MaterialInfoDTO> {

    private final NmnsContentUpdatedTimeRepository nmnsContentUpdatedTimeRepository;
    private final Logger logger;


    @Override
    protected void validateData(MaterialInfoDTO data) {
        if (data.getMaterialSn() == null) throw new NmnsServiceException("material_sn 不可為 null");
        if (data.getMaterialId() == null) throw new NmnsServiceException("material_id 不可為 null");
        if (data.getMaterialExpType() == null) throw new NmnsServiceException("material_exp_type 不可為 null");
        if (data.getMaterialGroups() == null) throw new NmnsServiceException("material_groups 不可為 null");
        if (data.getMaterialContentType() == null) throw new NmnsServiceException("material_content_type 不可為 null");
        if (data.getType() == null) throw new NmnsServiceException("type 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }

    @Override
    protected String getId(MaterialInfoDTO data) {
        return data.getMaterialSn();
    }


    @Override
    public List<MaterialInfoDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsContentUpdatedTimeRepository.findMaterialInfo(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
