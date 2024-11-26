package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.GameDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserGameSessionRepository;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("gameService")
@Slf4j
@RequiredArgsConstructor
public class GameService extends NmnsServiceBase<GameDTO> {

    private final NmnsUserGameSessionRepository nmnsUserGameSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(GameDTO data) {
        if (data.getGameSn() == null) throw new NmnsServiceException("game_sn 不可為 null");
        if (data.getGameType() == null) throw new NmnsServiceException("game_type 不可為 null");
        if (data.getMaterialSn() == null) throw new NmnsServiceException("material_sn 不可為 null");
        if (data.getPassNum() == null) throw new NmnsServiceException("pass_num 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }

    @Override
    protected String getId(GameDTO data) {
        return data.getGameSn();
    }

    @Override
    public List<GameDTO> findByDate(String date, ProcessContext processContext) {
        try {
            Timestamp start = ServiceUtil.getStartDate(date);
            Timestamp end = ServiceUtil.getEndDate(date);
            return nmnsUserGameSessionRepository.findGame(start, end);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
