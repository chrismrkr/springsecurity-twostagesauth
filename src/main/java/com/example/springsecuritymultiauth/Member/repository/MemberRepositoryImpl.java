package com.example.springsecuritymultiauth.Member.repository;

import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.Member.entity.MemberEntity;
import com.example.springsecuritymultiauth.Member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        MemberEntity memberEntity = member.toEntity();
        MemberEntity save = memberJpaRepository.save(memberEntity);
        return Member.from(save);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        Optional<MemberEntity> byUsername = memberJpaRepository.findByUsername(username);
        if(byUsername.isEmpty()) {
            return Optional.empty();
        }
        Optional<Member> member = byUsername.map(entity -> Member.from(entity));
        return member;
    }
}
