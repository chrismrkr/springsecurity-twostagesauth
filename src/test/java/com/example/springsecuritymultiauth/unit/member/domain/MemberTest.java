package com.example.springsecuritymultiauth.unit.member.domain;

import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.Member.entity.MemberEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MemberTest {

    @Test
    void Member를_MemberEntity로_변경() {
        // given
        Member member = Member.builder()
                .username("user1")
                .password("passwd1")
                .requiredAuthenticationLevel(2)
                .build();
        // when
        MemberEntity entity= member.toEntity();
        // then
        Assertions.assertEquals(member.getUsername(), entity.getUsername());
        Assertions.assertEquals(member.getPassword(), entity.getPassword());
        Assertions.assertEquals(member.getRequiredAuthenticationLevel(), entity.getRequiredAuthenticationLevel());
    }

    @Test
    void MemberEntity를_Member로_변경() {
        // given
        MemberEntity entity = MemberEntity.builder()
                .username("user2")
                .password("passwd2")
                .requiredAuthenticationLevel(2)
                .build();
        // when
        Member member = Member.from(entity);
        // then
        Assertions.assertEquals(member.getUsername(), entity.getUsername());
        Assertions.assertEquals(member.getPassword(), entity.getPassword());
        Assertions.assertEquals(member.getRequiredAuthenticationLevel(), entity.getRequiredAuthenticationLevel());
    }
}
