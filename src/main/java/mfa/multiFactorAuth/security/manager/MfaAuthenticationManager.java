package mfa.multiFactorAuth.security.manager;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class MfaAuthenticationManager implements AuthenticationManager, MessageSourceAware, InitializingBean {
    private static final Log logger = LogFactory.getLog(ProviderManager.class);
    private AuthenticationEventPublisher eventPublisher;
    private List<AuthenticationProvider> providers;
    private Map<Integer, AuthenticationProvider> authenticationProviders = new HashMap<>();
    protected MessageSourceAccessor messages;
    private AuthenticationManager parent;
    private boolean eraseCredentialsAfterAuthentication;
    private SecurityContextUtils securityContextUtils;

    public MfaAuthenticationManager(SecurityContextUtils securityContextUtils, AuthenticationProvider... providers) {
        this(securityContextUtils, Arrays.asList(providers), (AuthenticationManager)null);
    }

    public MfaAuthenticationManager(SecurityContextUtils securityContextUtils, List<AuthenticationProvider> providers) {
        this(securityContextUtils, providers, (AuthenticationManager)null);
        for(int i=0; i< providers.size(); i++) {
            authenticationProviders.put(i, providers.get(i));
        }
    }

    public MfaAuthenticationManager(SecurityContextUtils securityContextUtils, List<AuthenticationProvider> providers, AuthenticationManager parent) {
        this.eventPublisher = new MfaAuthenticationManager.NullEventPublisher();
        this.providers = Collections.emptyList();
        this.messages = SpringSecurityMessageSource.getAccessor();
        this.eraseCredentialsAfterAuthentication = true;
        Assert.notNull(providers, "providers list cannot be null");
        this.providers = providers;
        this.parent = parent;
        this.checkState();
        this.securityContextUtils = securityContextUtils;
    }

    public void afterPropertiesSet() {
        this.checkState();
    }

    private void checkState() {
        Assert.isTrue(this.parent != null || !this.providers.isEmpty(), "A parent AuthenticationManager or a list of AuthenticationProviders is required");
        Assert.isTrue(!CollectionUtils.contains(this.providers.iterator(), (Object)null), "providers list cannot contain null values");
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Class<? extends Authentication> toTest = authentication.getClass();
        AuthenticationException lastException = null;
        AuthenticationException parentException = null;
        Authentication result = null;
        Authentication parentResult = null;
        int currentPosition = 0;
        int size = this.providers.size();
        Iterator var9 = this.getProviders().iterator();

        Authentication currentAuthentication = securityContextUtils.getAuthentication();
        int authLevel = 0;
        if(currentAuthentication instanceof MfaAuthenticationToken) {
            authLevel = ((MfaAuthenticationToken)currentAuthentication).getAuthLevel();
        }

        AuthenticationProvider authenticationProvider = authenticationProviders.get(authLevel);
        if (authenticationProvider.supports(toTest)) {
            if (logger.isTraceEnabled()) {
                Log var10000 = logger;
                String var10002 = authenticationProvider.getClass().getSimpleName();
                ++currentPosition;
                var10000.trace(LogMessage.format("Authenticating request with %s (%d/%d)", var10002, currentPosition, size));
            }

            try {
                result = authenticationProvider.authenticate(authentication);
                this.copyDetails(authentication, result);
            } catch (InternalAuthenticationServiceException | AccountStatusException var14) {
                this.prepareException(var14, authentication);
                throw var14;
            } catch (AuthenticationException var15) {
                lastException = var15;
            }
        }

        if (result == null && this.parent != null) {
            try {
                parentResult = this.parent.authenticate(authentication);
                result = parentResult;
            } catch (ProviderNotFoundException var12) {
            } catch (AuthenticationException var13) {
                parentException = var13;
                lastException = var13;
            }
        }

        if (result != null) {
            if (this.eraseCredentialsAfterAuthentication && result instanceof CredentialsContainer) {
                ((CredentialsContainer)result).eraseCredentials();
            }

            if (parentResult == null) {
                this.eventPublisher.publishAuthenticationSuccess(result);
            }

            return result;
        } else {
            if (lastException == null) {
                lastException = new ProviderNotFoundException(this.messages.getMessage("ProviderManager.providerNotFound", new Object[]{toTest.getName()}, "No AuthenticationProvider found for {0}"));
            }

            if (parentException == null) {
                this.prepareException((AuthenticationException)lastException, authentication);
            }
            throw lastException;
        }
    }

    private void prepareException(AuthenticationException ex, Authentication auth) {
        this.eventPublisher.publishAuthenticationFailure(ex, auth);
    }

    private void copyDetails(Authentication source, Authentication dest) {
        if (dest instanceof AbstractAuthenticationToken && dest.getDetails() == null) {
            AbstractAuthenticationToken token = (AbstractAuthenticationToken)dest;
            token.setDetails(source.getDetails());
        }

    }

    public List<AuthenticationProvider> getProviders() {
        return this.providers;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    public void setAuthenticationEventPublisher(AuthenticationEventPublisher eventPublisher) {
        Assert.notNull(eventPublisher, "AuthenticationEventPublisher cannot be null");
        this.eventPublisher = eventPublisher;
    }

    public void setEraseCredentialsAfterAuthentication(boolean eraseSecretData) {
        this.eraseCredentialsAfterAuthentication = eraseSecretData;
    }

    public boolean isEraseCredentialsAfterAuthentication() {
        return this.eraseCredentialsAfterAuthentication;
    }

    private static final class NullEventPublisher implements AuthenticationEventPublisher {
        private NullEventPublisher() {
        }

        public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        }

        public void publishAuthenticationSuccess(Authentication authentication) {
        }
    }
}
