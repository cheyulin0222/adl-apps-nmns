package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsUserGameRankSession;
import com.arplanet.adlappnmns.domain.nmns.NmnsUserGameRankSessionPK;
import com.arplanet.adlappnmns.dto.GamePlayingLogDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NmnsUserGameRankSessionRepository extends JpaRepository<NmnsUserGameRankSession, NmnsUserGameRankSessionPK> {

    @Query(nativeQuery = true, value =
        "SELECT " +
            "CONCAT('log-', CONVERT(nugrs.uid, CHAR), '-', DATE_FORMAT(nugrs.created_at, '%Y%m%d'), '-', UUID()) as logSn, " +
            "CONVERT(nugrs.uid, CHAR) as uid, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "CONVERT(nugrs.room_id, CHAR) as gameSn, " +
            "CASE WHEN nuqs.is_correct = 0 THEN 'false' WHEN nuqs.is_correct = 1 THEN 'true' ELSE NULL END as correctness, " +
            "CONVERT(nci_title.info_val, CHAR) as content, " +
            "null as reward, " +
            "0 as score, " +
            "0 as clickNum, " +
            "0 as playNum, " +
            "1 as pass, " +
            "0 as backToAdl, " +
            "0 as supportUsage, " +
            "0 as supportUsageNum, " +
            "null as completionAfterSupportTime, " +
            "null as attemptsAfterSupportNum, " +
            "nugrs.created_at as creationTimestamp, " +
            "nugrs.updated_at as updateTimestamp " +
        "FROM nmns_user_game_rank_session nugrs " +
        "INNER JOIN nmns_user nu ON nugrs.uid = nu.uid " +
        "INNER JOIN nmns_user_quiz_session nuqs ON nugrs.session_id = nuqs.session_id " +
        "INNER JOIN nmns_quiz_questions nqq ON nuqs.question_id = nqq.question_id " +
        "INNER JOIN nmns_content_info nci_title ON nqq.content_id = nci_title.content_id AND nci_title.info_key = 'title' " +
        "WHERE nugrs.created_date = :date " +
        "AND nugrs.unit_content_id IS NOT NULL ")
    List<GamePlayingLogDTO> findGamePlayingLog(
        @Param("date") String date
    );
}
