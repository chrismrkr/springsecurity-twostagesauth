package mfa.multiFactorAuth.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class MfaAccessDeniedHandler implements AccessDeniedHandler {

    private final String errorPage;
    private final SecurityContextUtils securityContextUtils;
    private final String redirectUrlIfAuthNotComplete;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = securityContextUtils.getAuthentication();
        if(authentication instanceof MfaAuthenticationToken) {
            int authLevel = ((MfaAuthenticationToken)authentication).getAuthLevel();
            if(authLevel == 1) {
                log.info("redirect {}", redirectUrlIfAuthNotComplete);
                response.sendRedirect(redirectUrlIfAuthNotComplete);
            }
        }
        else {
            response.sendRedirect(errorPage);
        }
    }

}
