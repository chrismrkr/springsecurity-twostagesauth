package com.example.springsecuritymultiauth.security.handler;

import com.example.springsecuritymultiauth.Member.controller.port.MemberService;
import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import com.example.springsecuritymultiauth.token.UsernamePasswordMultiAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MultiStageAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final MemberAuthenticationLevelRepository memberAuthenticationLevelRepository;
    private final MemberService memberService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        int completedAuthenticationLevel = ((UsernamePasswordMultiAuthenticationToken) authentication).getAuthenticationProcessLevel();
        memberAuthenticationLevelRepository.updateAuthenticationLevel(username, completedAuthenticationLevel);
        response.sendRedirect(request.getContextPath() + "/login/success/" + username);
    }
}
