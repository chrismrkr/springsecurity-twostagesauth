package mfa.multiFactorAuth.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class MfaAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 570L;
    private final Object principal;
    private Object credentials;
    private int authLevel;
    private String pin;

    public MfaAuthenticationToken(Object principal, Object credentials, int authCount) {
        super((Collection)null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
        this.authLevel = authCount;
    }

    public MfaAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, int authCount) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
        this.authLevel = authCount;
    }

    public static UsernamePasswordAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new UsernamePasswordAuthenticationToken(principal, credentials);
    }

    public static UsernamePasswordAuthenticationToken authenticated(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        return new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
    }

    public Object getCredentials() {
        return this.credentials;
    }

    public Object getPrincipal() {
        return this.principal;
    }
    public int getAuthLevel() { return this.authLevel; }
    public String getPin() { return this.pin; }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }

    public void increaseAuthLevel() {
        this.authLevel++;
    }
    public void setPin(String pin) { this.pin = pin; }
}
