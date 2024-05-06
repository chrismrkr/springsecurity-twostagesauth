package com.example.springsecuritymultiauth.medium.security.repository;

import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class MemberAuthenticationLevelRedisRepositoryTest {
    @Autowired
    MemberAuthenticationLevelRepository memberAuthenticationLevelRepository;

    @Test
    void Member의_authenticationLevel을_시작() {
        // given
        Member member = Member.builder()
                .id(1L)
                .username("user1")
                .password("passwd1")
                .requiredAuthenticationLevel(2)
                .build();
        // when
        memberAuthenticationLevelRepository.initailizeAuthenticationLevel(member.getUsername());
        // then
        Optional<Integer> currentAuthenticationLevel = memberAuthenticationLevelRepository.findCurrentAuthenticationLevel(member.getUsername());
        Assertions.assertEquals(1, currentAuthenticationLevel.get());
    }

    @Test
    void Member의_authenticationLevel을_변경() {
        // given
        Member member = Member.builder()
                .id(2L)
                .username("user2")
                .password("passwd2")
                .requiredAuthenticationLevel(2)
                .build();
        memberAuthenticationLevelRepository.initailizeAuthenticationLevel(member.getUsername());
        // when
        memberAuthenticationLevelRepository.updateAuthenticationLevel(member.getUsername(), 2);
        // then
        Optional<Integer> currentAuthenticationLevel = memberAuthenticationLevelRepository.findCurrentAuthenticationLevel(member.getUsername());
        Assertions.assertEquals(2, currentAuthenticationLevel.get());
    }

    @Test
    void Member의_authenticationLevel_삭제() {
        // given
        Member member = Member.builder()
                .id(3L)
                .username("user3")
                .password("passwd3")
                .requiredAuthenticationLevel(2)
                .build();
        memberAuthenticationLevelRepository.initailizeAuthenticationLevel(member.getUsername());
        // when
        boolean b = memberAuthenticationLevelRepository.deleteAuthenticationLevel(member.getUsername());
        // then
        Optional<Integer> currentAuthenticationLevel = memberAuthenticationLevelRepository.findCurrentAuthenticationLevel(member.getUsername());
        Assertions.assertEquals(Optional.empty(), currentAuthenticationLevel);
    }

    @Test
    void 인증_과정_중이_아닌_Member_조회() {
        // given
        Member member = Member.builder()
                .id(4L)
                .username("user4")
                .password("passwd4")
                .requiredAuthenticationLevel(2)
                .build();
        // when
        Optional<Integer> currentAuthenticationLevel = memberAuthenticationLevelRepository.findCurrentAuthenticationLevel(member.getUsername());
        // then
        Assertions.assertEquals(Optional.empty(), currentAuthenticationLevel);
    }
}
