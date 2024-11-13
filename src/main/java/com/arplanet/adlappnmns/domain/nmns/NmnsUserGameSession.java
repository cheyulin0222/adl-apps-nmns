package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_user_game_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsUserGameSession {

    @Id
    @Column(name = "room_id")
    private String roomId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "unit_content_id")
    private String unitContentId;

    @Column(name = "user_num")
    private Integer userNum;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "started_at")
    private Timestamp startedAt;

    @Column(name = "expired_at")
    private Timestamp expiredAt;

    @Column(name = "finished_at")
    private Timestamp finishedAt;

    @Column(name = "created_date")
    private String createdDate;
}
