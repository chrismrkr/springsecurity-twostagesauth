package mfa.multiFactorAuth.security.provider;

import mfa.multiFactorAuth.domain.Account;
import mfa.multiFactorAuth.security.service.AccountContext;
import mfa.multiFactorAuth.security.service.FormUserDetailsService;
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class AuthenticationProviderTest {
    @Mock
    private SecurityContextUtils securityContextUtils;
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    @DisplayName("1차 인증 성공 test")
    void firstStageAuthSuccessTest(@Mock FormUserDetailsService formUserDetailsService, @Mock Authentication authentication) {
        /** 시나리오 : 1차 인증 provider 단위 테스트
         *  1. account 객체 생성 - 데이터베이스에 저장된 회원 레코드
         *  2. authentication Mock 객체 생성 - 사용자가 입력할 ID("kim")와 password("1234")
         *  3. formUserDetailsService Mock 객체 생성 - account를 포함한 AccountContext 반환
         *  => 인증 후 MfaAuthenticationToken(authLevel=1)을 반환함.(1차 인증 성공)
         */
        // given
        Account account = Account.builder()
                                .username("kim")
                                .password(passwordEncoder.encode("1234"))
                                .age(20)
                                .build();
        BDDMockito
                .given(authentication.getName())
                .willReturn("kim");
        BDDMockito
                .given(authentication.getCredentials())
                .willReturn("1234");
        BDDMockito
                .given(formUserDetailsService.loadUserByUsername(BDDMockito.any()))
                .willReturn(new AccountContext(account, new ArrayList<>()));
        FormAuthenticationProvider firstStageProvider = new FormAuthenticationProvider(passwordEncoder, formUserDetailsService);

        // when
        MfaAuthenticationToken authenticateToken = (MfaAuthenticationToken)firstStageProvider.authenticate(authentication);

        // then
        Assertions.assertEquals(1, authenticateToken.getAuthLevel());
    }

    @Test
    @DisplayName("1차 인증 실패 test")
    void firstStageAuthFailureTest(@Mock FormUserDetailsService formUserDetailsService, @Mock Authentication authentication) {
        /** 시나리오 : 1차 인증 provider 단위 테스트
         *  1. account 객체 생성 - 데이터베이스에 저장된 회원 레코드
         *  2. authentication Mock 객체 생성 - 사용자가 입력할 ID("kim")와 password("1111")
         *  3. formUserDetailsService Mock 객체 생성 - account를 포함한 AccountContext 반환
         *  => 인증 실패. BadCredentialsException 발생
         */
        // given
        Account account = Account.builder()
                .username("kim")
                .password(passwordEncoder.encode("1234"))
                .age(20)
                .build();
        BDDMockito
                .given(authentication.getName())
                .willReturn("kim");
        BDDMockito
                .given(authentication.getCredentials())
                .willReturn("1111");
        BDDMockito
                .given(formUserDetailsService.loadUserByUsername(BDDMockito.any()))
                .willReturn(new AccountContext(account, new ArrayList<>()));
        FormAuthenticationProvider firstStageProvider = new FormAuthenticationProvider(passwordEncoder, formUserDetailsService);

        // when, then
        Assertions.assertThrows(BadCredentialsException.class, () -> firstStageProvider.authenticate(authentication));
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
