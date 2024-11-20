package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.GameDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserGameSessionRepository;
import com.arplanet.adlappnmns.utils.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;


@Service("gameService")
@Slf4j
@RequiredArgsConstructor
public class GameService extends NmnsServiceBase<GameDTO> {

    private final NmnsUserGameSessionRepository nmnsUserGameSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(GameDTO data) {
        Objects.requireNonNull(data.getGameSn(), "game_sn 不可為 null");
        Objects.requireNonNull(data.getGameType(), "game_type 不可為 null");
        Objects.requireNonNull(data.getMaterialSn(), "material_sn 不可為 null");
        Objects.requireNonNull(data.getPassNum(), "pass_num 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
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
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
