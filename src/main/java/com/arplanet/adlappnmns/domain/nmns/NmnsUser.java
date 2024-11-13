package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    private String idp;

    private String sub;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "user_account")
    private String userAccount;

    @Column(name = "openid_sub")
    private String openidSub;

    private String name;

    private String picture;

    @Column(name = "login_method")
    private String loginMethod;

    private String email;

    @Column(name = "workspace_id")
    private String workspaceId;

    private String city;

    @Column(name = "is_active")
    private boolean active;

    private Long spoint;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    @Column(name = "last_activity_at")
    private Timestamp lastActivityAt;

}
