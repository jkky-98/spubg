package com.jkky98.spubg.discord.repository;

import com.jkky98.spubg.discord.domain.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StaticMapper {
    List<WeaponRanking> getTopWeaponRankings(@Param("memberIds") List<Long> memberIds);
    List<HeadshotRanking> getHeadshotRankings();
    List<GrenadeRanking> getGrenadeRankings();
    List<LongDistanceRanking> getLongDistanceRankings();
    List<RidingRanking> getRidingRankings();
    List<LotOfFireRanking> getLotOfFireRankings();
    List<PhaseDealtRanking> getPhaseDealtRankings();
    List<ClutchDealtRanking> getClutchDealtRankings();
}
