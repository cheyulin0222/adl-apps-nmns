package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.ItemInfoDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsContentUpdatedTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("itemInfoService")
@Slf4j
@RequiredArgsConstructor
public class ItemInfoService extends NmnsServiceBase<ItemInfoDTO> {

    private final NmnsContentUpdatedTimeRepository nmnsContentUpdatedTimeRepository;
    private final Logger logger;

    @Override
    protected void validateData(ItemInfoDTO data) {
        if (data.getItemSn() == null) throw new NmnsServiceException("item_sn 不可為 null");
        if (data.getMaterialSn() == null) throw new NmnsServiceException("material_sn 不可為 null");
        if (data.getItemType() == null) throw new NmnsServiceException("item_type 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }


    @Override
    public String getId(ItemInfoDTO data) {
        return data.getItemSn();
    }


    @Override
    public List<ItemInfoDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsContentUpdatedTimeRepository.findItemInfo(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
