package com.arplanet.adlappnmns.domain.nmns;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NmnsUserUnitContentSessionPK {

    private Long uid;

    @Column(name="session_id")
    private String sessionId;
}
