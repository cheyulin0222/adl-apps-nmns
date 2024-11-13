package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_user_quiz_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsUserQuizSession {

    @EmbeddedId
    private NmnsUserQuizSessionPK id;

    @Column(name="is_correct")
    private Boolean correct;

    @Column(name="created_at")
    private Timestamp createdAt;

    @Column(name="updated_at")
    private Timestamp updatedAt;

    @Column(name="started_at")
    private Timestamp startedAt;

    @Column(name="created_date")
    private String createdDate;
}
