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
public class NmnsUserGameRankSessionPK {

    private Long uid;

    @Column(name="room_id")
    private String roomId;
}
