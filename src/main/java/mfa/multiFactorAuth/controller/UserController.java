package mfa.multiFactorAuth.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.service.EmailAuthService;
import org.springframework.http.HttpEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final EmailAuthService emailAuthService;
    @GetMapping("/login")
    public String do1stLogin() {
        return "login";
    }
    @GetMapping("/second-login")
    public String do2ndLogin() {
        return "second-login";
    }

    @PostMapping("/email-confirm")
    @ResponseBody
    public PinDto makePinCode(@RequestBody PinDto pinDto) { // pin 생성하는 코드
        String pin = null;
        try {
            pin = emailAuthService.sendAuthEmail(pinDto.getMessengerId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MfaAuthenticationToken authentication = (MfaAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        authentication.setPin(pin);
        return pinDto;
    }

    @Data
    private static class PinDto {
        private String messengerId;
    }
}
