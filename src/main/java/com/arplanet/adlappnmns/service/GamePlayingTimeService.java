package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.GamePlayingTimeDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserGameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Slf4j
@Service("gamePlayingTimeService")
@RequiredArgsConstructor
public class GamePlayingTimeService extends NmnsServiceBase<GamePlayingTimeDTO> {

    private final NmnsUserGameSessionRepository nmnsUserGameSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(GamePlayingTimeDTO data) {
        if (data.getPlayingTimeSn() == null) throw new NmnsServiceException("playing_time_sn 不可為 null");
        if (data.getUid() == null) throw new NmnsServiceException("uid 不可為 null");
        if (data.getUserId() == null) throw new NmnsServiceException("user_id 不可為 null");
        if (data.getGameSn() == null) throw new NmnsServiceException("game_sn 不可為 null");
        if (data.getDuration() == null) throw new NmnsServiceException("duration 不可為 null");
        if (data.getStartTimestamp() == null) throw new NmnsServiceException("start_timestamp 不可為 null");
        if (data.getEndTimestamp() == null) throw new NmnsServiceException("end_timestamp 不可為 null");
        if (data.getPlayerRank() == null) throw new NmnsServiceException("rank 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
    }

    @Override
    public List<GamePlayingTimeDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsUserGameSessionRepository.findGamePlayingTime(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
