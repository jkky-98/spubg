package com.jkky98.spubg.repository;

import com.jkky98.spubg.domain.MatchWeaponDetail;
import com.jkky98.spubg.domain.MemberMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchWeaponDetailRepository extends JpaRepository<MatchWeaponDetail, Long> {

}
