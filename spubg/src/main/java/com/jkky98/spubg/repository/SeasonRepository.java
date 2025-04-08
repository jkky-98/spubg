package com.jkky98.spubg.repository;

import com.jkky98.spubg.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    Optional<Season> findByBoolIsCurrentSeasonTrue();
}
