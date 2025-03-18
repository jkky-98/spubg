package com.jkky98.spubg.repository;

import com.jkky98.spubg.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByBoolIsAnalysisFalse();
    Match findByMatchApiId(String matchApiId);
    boolean existsByMatchApiId(String matchApiId);
    @Query("SELECT COUNT(m) FROM Match m WHERE m.matchApiId IN :matchApiIds")
    long countByMatchApiIdIn(@Param("matchApiIds") List<String> matchApiIds);

}
