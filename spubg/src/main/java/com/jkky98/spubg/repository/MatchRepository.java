package com.jkky98.spubg.repository;

import com.jkky98.spubg.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByBoolIsAnalysisFalse();
    Match findByMatchApiId(String matchApiId);
}
