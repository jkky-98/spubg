package com.jkky98.spubg.repository;

import com.jkky98.spubg.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByAccountId(String accountId);
}
