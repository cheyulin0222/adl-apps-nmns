package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "nmns_content_updated_time")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NmnsContentUpdatedTime {

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "updated_date")
    private String updatedDate;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
