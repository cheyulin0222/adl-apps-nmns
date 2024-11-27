package com.arplanet.adlappnmns.service;

import com.arplanet.adlappnmns.domain.s3.LogBase;
import com.arplanet.adlappnmns.domain.s3.SessionInfoLogContext;
import com.arplanet.adlappnmns.dto.GamePlayingLogDTO;
import com.arplanet.adlappnmns.dto.ProcessContext;
import com.arplanet.adlappnmns.exception.NmnsServiceException;
import com.arplanet.adlappnmns.log.Logger;
import com.arplanet.adlappnmns.repository.nmns.NmnsUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.arplanet.adlappnmns.enums.ErrorType.SYSTEM;


@Service("gamePlayingLogService")
@RequiredArgsConstructor
public class GamePlayingLogService extends NmnsServiceBase<GamePlayingLogDTO> {

    public static final String GAME_PLAYING_LOG_UNIT_CONTENT_TYPE = "game";

    private final NmnsUserRepository nmnsUserRepository;
    private final Logger logger;

    @Override
    protected void validateData(GamePlayingLogDTO data) {
        if (data.getLogSn() == null) throw new NmnsServiceException("log_sn 不可為 null");
        if (data.getUid() == null) throw new NmnsServiceException("uid 不可為 null");
        if (data.getUserId() == null) throw new NmnsServiceException("user_id 不可為 null");
        if (data.getGameSn() == null) throw new NmnsServiceException("game_sn 不可為 null");
        if (data.getCorrectness() == null) throw new NmnsServiceException("correctness 不可為 null");
        if (data.getContent() == null) throw new NmnsServiceException("content 不可為 null");
        if (data.getScore() == null) throw new NmnsServiceException("score 不可為 null");
        if (data.getClickNum() == null) throw new NmnsServiceException("click_num 不可為 null");
        if (data.getPlayNum() == null) throw new NmnsServiceException("play_num 不可為 null");
        if (data.getPass() == null) throw new NmnsServiceException("pass 不可為 null");
        if (data.getBackToAdl() == null) throw new NmnsServiceException("back_to_adl 不可為 null");
        if (data.getSupportUsage() == null) throw new NmnsServiceException("support_usage 不可為 null");
        if (data.getSupportUsageNum() == null) throw new NmnsServiceException("support_usage_num 不可為 null");
        if (data.getCreationTimestamp() == null) throw new NmnsServiceException("creation_timestamp 不可為 null");
        if (data.getUpdateTimestamp() == null) throw new NmnsServiceException("update_timestamp 不可為 null");
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
