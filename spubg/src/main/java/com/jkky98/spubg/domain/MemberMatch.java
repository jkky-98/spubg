package com.jkky98.spubg.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMatch {

    @Id
    @GeneratedValue
    @Column(name = "m_mch_id")
    private Long id;

    private boolean boolIsAnalysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @OneToMany(mappedBy = "memberMatch")
    @Builder.Default
    private List<MatchWeaponDetail> matchWeaponDetails = new ArrayList<>();
}
