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
public class MatchReader {

    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public Match read(Long matchId) {
        return matchRepository.findById(matchId).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Match> readByBoolIsAnalysisFalse() {
        return matchRepository.findByBoolIsAnalysisFalse();
    }

    @Transactional(readOnly = true)
    public boolean checkIfExistsByMatchApiId(String matchApiId) {
        return matchRepository.existsByMatchApiId(matchApiId);
    }

    @Transactional(readOnly = true)
    public long readCountByMatchApiIds(List<String> matchApiIds) {
        return matchRepository.countByMatchApiIdIn(matchApiIds);
    }
}
