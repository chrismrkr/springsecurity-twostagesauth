package com.example.springsecuritymultiauth.Member.service;

import com.example.springsecuritymultiauth.Member.controller.dto.register.MemberRegisterRequestDto;
import com.example.springsecuritymultiauth.Member.controller.port.MemberService;
import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.Member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public Member create(MemberRegisterRequestDto dto) {
        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .requiredAuthenticationLevel(2)
                .build();
        Member save = memberRepository.save(member);
        return save;
    }

    @Override
    public Optional<Member> find(String username) {
        Optional<Member> byUsername = memberRepository.findByUsername(username);
        return byUsername;
    }
}
