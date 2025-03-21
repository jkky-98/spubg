package com.jkky98.spubg.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchWeaponDetail {

    @Id
    @GeneratedValue
    @Column(name = "match_w_d_id")
    private Long id;

    private String attackId;

    @Enumerated(EnumType.STRING)
    private WeaponName weaponName;

    @Enumerated(EnumType.STRING)
    private WeaponType weaponType;

    @Enumerated(EnumType.STRING)
    private DamageWhere damageWhere;

    @Column(precision = 10, scale = 1)
    private BigDecimal damage;

    // 3.18 추가 필드
    @Column(precision = 10, scale = 2)
    private BigDecimal attackerHealth;

    private boolean attackerIsInVehicle;

    private boolean groggy;

    // isGame
    @Column(precision = 10, scale = 2)
    private BigDecimal phase;

    // 데미지 이벤트 발생시 거리 측정
    @Column(precision = 10, scale = 2)
    private BigDecimal damDistance;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_mch_id")
    private MemberMatch memberMatch;
}
