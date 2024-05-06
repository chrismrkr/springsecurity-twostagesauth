package com.example.springsecuritymultiauth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class UsernamePasswordMultiAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 610L;
    private final Object principal;
    private Object credentials;
    private final int authenticationProcessLevel;

    public UsernamePasswordMultiAuthenticationToken(Object principal, Object credentials, int authenticationProcessLevel) {
        super((Collection)null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
        this.authenticationProcessLevel = authenticationProcessLevel;
    }

    public UsernamePasswordMultiAuthenticationToken(Object principal, Object credentials, int authenticationProcessLevel, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.authenticationProcessLevel = authenticationProcessLevel;
        super.setAuthenticated(true);
    }

    public static UsernamePasswordMultiAuthenticationToken unauthenticated(Object principal, Object credentials, int authenticationProcessLevel) {
        return new UsernamePasswordMultiAuthenticationToken(principal, credentials, authenticationProcessLevel);
    }

    public static UsernamePasswordMultiAuthenticationToken authenticated(Object principal, Object credentials, int authenticationProcessLevel, Collection<? extends GrantedAuthority> authorities) {
        return new UsernamePasswordMultiAuthenticationToken(principal, credentials, authenticationProcessLevel, authorities);
    }
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
    public int getAuthenticationProcessLevel() {
        return this.authenticationProcessLevel;
    }
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
