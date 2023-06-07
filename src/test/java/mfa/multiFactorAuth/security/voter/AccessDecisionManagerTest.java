package mfa.multiFactorAuth.security.voter;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.web.FilterInvocation;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessDecisionManagerTest {
    MfaAccessDecisionManager accessDecisionManager;
    @BeforeEach
    void initVoter() {
        List<AccessDecisionVoter<? extends Object> > decisionVoters = new ArrayList<>();
        decisionVoters.add(new RoleVoter());
        accessDecisionManager = new MfaAccessDecisionManager(decisionVoters);
    }

    @Test
    @DisplayName("1차 인증 완료 상태에서 GET / 호출")
    void accessFail(@Mock MfaAuthenticationToken authentication, @Mock FilterInvocation filterInvocation) {
        //given
        given(authentication.getAuthLevel()).willReturn(1);
        given(filterInvocation.getRequestUrl()).willReturn("/");
        //when - then
        Assertions.assertThrows(AccessDeniedException.class,
                () -> accessDecisionManager.decide(authentication, filterInvocation, null));
    }
    @Test
    @DisplayName("1차 인증 완료 상태에서 GET /second-login 호출")
    void accessSuccess(@Mock MfaAuthenticationToken authentication, @Mock FilterInvocation filterInvocation) {
        //given
        given(authentication.getAuthLevel()).willReturn(1);
        given(filterInvocation.getRequestUrl()).willReturn("/second-login");
        //when
        accessDecisionManager.decide(authentication, filterInvocation, null);
    }


    @Test
    @DisplayName("1차 인증 완료 상태에서 POST /email-confirm 호출")
    void accessSuccess2(@Mock MfaAuthenticationToken authentication, @Mock FilterInvocation filterInvocation) {
        // given
        given(authentication.getAuthLevel()).willReturn(1);
        given(filterInvocation.getRequestUrl()).willReturn("/email-confirm");
        // when
        accessDecisionManager.decide(authentication, filterInvocation, null);
    }
}
