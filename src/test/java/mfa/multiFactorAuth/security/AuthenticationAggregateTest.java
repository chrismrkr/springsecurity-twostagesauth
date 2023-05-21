package mfa.multiFactorAuth.security;


import mfa.multiFactorAuth.controller.UserController;
import mfa.multiFactorAuth.security.provider.SubAuthenticationProvider;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.service.EmailAuthService;
import mfa.multiFactorAuth.service.impl.EmailAuthServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.plugins.MockMaker;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
public class AuthenticationAggregateTest {


}
