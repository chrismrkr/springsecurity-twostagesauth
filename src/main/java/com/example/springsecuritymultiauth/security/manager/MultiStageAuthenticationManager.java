package com.example.springsecuritymultiauth.security.manager;

import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import com.example.springsecuritymultiauth.token.UsernamePasswordMultiAuthenticationToken;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MultiStageAuthenticationManager implements AuthenticationManager, MessageSourceAware, InitializingBean  {
    private static final Log logger = LogFactory.getLog(ProviderManager.class);
    private AuthenticationEventPublisher eventPublisher;
    private List<AuthenticationProvider> providers;
    protected MessageSourceAccessor messages;
    private boolean eraseCredentialsAfterAuthentication;

    public MultiStageAuthenticationManager(AuthenticationProvider... providers) {
        this.providers = Arrays.asList(providers);
    }

    public MultiStageAuthenticationManager(List<AuthenticationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public void afterPropertiesSet() {
        this.checkState();
    }
    private void checkState() {
        Assert.isTrue(!this.providers.isEmpty(), "A list of AuthenticationProviders is required");
        Assert.isTrue(!CollectionUtils.contains(this.providers.iterator(), (Object)null), "providers list cannot contain null values");
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Class<? extends Authentication> toTest = authentication.getClass();
        AuthenticationException lastException = null;
        Authentication result = null;

        int currentPosition = ((UsernamePasswordMultiAuthenticationToken)authentication).getAuthenticationProcessLevel() - 1;
        int size = this.getProviders().size();

        AuthenticationProvider authenticationProvider = this.getProviders().get(currentPosition);
        if(authenticationProvider.supports(toTest)) {
            if (logger.isTraceEnabled()) {
                Log var10000 = logger;
                String var10002 = authenticationProvider.getClass().getSimpleName();
                var10000.trace(LogMessage.format("Authenticating request with %s (%d/%d)", var10002, currentPosition, size));
            }
            try {
                result = authenticationProvider.authenticate(authentication);
                if (result != null) {
                    this.copyDetails(authentication, result);
                }
            } catch (InternalAuthenticationServiceException | AccountStatusException var14) {
                this.prepareException(var14, authentication);
                throw var14;
            } catch (AuthenticationException var15) {
                lastException = var15;
            }
        }

        if(result != null) {
            if (this.eraseCredentialsAfterAuthentication && result instanceof CredentialsContainer) {
                ((CredentialsContainer)result).eraseCredentials();
            }
            return result;
        } else {
            if (lastException == null) {
                lastException = new ProviderNotFoundException(this.messages.getMessage("ProviderManager.providerNotFound", new Object[]{toTest.getName()}, "No AuthenticationProvider found for {0}"));
            }
            throw lastException;
        }
    }
    private void copyDetails(Authentication source, Authentication dest) {
        if (dest instanceof AbstractAuthenticationToken token && dest.getDetails() == null) {
            token.setDetails(source.getDetails());
        }
    }
    private void prepareException(AuthenticationException ex, Authentication auth) {
        this.eventPublisher.publishAuthenticationFailure(ex, auth);
    }
    public List<AuthenticationProvider> getProviders() {
        return this.providers;
    }
}
