package com.example.springsecuritymultiauth.unit.member.service;

import com.example.springsecuritymultiauth.Member.controller.dto.register.MemberRegisterRequestDto;
import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.Member.entity.MemberEntity;
import com.example.springsecuritymultiauth.Member.service.MemberServiceImpl;
import com.example.springsecuritymultiauth.Member.service.port.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemberServiceTest {
    MemberServiceImpl memberService = new MemberServiceImpl(new MockMemberRepository());

    @Test
    void Member를_저장() {
        // given
        MemberRegisterRequestDto dto = new MemberRegisterRequestDto("user1", "passwd1");
        // when
        Member member = memberService.create(dto);
        // then
        Assertions.assertEquals(dto.getPassword(), member.getPassword());
        Assertions.assertEquals(dto.getUsername(), member.getUsername());
    }

    @Test
    void Member를_조회() {
        // given
        MemberRegisterRequestDto dto = new MemberRegisterRequestDto("user2", "passwd2");
        Member member = memberService.create(dto);
        // when
        Member member1 = memberService.find("user2").get();
        // then
        Assertions.assertEquals(member1.getPassword(), member.getPassword());
        Assertions.assertEquals(member1.getUsername(), member.getUsername());
    }

    static class MockMemberRepository implements MemberRepository {
        List<MemberEntity> datas;
        AtomicLong idGenerator = new AtomicLong(1L);
        public MockMemberRepository() {
            this.datas = new ArrayList<>();
        }
        @Override
        public Member save(Member member) {
            MemberEntity entity = member.toEntity();
            datas.add(entity);
            return Member.from(entity);
        }

        @Override
        public Optional<Member> findByUsername(String username) {
            Optional<MemberEntity> any = datas.stream()
                    .filter(memberEntity -> memberEntity.getUsername().equals(username))
                    .findAny();
            Optional<Member> member = any.map(entity -> Member.from(entity));
            return member;
        }
    }
}
