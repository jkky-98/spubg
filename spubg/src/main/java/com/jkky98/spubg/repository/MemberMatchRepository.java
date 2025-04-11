package com.jkky98.spubg.repository;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.domain.Member;
import com.jkky98.spubg.domain.MemberMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberMatchRepository extends JpaRepository<MemberMatch, Long> {

    @Query("select m from MemberMatch m where m.match.boolIsAnalysis = true AND m.match.gameMode = com.jkky98.spubg.pubg.enums.GameMode.SQUAD AND m.boolIsAnalysis = false")
    List<MemberMatch> findByMatchIsAnalyzedAndSquad();

    Optional<MemberMatch> findByMemberAndMatch(Member member, Match match);
}
