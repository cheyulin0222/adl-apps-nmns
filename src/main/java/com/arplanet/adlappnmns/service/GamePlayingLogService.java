package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.arplanet.adlappnmns.dto.GamePlayingLogDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("gamePlayingLogService")
@RequiredArgsConstructor
public class GamePlayingLogService extends NmnsServiceBase<GamePlayingLogDTO> {

    public static final String GAME_PLAYING_LOG_UNIT_CONTENT_TYPE = "game";

    private final NmnsUserRepository nmnsUserRepository;
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
        Objects.requireNonNull(data.getPass(), "pass 不可為 null");
        Objects.requireNonNull(data.getBackToAdl(), "back_to_adl 不可為 null");
        Objects.requireNonNull(data.getSupportUsage(), "support_usage 不可為 null");
        Objects.requireNonNull(data.getSupportUsageNum(), "support_usage_num 不可為 null");
        Objects.requireNonNull(data.getCreationTimestamp(), "creation_timestamp 不可為 null");
        Objects.requireNonNull(data.getUpdateTimestamp(), "update_timestamp 不可為 null");
    }

    @Override
    public List<GamePlayingLogDTO> findByDate(String date, ProcessContext processContext) {
        try {
            List<LogBase<SessionInfoLogContext>> sessionInfoList = processContext.getSessionInfoList();

            List<Long> uidList = sessionInfoList.stream()
                    .filter(logBase -> "game".equals(logBase.getContext().getUnitContentType()))
                    .map(logBase -> logBase.getContext().getUid())
                    .distinct()
                    .toList();

            Map<Long, Map<String, String>> userInfoMap  = nmnsUserRepository.findUserMapByUidIn(uidList);

            return sessionInfoList.stream()
                    .filter(logBase -> "game".equals(logBase.getContext().getUnitContentType()))
                    .map(logBase -> {
                        Long uid = logBase.getContext().getUid();
                        Map<String, String> userInfo = userInfoMap.get(uid);

                        return GamePlayingLogDTO.builder()
                                .logSn(logBase.getLogSn())
                                .uid(String.valueOf(uid))
                                .openidSub(userInfo != null ? userInfo.get("openidSub") : null)
                                .userId(userInfo != null ? userInfo.get("userId") : null)
                                .gameSn(logBase.getContext().getRoomId())
                                .correctness(String.valueOf(logBase.getContext().getCorrect()))
                                .content(logBase.getContext().getQuestionTitle())
                                .reward(null)
                                .score(0)
                                .clickNum(0)
                                .playNum(0)
                                .pass(true)
                                .backToAdl(false)
                                .supportUsage(false)
                                .supportUsageNum(0)
                                .completionAfterSupportTime(null)
                                .attemptsAfterSupportNum(null)
                                .creationTimestamp(logBase.getEventTimestamp())
                                .updateTimestamp(logBase.getEventTimestamp())
                                .build();
                    }).toList();
        } catch (Exception e) {
            logger.error("至資料庫取得資料失敗", e, SYSTEM);
            throw new RuntimeException(e);
        }
    }
}
