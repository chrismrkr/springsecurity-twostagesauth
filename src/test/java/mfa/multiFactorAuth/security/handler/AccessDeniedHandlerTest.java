package mfa.multiFactorAuth.security.handler;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class AccessDeniedHandlerTest {
    @Mock private SecurityContextUtils securityContextUtils;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock AccessDeniedException accessDeniedException;
    @Captor
    private ArgumentCaptor<String> redirectUrlCaptor;

    private AccessDeniedHandler accessDeniedHandler;

    @Test
    @DisplayName("1차 인증까지만 완료된 후, 리소스 접근 : 2차 인증 페이지로 redirect")
    void authenticationNotComplete() throws ServletException, IOException {
        // given
        BDDMockito.given(securityContextUtils.getAuthentication())
                .willReturn(new MfaAuthenticationToken("kim", "123", 1));
        accessDeniedHandler = new MfaAccessDeniedHandler("/error", securityContextUtils);

        // when
        accessDeniedHandler.handle(request, response, accessDeniedException);
        BDDMockito.verify(response, BDDMockito.times(1)).sendRedirect(redirectUrlCaptor.capture());
        String redirectUrl = redirectUrlCaptor.getValue();

        // then
        Assertions.assertEquals("/second-login", redirectUrl);
    }
}
