package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_user_school_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsUserSchoolClasses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long uid;

    @Column(name = "school_id")
    private String schoolId;

    private String grade;

    @Column(name = "classno")
    private String classNo;

    private String semester;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
