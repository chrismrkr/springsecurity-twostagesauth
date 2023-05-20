package mfa.multiFactorAuth.security.handler;

import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class MfaAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private RequestCache requestCache = new HttpSessionRequestCache();
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        int currentAuthLevel = ((MfaAuthenticationToken)authentication).getAuthLevel();
        if(currentAuthLevel == 1) {
            redirectStrategy.sendRedirect(request, response, "/second-login");
        }
        else if(currentAuthLevel == 2) {
            redirectStrategy.sendRedirect(request, response, "/");
        }
        else {
            throw new RuntimeException("Invalid Auth Level");
        }

    }
}
