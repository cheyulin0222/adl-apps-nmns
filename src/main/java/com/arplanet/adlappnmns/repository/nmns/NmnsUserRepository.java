package com.arplanet.adlappnmns.repository.nmns;

import com.arplanet.adlappnmns.domain.nmns.NmnsUser;
import com.arplanet.adlappnmns.dto.UserInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public interface NmnsUserRepository extends JpaRepository<NmnsUser, Long> {

    @Query(nativeQuery = true, value =
        "SELECT " +
            "nu.uid as uid, " +
            "CONVERT(nu.idp, CHAR) as idp, " +
            "CONVERT(nu.login_method, CHAR) as loginMethod, " +
            "CONVERT(nu.user_id, CHAR) as userId, " +
            "CONVERT(nu.sub, CHAR) as openidSub, " +
            "CONVERT(nu.name, CHAR) as name, " +
            "CONVERT(nu.email, CHAR) as email, " +
            "NULLIF(GROUP_CONCAT(DISTINCT CONVERT(nust.title, CHAR) SEPARATOR ','), '') as identity, " +
            "null as citySsoSub, " +
            "nu.created_at as creationTimestamp, " +
            "GREATEST(nu.updated_at, MAX(nust.updated_at)) as updateTimestamp " +
        "FROM nmns_user nu " +
        "LEFT JOIN nmns_user_school_titles nust ON nu.uid = nust.uid " +
        "WHERE nu.updated_at BETWEEN :start AND :end " +
            "OR nust.updated_at BETWEEN :start AND :end " +
        "GROUP BY nu.uid")
    List<UserInfoDTO> findUserInfo(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query(value = "SELECT " +
            "uid as `key`, " +
            "JSON_OBJECT(" +
                "'userId', user_id, " +
                "'openidSub', sub" +
            ") as value " +
            "FROM nmns_user " +
            "WHERE uid IN :uidList",
            nativeQuery = true)
    Map<Long, Map<String, String>> findUserMapByUidIn(@Param("uidList") List<Long> uidList);
}
