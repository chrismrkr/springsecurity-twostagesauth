package com.example.springsecuritymultiauth.security.provider;

import com.example.springsecuritymultiauth.Member.controller.port.MemberService;
import com.example.springsecuritymultiauth.Member.domain.Member;
import com.example.springsecuritymultiauth.token.UsernamePasswordMultiAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("secondStepAuthenticationProvider")
@RequiredArgsConstructor
public class SecondStepAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String authenticationCode = (String) authentication.getCredentials();
        int authenticationProcessLevel = ((UsernamePasswordMultiAuthenticationToken)authentication).getAuthenticationProcessLevel();

        if(!authenticationCode.equals("0000")) {
            throw new AuthenticationCredentialsNotFoundException("Authentication code not match");
        }

        UsernamePasswordMultiAuthenticationToken authenticationToken = new UsernamePasswordMultiAuthenticationToken(username, null, authenticationProcessLevel);
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordMultiAuthenticationToken.class);
    }
}
