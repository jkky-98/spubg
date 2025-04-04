package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.MatchWeaponDetail;
import com.jkky98.spubg.repository.MatchWeaponDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class MatchWeaponDetailWriter {

    private final MatchWeaponDetailRepository matchWeaponDetailRepository;

    public List<MatchWeaponDetail> saveAll(List<MatchWeaponDetail> matchWeaponDetails) {
        return matchWeaponDetailRepository.saveAll(matchWeaponDetails);
    }
}
