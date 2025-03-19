package com.jkky98.spubg.discord.repository;

import com.jkky98.spubg.domain.Member;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MemberMapper {
    List<com.jkky98.spubg.discord.domain.Member> findAll();

    @Select("SELECT * FROM member WHERE username = #{username}")
    Member findByUsername(String username);

    @Update("UPDATE member SET discord_name = #{discordName} WHERE username = #{username}")
    void update(Member member);
}
