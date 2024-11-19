package com.arplanet.adlappnmns.domain.nmns;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_user_game_rank_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsUserGameRankSession {

    @EmbeddedId
    private NmnsUserGameRankSessionPK id;

    @Column(name="unit_content_id")
    private String unitContentId;

    @Column(name="session_id")
    private String sessionId;

    private Integer rank;

    @Column(name="created_at")
    private Timestamp createdAt;

    @Column(name="updated_at")
    private Timestamp updatedAt;

    @Column(name="service_date")
    private String serviceDate;
}
