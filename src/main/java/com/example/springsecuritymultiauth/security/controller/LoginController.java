package com.example.springsecuritymultiauth.security.controller;

import com.example.springsecuritymultiauth.Member.controller.port.MemberService;
import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.security.dto.LoginResponseDto;
import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final MemberService memberService;
    private final MemberAuthenticationLevelRepository memberAuthenticationLevelRepository;

    @GetMapping("/login/success/{username}")
    public LoginResponseDto success(@PathVariable("username") String username) {
        Member member = memberService.find(username).get();
        Integer currentAuthenticationLevel = memberAuthenticationLevelRepository.findCurrentAuthenticationLevel(username).get();
        if(currentAuthenticationLevel == member.getRequiredAuthenticationLevel()) { // finish
            memberAuthenticationLevelRepository.deleteAuthenticationLevel(username);
            return new LoginResponseDto("END", currentAuthenticationLevel, currentAuthenticationLevel);
        } else {
            return new LoginResponseDto("CONTINUE", currentAuthenticationLevel, currentAuthenticationLevel+1);
        }
    }

    @GetMapping("/login/failure")
    public String fail() {
        return "fail";
    }
}
