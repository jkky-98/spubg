package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.Season;
import com.jkky98.spubg.repository.SeasonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonReader {
    private final SeasonRepository seasonRepository;

    public Season readCurrentSeason() {
        return seasonRepository.findByBoolIsCurrentSeasonTrue().orElseThrow(EntityNotFoundException::new);
    }
}
