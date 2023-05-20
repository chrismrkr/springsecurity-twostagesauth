package mfa.multiFactorAuth.security.voter;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;
import java.util.List;

public class MfaAccessDecisionManager extends AbstractAccessDecisionManager {
    public MfaAccessDecisionManager(List<AccessDecisionVoter<?>> decisionVoters) {
        super(decisionVoters);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException { // return: 리소스 접근 허용
        /* 인증 단계 확인 */
        FilterInvocation filterInvocation = (FilterInvocation)object;
        if(authentication instanceof MfaAuthenticationToken) {
            if(((MfaAuthenticationToken)authentication).getAuthLevel() == 1) {
                if (filterInvocation.getRequestUrl().equals("/second-login") || filterInvocation.getRequestUrl().equals("/email-confirm")) {
                    return;
                } else {
                    throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
                }
            }
        }

        int deny = 0;
        for (AccessDecisionVoter voter : getDecisionVoters()) {
            int result = voter.vote(authentication, object, configAttributes);
            switch (result) {
                case AccessDecisionVoter.ACCESS_GRANTED:
                    return;
                case AccessDecisionVoter.ACCESS_DENIED:
                    deny++;
                    break;
                default:
                    break;
            }
        }
        if (deny > 0) {
            throw new AccessDeniedException(
                    this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
        }
        // To get this far, every AccessDecisionVoter abstained
        checkAllowIfAllAbstainDecisions();
    }

}
