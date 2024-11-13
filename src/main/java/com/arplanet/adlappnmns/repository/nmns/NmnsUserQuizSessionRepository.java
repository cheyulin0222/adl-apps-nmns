package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsUserQuizSession;
import com.arplanet.adlappnmns.domain.nmns.NmnsUserQuizSessionPK;
import com.arplanet.adlappnmns.dto.AseessmentLogDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NmnsUserQuizSessionRepository extends JpaRepository<NmnsUserQuizSession, NmnsUserQuizSessionPK> {

    @Query(nativeQuery = true, value =
        "SELECT " +
            "CONCAT('log-', CONVERT(nuqs.uid, CHAR), '-', CONVERT(nuqs.created_date, CHAR), '-', UUID()) as assessmentLogSn, " +
            "CONVERT(nuqs.session_id, CHAR) as assessmentSn, " +
            "CONVERT(nuqs.question_id, CHAR) as itemSn, " +
            "CONVERT(nuqs.uid, CHAR) as uid, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "COALESCE(nuqs.started_at, nuqs.created_at) as startTimestamp, " +
            "nuqs.created_at as endTimestamp, " +
            "CAST(TIMESTAMPDIFF(SECOND, COALESCE(nuqs.started_at, nuqs.created_at), nuqs.created_at) AS UNSIGNED) as duration, " +
            "CASE WHEN nuqs.is_correct = 0 THEN 'false' WHEN nuqs.is_correct = 1 THEN 'true' ELSE NULL END as correctness, " +
            "CONVERT(nuqs.option_id, CHAR) as userAnswer, " +
            "nuqs.created_at as creationTimestamp, " +
            "nuqs.updated_at as updateTimestamp " +
        "FROM nmns_user_quiz_session nuqs " +
        "INNER JOIN nmns_user nu ON nuqs.uid = nu.uid " +
        "WHERE nuqs.created_date = :date ")
    List<AseessmentLogDTO> findAssessmentLog(
            @Param("date") String date
    );
}
