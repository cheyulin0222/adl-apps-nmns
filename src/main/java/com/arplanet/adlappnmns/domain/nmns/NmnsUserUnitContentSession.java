package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_user_unit_content_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsUserUnitContentSession {

    @EmbeddedId
    private NmnsUserUnitContentSessionPK id;

    @Column(name="unit_content_id")
    private String unitContentId;

    @Column(name="tick_count")
    private Integer tickCount;

    @Column(name="player_time")
    private Integer playerTime;

    @Column(name="created_at")
    private Timestamp createdAt;

    @Column(name="updated_at")
    private Timestamp updatedAt;

    @Column(name="expired_at")
    private Timestamp expiredAt;

    @Column(name="finished_at")
    private Timestamp finishedAt;

    @Column(name="room_id")
    private String roomId;

    @Column(name="created_date")
    private String createdDate;
}
