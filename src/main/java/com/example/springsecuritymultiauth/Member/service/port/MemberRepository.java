package com.example.springsecuritymultiauth.Member.service.port;

import com.example.springsecuritymultiauth.Member.domain.Member;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findByUsername(String username);
}
