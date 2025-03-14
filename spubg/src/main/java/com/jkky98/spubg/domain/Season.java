package com.jkky98.spubg.domain;

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
public class Season {

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
