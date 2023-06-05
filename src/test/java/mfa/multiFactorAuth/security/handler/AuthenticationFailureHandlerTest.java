package mfa.multiFactorAuth.security.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFailureHandlerTest {
    MfaAuthenticationFailureHandler failureHandler = new MfaAuthenticationFailureHandler();

    @Test
    @DisplayName("1차 인증 실패 - redirect /login")
    void firstStageAuthenticationFailureTest() {

    }

    @Test
    @DisplayName("2차 인증 실패 - redirect /second-login")
    void secondStageAuthenticationFailureTest() {

    }
}
