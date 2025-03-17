package com.jkky98.spubg.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Match {

    /**
     * 직접 지정 필요
     */
    @Id
    @GeneratedValue
    @Column(name = "match_id")
    private Long id;

    private String matchApiId;

    private String map;

    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    private String assetId;

    private String assetUrl;

    private LocalDateTime createdAt;

    private String season;

    @OneToMany(mappedBy = "match")
    @Builder.Default
    private List<MemberMatch> memberMatches = new ArrayList<>();

    private boolean boolIsAnalysis;
}
