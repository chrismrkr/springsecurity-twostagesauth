package com.example.springsecuritymultiauth.Member.controller;

import com.example.springsecuritymultiauth.Member.controller.dto.register.MemberRegisterRequestDto;
import com.example.springsecuritymultiauth.Member.controller.dto.register.MemberRegisterResponseDto;
import com.example.springsecuritymultiauth.Member.controller.port.MemberService;
import com.example.springsecuritymultiauth.Member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor

public class MemberController {
    private final MemberService memberService;

    @PostMapping("/member/register")
    public MemberRegisterResponseDto create(@RequestBody MemberRegisterRequestDto dto) {
        Member member = memberService.create(dto);
        return new MemberRegisterResponseDto("S", member.getUsername(), LocalDateTime.now());
    }

}
