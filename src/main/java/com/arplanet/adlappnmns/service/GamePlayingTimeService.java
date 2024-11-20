package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.GamePlayingTimeDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserGameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service("gamePlayingTimeService")
@RequiredArgsConstructor
public class GamePlayingTimeService extends NmnsServiceBase<GamePlayingTimeDTO> {

    private final NmnsUserGameSessionRepository nmnsUserGameSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(GamePlayingTimeDTO data) {
        Objects.requireNonNull(data.getPlayingTimeSn(), "playing_time_sn 不可為 null");
        Objects.requireNonNull(data.getUid(), "uid 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getGameSn(), "game_sn 不可為 null");
        Objects.requireNonNull(data.getDuration(), "duration 不可為 null");
        Objects.requireNonNull(data.getStartTimestamp(), "start_timestamp 不可為 null");
        Objects.requireNonNull(data.getEndTimestamp(), "end_timestamp 不可為 null");
        Objects.requireNonNull(data.getPlayerRank(), "rank 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    public List<GamePlayingTimeDTO> findByDate(String date, ProcessContext processContext) {
        try {
            date = date.replace("-", "");
            return nmnsUserGameSessionRepository.findGamePlayingTime(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
