package com.jkky98.spubg.service.implement;

import com.jkky98.spubg.domain.MatchWeaponDetail;
import com.jkky98.spubg.pubg.enums.WeaponName;
import com.jkky98.spubg.repository.MatchWeaponDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MatchWeaponDetailWriterTest {

    @Autowired
    MatchWeaponDetailWriter matchWeaponDetailWriter;

    @Autowired
    MatchWeaponDetailRepository matchWeaponDetailRepository;

    @Test
    @DisplayName("[MatchWeaponDetailWriter][saveAll] 성공 테스트")
    void testSaveAll() {
        // given
        MatchWeaponDetail detail1 = MatchWeaponDetail.builder()
                .weaponName(WeaponName.AKM)
                .damage(BigDecimal.valueOf(30L))
                .build();

        MatchWeaponDetail detail2 = MatchWeaponDetail.builder()
                .weaponName(WeaponName.AUG_A3)
                .damage(BigDecimal.valueOf(40L))
                .build();

        List<MatchWeaponDetail> inputList = List.of(detail1, detail2);

        // when
        List<MatchWeaponDetail> savedList = matchWeaponDetailWriter.saveAll(inputList);

        // then
        assertThat(savedList).hasSize(2);
        assertThat(savedList).allMatch(detail -> detail.getId() != null);
        assertThat(matchWeaponDetailRepository.findAll()).hasSize(2);
    }
}
