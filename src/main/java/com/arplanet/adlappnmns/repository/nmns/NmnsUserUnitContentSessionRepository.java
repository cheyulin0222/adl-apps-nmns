package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsUserUnitContentSession;
import com.arplanet.adlappnmns.domain.nmns.NmnsUserUnitContentSessionPK;
import com.arplanet.adlappnmns.dto.AssessmentDTO;
import com.arplanet.adlappnmns.dto.InstructionDataLogDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NmnsUserUnitContentSessionRepository extends JpaRepository<NmnsUserUnitContentSession, NmnsUserUnitContentSessionPK> {

    @Query(nativeQuery = true, value =
        "SELECT " +
            "CONVERT(nsucs.session_id, CHAR) as logSn, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "'play_content' as logType, " +
            "CONVERT(nsucs.unit_content_id, CHAR) as materialSn, " +
            "nsucs.created_at as startTimestamp, " +
            "nsucs.finished_at as endTimestamp, " +
            "nsucs.expired_at as expiredTimestamp, " +
            "CAST(TIMESTAMPDIFF(SECOND, nsucs.created_at, COALESCE(nsucs.finished_at, nsucs.updated_at)) AS SIGNED) as duration, " +
            "nsucs.created_at as creationTimestamp, " +
            "nsucs.updated_at as updateTimestamp " +
        "FROM nmns_user_unit_content_session nsucs " +
        "INNER JOIN nmns_user nu ON nsucs.uid = nu.uid " +
        "WHERE nsucs.service_date = :date ")
    List<InstructionDataLogDTO> findInstructionDataLog(
        @Param("date") String date
    );

    @Query(nativeQuery = true, value =
        "SELECT " +
            "CONVERT(nsucs.session_id, CHAR) as assessmentSn, " +
            "CONVERT(nuqs.quiz_id, CHAR) as paperSn, " +
            "CONVERT(nsucs.uid, CHAR) as uid, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "ROUND(SUM(CASE WHEN nuqs.is_correct = 1 THEN 1 ELSE 0 END) / COUNT(*) * 100) as score, " +
            "CAST(TIMESTAMPDIFF(SECOND, nsucs.created_at, nsucs.updated_at) AS UNSIGNED) as duration, " +
            "nsucs.created_at as creationTimestamp, " +
            "nsucs.updated_at as updateTimestamp " +
        "FROM nmns_user_unit_content_session nsucs " +
        "INNER JOIN nmns_user_quiz_session nuqs ON nsucs.session_id = nuqs.session_id " +
        "INNER JOIN nmns_user nu ON nuqs.uid = nu.uid " +
        "WHERE nsucs.service_date = :date " +
        "GROUP BY nsucs.session_id ")
    List<AssessmentDTO> findAssessment(
            @Param("date") String date
    );

}
