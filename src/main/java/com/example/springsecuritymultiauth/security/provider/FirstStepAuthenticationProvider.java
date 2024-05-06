package com.example.springsecuritymultiauth.security.provider;

import com.example.springsecuritymultiauth.Member.controller.port.MemberService;
import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.token.UsernamePasswordMultiAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("firstStepAuthenticationProvider")
@RequiredArgsConstructor
public class FirstStepAuthenticationProvider implements AuthenticationProvider {
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final MemberService memberService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        int authenticationProcessLevel = ((UsernamePasswordMultiAuthenticationToken)authentication).getAuthenticationProcessLevel();

        Optional<Member> member = memberService.find(username);
        if(member.isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Username not found");
        }
        if(!passwordEncoder.matches(password, member.get().getPassword())) {
            throw new AuthenticationCredentialsNotFoundException("Password not match");
        }
        UsernamePasswordMultiAuthenticationToken authenticationToken = new UsernamePasswordMultiAuthenticationToken(username, null, authenticationProcessLevel);
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordMultiAuthenticationToken.class);
    }
}
