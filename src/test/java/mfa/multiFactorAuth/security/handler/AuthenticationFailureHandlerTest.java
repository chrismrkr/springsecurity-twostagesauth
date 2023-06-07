package mfa.multiFactorAuth.security.handler;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AuthenticationFailureHandlerTest {
    MfaAuthenticationFailureHandler failureHandler = new MfaAuthenticationFailureHandler("/login?error");
    @Mock RedirectStrategy redirectStrategy;
    @Captor
    ArgumentCaptor<String> urlCaptor;
    @Test
    @DisplayName("1차 인증 실패 - redirect /login")
    void firstStageAuthenticationFailureTest() throws ServletException, IOException {
        //given
        failureHandler.setRedirectStrategy(redirectStrategy);
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authenticationException = new BadCredentialsException("BadCredentialException");

        //when
        failureHandler.onAuthenticationFailure(request, response, authenticationException);
        verify(redirectStrategy).sendRedirect(any(), any(), urlCaptor.capture());

        //then
        String capturedUrl = urlCaptor.getValue();
        Assertions.assertEquals(capturedUrl, "/login?error");

    }

    @Test
    @DisplayName("2차 인증 실패 - redirect /second-login")
    void secondStageAuthenticationFailureTest() throws ServletException, IOException {
        //given
        failureHandler.setRedirectStrategy(redirectStrategy);
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authenticationException = new BadCredentialsException("pinNotMatchException");

        //when
        failureHandler.onAuthenticationFailure(request, response, authenticationException);
        verify(redirectStrategy).sendRedirect(any(), any(), urlCaptor.capture());

        //then
        String capturedUrl = urlCaptor.getValue();
        Assertions.assertEquals(capturedUrl, "/second-login");
    }
}
