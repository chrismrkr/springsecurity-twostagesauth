package mfa.multiFactorAuth.security.handler;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.RedirectStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationSuccessHandlerTest {
    MfaAuthenticationSuccessHandler successHandler;
    @Mock RedirectStrategy redirectStrategy;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    @Mock MfaAuthenticationToken authenticationToken;
    @Captor ArgumentCaptor<String> redirectUrlCaptor;

    @BeforeEach
    void initMockHttpServlet() {
        successHandler = new MfaAuthenticationSuccessHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("1차 인증에 성공 - 2차 인증 페이지로 redirect")
    void firstStageAuthenticationSuccess() throws ServletException, IOException {
        // given
        successHandler.setRedirectStrategy(redirectStrategy);
        given(authenticationToken.getAuthLevel()).willReturn(1);
        // when - then
        successHandler.onAuthenticationSuccess(request, response, authenticationToken);
    }
}
