package com.jkky98.spubg.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String accountId;

    private String clanId;

    private String banType;

    private String username;

    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<MemberMatch> memberMatches = new ArrayList<>();
}
