package com.example.springsecuritymultiauth.Member.controller.port;

import com.example.springsecuritymultiauth.Member.controller.dto.register.MemberRegisterRequestDto;
import com.example.springsecuritymultiauth.Member.domain.Member;

import java.util.Optional;

public interface MemberService {
    Member create(MemberRegisterRequestDto dto);
    Optional<Member> find(String username);
}
