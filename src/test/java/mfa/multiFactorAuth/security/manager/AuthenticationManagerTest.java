package mfa.multiFactorAuth.security.manager;

import mfa.multiFactorAuth.security.provider.FormAuthenticationProvider;
import mfa.multiFactorAuth.security.provider.SubAuthenticationProvider;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AuthenticationManagerTest {
    @Mock private SecurityContextUtils securityContextUtils;
    @Mock private FormAuthenticationProvider formAuthenticationProvider;
    @Mock private SubAuthenticationProvider subAuthenticationProvider;
    private List<AuthenticationProvider> providerList = new ArrayList<>();
    private MfaAuthenticationManager authenticationManager;
    @BeforeEach
    void makeProviderList() {
        providerList.add(formAuthenticationProvider);
        providerList.add(subAuthenticationProvider);
        this.authenticationManager = new MfaAuthenticationManager(securityContextUtils, providerList);
    }

    @Test
    @DisplayName("1차 인증 : formAuthenticationProvider 호출")
    void first_stage_authenticationManagerTest(@Mock Authentication authentication) {
        /** 시나리오 : authenticationManager.authenticate(authenticate) 테스트
         * ==> SecurityContext에 저장된 것이 AnonymousToken인 경우, 1차 인증을 시도한다.
         */
        // given
        List<GrantedAuthority> dummyList = new ArrayList<>();
        dummyList.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "dummy";
            }
        });
        BDDMockito.given(securityContextUtils.getAuthentication())
                .willReturn(new AnonymousAuthenticationToken("1", "kim", dummyList));

        BDDMockito.given(formAuthenticationProvider.supports(BDDMockito.any()))
                        .willReturn(true);
        BDDMockito.given(formAuthenticationProvider.authenticate(authentication))
                .willReturn(new MfaAuthenticationToken("kim", null, 1));

        // when
        MfaAuthenticationToken token = (MfaAuthenticationToken)authenticationManager.authenticate(authentication);
        // then
        Assertions.assertEquals(1, token.getAuthLevel());
    }

    @Test
    @DisplayName("2차 인증 : subAuthenticationProvider 호출")
    void second_stage_authenticationManagerTest(@Mock Authentication authentication) {
        /** 시나리오 : authenticationManager.authenticate(authenticate) 테스트
         * ==> SecurityContext에 저장된 것이 MfaAuthenticationToken(authLevel==1)인 경우, 2차 인증을 시도한다.
         */
        BDDMockito.given(securityContextUtils.getAuthentication())
                .willReturn(new MfaAuthenticationToken("kim", null, 1));

        BDDMockito.given(subAuthenticationProvider.supports(BDDMockito.any()))
                .willReturn(true);
        BDDMockito.given(subAuthenticationProvider.authenticate(authentication))
                .willReturn(new MfaAuthenticationToken("kim", null, 2));

        // when
        MfaAuthenticationToken token = (MfaAuthenticationToken)authenticationManager.authenticate(authentication);
        // then
        Assertions.assertEquals(2, token.getAuthLevel());

    }
}
