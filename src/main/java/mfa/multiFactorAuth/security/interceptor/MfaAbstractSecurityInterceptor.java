package mfa.multiFactorAuth.security.interceptor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mfa.multiFactorAuth.security.token.MfaAuthenticationToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.log.LogMessage;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.event.PublicInvocationEvent;
import org.springframework.security.access.intercept.AfterInvocationManager;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.access.intercept.RunAsManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public abstract class MfaAbstractSecurityInterceptor implements InitializingBean, ApplicationEventPublisherAware, MessageSourceAware {
    protected final Log logger = LogFactory.getLog(this.getClass());
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private ApplicationEventPublisher eventPublisher;
    private AccessDecisionManager accessDecisionManager;
    private AfterInvocationManager afterInvocationManager;
    private AuthenticationManager authenticationManager = new MfaAbstractSecurityInterceptor.NoOpAuthenticationManager();
    private RunAsManager runAsManager = new NullRunAsManager();
    private boolean alwaysReauthenticate = false;
    private boolean rejectPublicInvocations = false;
    private boolean validateConfigAttributes = true;
    private boolean publishAuthorizationSuccess = false;

    public MfaAbstractSecurityInterceptor() {
    }

    public void afterPropertiesSet() {
        Assert.notNull(this.getSecureObjectClass(), "Subclass must provide a non-null response to getSecureObjectClass()");
        Assert.notNull(this.messages, "A message source must be set");
        Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
        Assert.notNull(this.accessDecisionManager, "An AccessDecisionManager is required");
        Assert.notNull(this.runAsManager, "A RunAsManager is required");
        Assert.notNull(this.obtainSecurityMetadataSource(), "An SecurityMetadataSource is required");
        Assert.isTrue(this.obtainSecurityMetadataSource().supports(this.getSecureObjectClass()), () -> {
            return "SecurityMetadataSource does not support secure object class: " + this.getSecureObjectClass();
        });
        Assert.isTrue(this.runAsManager.supports(this.getSecureObjectClass()), () -> {
            return "RunAsManager does not support secure object class: " + this.getSecureObjectClass();
        });
        Assert.isTrue(this.accessDecisionManager.supports(this.getSecureObjectClass()), () -> {
            return "AccessDecisionManager does not support secure object class: " + this.getSecureObjectClass();
        });
        if (this.afterInvocationManager != null) {
            Assert.isTrue(this.afterInvocationManager.supports(this.getSecureObjectClass()), () -> {
                return "AfterInvocationManager does not support secure object class: " + this.getSecureObjectClass();
            });
        }

        if (this.validateConfigAttributes) {
            Collection<ConfigAttribute> attributeDefs = this.obtainSecurityMetadataSource().getAllConfigAttributes();
            if (attributeDefs == null) {
                this.logger.warn("Could not validate configuration attributes as the SecurityMetadataSource did not return any attributes from getAllConfigAttributes()");
                return;
            }

            this.validateAttributeDefs(attributeDefs);
        }

    }

    private void validateAttributeDefs(Collection<ConfigAttribute> attributeDefs) {
        Set<ConfigAttribute> unsupportedAttrs = new HashSet();
        Iterator var3 = attributeDefs.iterator();

        while(true) {
            ConfigAttribute attr;
            do {
                do {
                    do {
                        if (!var3.hasNext()) {
                            if (unsupportedAttrs.size() != 0) {
                                this.logger.trace("Did not validate configuration attributes since validateConfigurationAttributes is false");
                                throw new IllegalArgumentException("Unsupported configuration attributes: " + unsupportedAttrs);
                            }

                            this.logger.trace("Validated configuration attributes");
                            return;
                        }

                        attr = (ConfigAttribute)var3.next();
                    } while(this.runAsManager.supports(attr));
                } while(this.accessDecisionManager.supports(attr));
            } while(this.afterInvocationManager != null && this.afterInvocationManager.supports(attr));

            unsupportedAttrs.add(attr);
        }
    }

    protected InterceptorStatusToken beforeInvocation(Object object) {
        Assert.notNull(object, "Object was null");
        if (!getSecureObjectClass().isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("Security invocation attempted for object " + object.getClass().getName()
                    + " but AbstractSecurityInterceptor only configured to support secure objects of type: "
                    + getSecureObjectClass());
        }
        Collection<ConfigAttribute> attributes = this.obtainSecurityMetadataSource().getAttributes(object);
        if (CollectionUtils.isEmpty(attributes)) {
            Assert.isTrue(!this.rejectPublicInvocations,
                    () -> "Secure object invocation " + object
                            + " was denied as public invocations are not allowed via this interceptor. "
                            + "This indicates a configuration error because the "
                            + "rejectPublicInvocations property is set to 'true'");
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.format("Authorized public object %s", object));
            }
            publishEvent(new PublicInvocationEvent(object));
            return null; // no further work post-invocation
        }
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            credentialsNotFound(this.messages.getMessage("AbstractSecurityInterceptor.authenticationNotFound",
                    "An Authentication object was not found in the SecurityContext"), object, attributes);
        }

        Authentication authenticated = authenticateIfRequired();
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(LogMessage.format("Authorizing %s with attributes %s", object, attributes));
        }
        // Attempt authorization
        attemptAuthorization(object, attributes, authenticated);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Authorized %s with attributes %s", object, attributes));
        }
        if (this.publishAuthorizationSuccess) {
            publishEvent(new AuthorizedEvent(object, attributes, authenticated));
        }

        // Attempt to run as a different user
        Authentication runAs = this.runAsManager.buildRunAs(authenticated, object, attributes);
        if (runAs != null) {
            SecurityContext origCtx = SecurityContextHolder.getContext();
            SecurityContext newCtx = SecurityContextHolder.createEmptyContext();
            newCtx.setAuthentication(runAs);
            SecurityContextHolder.setContext(newCtx);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.format("Switched to RunAs authentication %s", runAs));
            }
            // need to revert to token.Authenticated post-invocation
            return new InterceptorStatusToken(origCtx, true, attributes, object);
        }
        this.logger.trace("Did not switch RunAs authentication since RunAsManager returned null");
        // no further work post-invocation
        return new InterceptorStatusToken(SecurityContextHolder.getContext(), false, attributes, object);

    }

    private void attemptAuthorization(Object object, Collection<ConfigAttribute> attributes, Authentication authenticated) {
        try {
            this.accessDecisionManager.decide(authenticated, object, attributes);
        } catch (AccessDeniedException var5) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Failed to authorize %s with attributes %s using %s", object, attributes, this.accessDecisionManager));
            } else if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.format("Failed to authorize %s with attributes %s", object, attributes));
            }

            this.publishEvent(new AuthorizationFailureEvent(object, attributes, authenticated, var5));
            throw var5;
        }
    }

    protected void finallyInvocation(InterceptorStatusToken token) {
        if (token != null && token.isContextHolderRefreshRequired()) {
            SecurityContextHolder.setContext(token.getSecurityContext());
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.of(() -> {
                    return "Reverted to original authentication " + token.getSecurityContext().getAuthentication();
                }));
            }
        }

    }

    protected Object afterInvocation(InterceptorStatusToken token, Object returnedObject) {
        if (token == null) {
            return returnedObject;
        } else {
            this.finallyInvocation(token);
            if (this.afterInvocationManager != null) {
                try {
                    returnedObject = this.afterInvocationManager.decide(token.getSecurityContext().getAuthentication(), token.getSecureObject(), token.getAttributes(), returnedObject);
                } catch (AccessDeniedException var4) {
                    this.publishEvent(new AuthorizationFailureEvent(token.getSecureObject(), token.getAttributes(), token.getSecurityContext().getAuthentication(), var4));
                    throw var4;
                }
            }

            return returnedObject;
        }
    }

    private Authentication authenticateIfRequired() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && !this.alwaysReauthenticate) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not re-authenticate %s before authorizing", authentication));
            }
            return authentication;
        }

        authentication = this.authenticationManager.authenticate(authentication);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Re-authenticated %s before authorizing", authentication));
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return authentication;
    }

    private void credentialsNotFound(String reason, Object secureObject, Collection<ConfigAttribute> configAttribs) {
        AuthenticationCredentialsNotFoundException exception = new AuthenticationCredentialsNotFoundException(reason);
        AuthenticationCredentialsNotFoundEvent event = new AuthenticationCredentialsNotFoundEvent(secureObject, configAttribs, exception);
        this.publishEvent(event);
        throw exception;
    }

    public AccessDecisionManager getAccessDecisionManager() {
        return this.accessDecisionManager;
    }

    public AfterInvocationManager getAfterInvocationManager() {
        return this.afterInvocationManager;
    }

    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    public RunAsManager getRunAsManager() {
        return this.runAsManager;
    }

    public abstract Class<?> getSecureObjectClass();

    public boolean isAlwaysReauthenticate() {
        return this.alwaysReauthenticate;
    }

    public boolean isRejectPublicInvocations() {
        return this.rejectPublicInvocations;
    }

    public boolean isValidateConfigAttributes() {
        return this.validateConfigAttributes;
    }

    public abstract SecurityMetadataSource obtainSecurityMetadataSource();

    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

    public void setAfterInvocationManager(AfterInvocationManager afterInvocationManager) {
        this.afterInvocationManager = afterInvocationManager;
    }

    public void setAlwaysReauthenticate(boolean alwaysReauthenticate) {
        this.alwaysReauthenticate = alwaysReauthenticate;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setAuthenticationManager(AuthenticationManager newManager) {
        this.authenticationManager = newManager;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    public void setPublishAuthorizationSuccess(boolean publishAuthorizationSuccess) {
        this.publishAuthorizationSuccess = publishAuthorizationSuccess;
    }

    public void setRejectPublicInvocations(boolean rejectPublicInvocations) {
        this.rejectPublicInvocations = rejectPublicInvocations;
    }

    public void setRunAsManager(RunAsManager runAsManager) {
        this.runAsManager = runAsManager;
    }

    public void setValidateConfigAttributes(boolean validateConfigAttributes) {
        this.validateConfigAttributes = validateConfigAttributes;
    }

    private void publishEvent(ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }

    }

    private static class NoOpAuthenticationManager implements AuthenticationManager {
        private NoOpAuthenticationManager() {
        }

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            throw new AuthenticationServiceException("Cannot authenticate " + authentication);
        }
    }
    private static class NullRunAsManager implements RunAsManager {
        NullRunAsManager() {
        }

        public Authentication buildRunAs(Authentication authentication, Object object, Collection<ConfigAttribute> config) {
            return null;
        }

        public boolean supports(ConfigAttribute attribute) {
            return false;
        }

        public boolean supports(Class<?> clazz) {
            return true;
        }
    }
}
