package com.jkky98.spubg.domain;

import com.jkky98.spubg.domain.base.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Season extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "season_id")
    private Long id;

    private String seasonApiId;
    private boolean boolIsCurrentSeason;
    private boolean boolIsOffseason;

    public void updateBoolIsCurrentSeason() {
        boolIsCurrentSeason = !boolIsCurrentSeason;
    }
}
