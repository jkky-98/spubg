package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchWriter {

    private final MatchRepository matchRepository;

    @Transactional
    public Match create(Match match) {
        return matchRepository.save(match);
    }

    @Transactional
    public List<Match> createAll(List<Match> matchs) {
        return matchRepository.saveAll(matchs);
    }
}
