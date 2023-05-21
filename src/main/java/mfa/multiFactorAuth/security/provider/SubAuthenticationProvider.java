package mfa.multiFactorAuth.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;


@Slf4j
@RequiredArgsConstructor
public class SubAuthenticationProvider implements AuthenticationProvider {
    private final SecurityContextUtils securityContextUtils;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        /** 사용자가 입력한 pin 번호와
         *  실제 인증 코드가 동일한지 확인한다.
         */

        MfaAuthenticationToken authenticationToken = (MfaAuthenticationToken)securityContextUtils.getAuthentication();

        String inputPin = (String)authentication.getCredentials();
        if(!inputPin.equals(authenticationToken.getPin())) {
            throw new BadCredentialsException("pin Not Match Exception");
        }

        authenticationToken.increaseAuthLevel();
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
