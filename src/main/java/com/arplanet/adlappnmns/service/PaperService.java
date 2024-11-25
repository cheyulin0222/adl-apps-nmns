package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.PaperDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsContentUpdatedTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("paperService")
@Slf4j
@RequiredArgsConstructor
public class PaperService extends NmnsServiceBase<PaperDTO> {

    private final NmnsContentUpdatedTimeRepository nmnsContentUpdatedTimeRepository;
    private final Logger logger;



    @Override
    protected void validateData(PaperDTO data) {
        Objects.requireNonNull(data.getPaperSn(), "paper_sn 不可為 null");
        Objects.requireNonNull(data.getMaterialSn(), "material_sn 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    protected String getId(PaperDTO data) {
        return data.getPaperSn();
    }


    @Override
    public List<PaperDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsContentUpdatedTimeRepository.findPaper(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
