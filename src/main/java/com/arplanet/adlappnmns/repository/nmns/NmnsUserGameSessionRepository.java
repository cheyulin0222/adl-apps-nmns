package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsUserGameSession;
import com.arplanet.adlappnmns.dto.GameDTO;
import com.arplanet.adlappnmns.dto.GamePlayingTimeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface NmnsUserGameSessionRepository extends JpaRepository<NmnsUserGameSession, String> {

    @Query(nativeQuery = true, value =
        "SELECT " +
            "CONVERT(nugs.room_id, CHAR) as gameSn, " +
            "'競賽關卡' as gameType, " +
            "CONVERT(ncuc.unit_content_id, CHAR) as materialSn, " +
            "CONVERT(ncuc.player_provider, CHAR) as publisher, " +
            "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_subject' THEN ncm.metrics_val END), '') as subject, " +
            "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_code' THEN ncm.metrics_val END), '') as node, " +
            "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'grade_stage' THEN ncm.metrics_val END), '') as gradeStage, " +
            "NULLIF(GROUP_CONCAT(DISTINCT CASE WHEN ncm.metrics_key = 'c108_competence' THEN ncm.metrics_val END), '') as coreCompetence, " +
            "null as ranking, " +
            "nugs.user_num as pass_num, " +
            "nugs.finished_at as endTimestamp, " +
            "nugs.created_at as creationTimestamp, " +
            "nugs.updated_at as updateTimestamp " +
        "FROM nmns_user_game_session nugs " +
        "INNER JOIN nmns_course_units_content ncuc ON nugs.unit_content_id = ncuc.unit_content_id " +
        "LEFT JOIN nmns_course_units ncu ON ncuc.unit_id = ncu.unit_id " +
        "LEFT JOIN nmns_content_metrics ncm ON ncu.content_id = ncm.content_id " +
        "WHERE nugs.updated_at BETWEEN :start AND :end " +
        "GROUP BY nugs.room_id")
    List<GameDTO> findGame(
        @Param("start") Timestamp start,
        @Param("end") Timestamp end
    );

    @Query(nativeQuery = true, value =
        "SELECT " +
            "CONVERT(nugrs.session_id, CHAR) as playingTimeSn, " +
            "CONVERT(nugrs.uid, CHAR) as uid, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "CONVERT(nugs.room_id, CHAR) as gameSn, " +
            "CAST(TIMESTAMPDIFF(SECOND, nugs.started_at, COALESCE(nugs.finished_at, nugs.started_at)) AS SIGNED) as duration, " +
            "nugs.started_at as startTimestamp, " +
            "COALESCE(nugs.finished_at, nugs.started_at) as endTimestamp, " +
            "nugrs.rank as playerRank, " +
            "nugs.created_at as creationTimestamp, " +
            "nugs.updated_at as updateTimestamp " +
        "FROM nmns_user_game_session nugs " +
        "INNER JOIN nmns_user_game_rank_session nugrs ON nugs.room_id = nugrs.room_id " +
        "INNER JOIN nmns_user nu ON nugrs.uid = nu.uid " +
        "WHERE nugs.created_date = :date " +
        "AND nugrs.unit_content_id IS NOT NULL " +
        "AND nugrs.session_id IS NOT NULL " +
        "AND nugs.started_at IS NOT NULL ")
    List<GamePlayingTimeDTO> findGamePlayingTime(
        @Param("date") String date
    );

}
