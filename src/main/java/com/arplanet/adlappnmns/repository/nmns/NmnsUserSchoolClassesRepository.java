package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsUserSchoolClasses;
import com.arplanet.adlappnmns.dto.ClassInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;


@Repository
public interface NmnsUserSchoolClassesRepository extends JpaRepository<NmnsUserSchoolClasses, Long> {

    @Query(nativeQuery = true, value =
        "SELECT " +
            "nusc.id as classSn, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.city, CHAR) as cityId, " +
            "CONVERT(ns.school_id, CHAR) as organizationId, " +
            "CONVERT(ns.school_name, CHAR) as organizationName, " +
            "CASE " +
            "    WHEN nusc.semester IS NULL THEN NULL " +
            "    WHEN LENGTH(nusc.semester) >= 3 THEN LEFT(nusc.semester, 3) " +
            "    ELSE NULL " +
            "END as academic, " +
            "CASE " +
            "    WHEN nusc.semester IS NULL THEN NULL " +
            "    WHEN LENGTH(nusc.semester) > 0 THEN RIGHT(nusc.semester, 1) " +
            "    ELSE NULL " +
            "END as semester, " +
            "CONVERT(nusc.grade, CHAR) as grade, " +
            "CONVERT(nusc.classno, CHAR) as classNo, " +
            "null as classNum, " +
            "null as classId, " +
            "nusc.created_at as creationTimestamp, " +
            "COALESCE(GREATEST(nusc.updated_at, nu.updated_at, ns.updated_at), nusc.updated_at) as updateTimestamp " +
        "FROM nmns_user_school_classes nusc " +
        "INNER JOIN nmns_user nu ON nusc.uid = nu.uid " +
        "INNER JOIN nmns_schools ns ON nusc.school_id = ns.school_id " +
        "WHERE nusc.updated_at BETWEEN :start AND :end " +
            "OR nu.updated_at BETWEEN :start AND :end " +
            "OR ns.updated_at BETWEEN :start AND :end ")
    List<ClassInfoDTO> findClassInfo(
        @Param("start") Timestamp start,
        @Param("end") Timestamp end
    );
}
