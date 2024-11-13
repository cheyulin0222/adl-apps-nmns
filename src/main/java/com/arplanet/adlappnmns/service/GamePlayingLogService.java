package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.dto.GamePlayingLogDTO;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserGameRankSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service("gamePlayingLogService")
@RequiredArgsConstructor
public class GamePlayingLogService extends NmnsServiceBase<GamePlayingLogDTO> {

    private final NmnsUserGameRankSessionRepository nmnsUserGameRankSessionRepository;
    private final Logger logger;

    @Override
    protected void validateData(GamePlayingLogDTO data) {
        Objects.requireNonNull(data.getLogSn(), "log_sn 不可為 null");
        Objects.requireNonNull(data.getUid(), "uid 不可為 null");
        Objects.requireNonNull(data.getOpenidSub(), "openid_sub 不可為 null");
        Objects.requireNonNull(data.getUserId(), "user_id 不可為 null");
        Objects.requireNonNull(data.getGameSn(), "game_sn 不可為 null");
        Objects.requireNonNull(data.getCorrectness(), "correctness 不可為 null");
        Objects.requireNonNull(data.getContent(), "content 不可為 null");
        Objects.requireNonNull(data.getScore(), "score 不可為 null");
        Objects.requireNonNull(data.getClickNum(), "click_num 不可為 null");
        Objects.requireNonNull(data.getPlayNum(), "play_num 不可為 null");
        Objects.requireNonNull(data.isPass(), "pass 不可為 null");
        Objects.requireNonNull(data.isBackToAdl(), "back_to_adl 不可為 null");
        Objects.requireNonNull(data.isSupportUsage(), "support_usage 不可為 null");
        Objects.requireNonNull(data.getSUpportUsageNum(), "support_usage_num 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    public List<GamePlayingLogDTO> findByDate(String date) {
        try {
            date = date.replace("-", "");
            return nmnsUserGameRankSessionRepository.findGamePlayingLog(date);
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e);
            throw new RuntimeException(e);
        }
    }
}
