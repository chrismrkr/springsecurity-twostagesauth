package mfa.multiFactorAuth.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import mfa.multiFactorAuth.service.EmailAuthService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final EmailAuthService emailAuthService;
    private final SecurityContextUtils securityContextUtils;
    @GetMapping("/login")
    public String do1stLogin() {
        return "login";
    }
    @GetMapping("/second-login")
    public String getSecondLoginPage() {
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

        MfaAuthenticationToken authentication = (MfaAuthenticationToken)securityContextUtils.getAuthentication();
        authentication.setPin(pin);
        return pinDto;
    }

    @Data
    public static class PinDto {
        private String messengerId;

        public PinDto(String messengerId, String pin) {
            this.messengerId = messengerId;
        }
    }
}
