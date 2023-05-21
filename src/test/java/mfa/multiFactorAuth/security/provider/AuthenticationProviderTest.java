package mfa.multiFactorAuth.security.provider;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationProviderTest {
    @Mock
    private SecurityContextUtils securityContextUtils;

    @Test
    @DisplayName("1차 인증 성공 test")
    void firstStageAuthSuccessTest() {

    }


    @Test
    @DisplayName("2차 인증 성공 test")
    void secondStageAuthSuccessTest(@Mock Authentication authentication) throws Exception {
        /** 시나리오
         * 조건 1. SecurityContext에 1차 인증까지 완료된 MfaAuthenticationToken 저장된 상태
         *          (pin = "123456" 저장됨)
         * 조건 2. Authentication 객체를 통해 사용자가 입력한 pin 값이 들어온다.
         */
        // given
        MfaAuthenticationToken storedAuthenticationToken = new MfaAuthenticationToken("user", "1111", null, 1);
        storedAuthenticationToken.setPin("123456");

        BDDMockito.given(securityContextUtils.getAuthentication()).willReturn(storedAuthenticationToken);// 저장된 pin
        BDDMockito.given((String)authentication.getCredentials()).willReturn("123456"); // 입력된 pin
        // when
        SubAuthenticationProvider subAuthenticationProvider = new SubAuthenticationProvider(securityContextUtils);
        MfaAuthenticationToken resultToken = (MfaAuthenticationToken)subAuthenticationProvider.authenticate(authentication);

        // then
        Assertions.assertEquals(2, resultToken.getAuthLevel());
    }

    @Test
    @DisplayName("2차 인증 실패 test")
    void secondStageAuthFailureTest(@Mock Authentication authentication) throws Exception {
        /** 시나리오
         * 조건 1. SecurityContext에 1차 인증까지 완료된 MfaAuthenticationToken 저장된 상태
         *          (pin = "123456" 저장됨)
         * 조건 2. Authentication 객체를 통해 사용자가 입력한 pin 값이 들어온다.
         */
        // given
        MfaAuthenticationToken storedAuthenticationToken = new MfaAuthenticationToken("user", "1111", null, 1);
        storedAuthenticationToken.setPin("123456");

        BDDMockito.given(securityContextUtils.getAuthentication()).willReturn(storedAuthenticationToken);// 저장된 pin
        BDDMockito.given((String)authentication.getCredentials()).willReturn("11111"); // 입력된 pin
        // when
        SubAuthenticationProvider subAuthenticationProvider = new SubAuthenticationProvider(securityContextUtils);

        // then
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            subAuthenticationProvider.authenticate(authentication);
        });
    }
}
