package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.repository.MatchRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchReader {

    private final MatchRepository matchRepository;


    public Match read(Long matchId) {
        return matchRepository.findById(matchId).orElseThrow(EntityNotFoundException::new);
    }

    public List<Match> readByBoolIsAnalysisFalse() {
        return matchRepository.findByBoolIsAnalysisFalse();
    }

    public boolean checkIfExistsByMatchApiId(String matchApiId) {
        return matchRepository.existsByMatchApiId(matchApiId);
    }

    public long readCountByMatchApiIds(List<String> matchApiIds) {
        return matchRepository.countByMatchApiIdIn(matchApiIds);
    }
}
