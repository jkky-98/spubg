package com.jkky98.spubg.discord.repository;

import com.jkky98.spubg.discord.domain.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StaticMapper {
    List<WeaponRanking> getTopWeaponRankings();
    List<HeadshotRanking> getHeadshotRankings();
    List<GrenadeRanking> getGrenadeRankings();
    List<LongDistanceRanking> getLongDistanceRankings();
    List<RidingRanking> getRidingRankings();
    List<LotOfFireRanking> getLotOfFireRankings();
    List<PhaseDealtRanking> getPhaseDealtRankings();
    List<ClutchDealtRanking> getClutchDealtRankings();
    List<GroggyRanking> getGroggyRankings();
    List<SmokeRanking> getSmokeRankings();
}
