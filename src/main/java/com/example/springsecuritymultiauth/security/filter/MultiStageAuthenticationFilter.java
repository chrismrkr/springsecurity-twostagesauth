package com.example.springsecuritymultiauth.security.filter;

import com.example.springsecuritymultiauth.security.dto.LoginRequestDto;
import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import com.example.springsecuritymultiauth.token.UsernamePasswordMultiAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class MultiStageAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/login", "POST");
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private String authenticationProcessLevelParameter = "authenticationProcessLevel";
    private boolean postOnly = true;
    private final MemberAuthenticationLevelRepository memberAuthenticationLevelRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MultiStageAuthenticationFilter(AuthenticationManager authenticationManager, MemberAuthenticationLevelRepository memberAuthenticationLevelRepository) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.memberAuthenticationLevelRepository = memberAuthenticationLevelRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        LoginRequestDto dto = objectMapper.readValue(requestBody, LoginRequestDto.class);
        String username = dto.getUsername();
        username = username != null ? username.trim() : "";
        String password = dto.getPassword();
        password = password != null ? password : "";
        Integer authenticationProcessLevel = dto.getAuthenticationProcessLevel();
        authenticationProcessLevel = authenticationProcessLevel != null ? authenticationProcessLevel : 1;

        Integer completedAuthenticationLevel = memberAuthenticationLevelRepository.findCurrentAuthenticationLevel(username)
                .orElse(0);
        if(completedAuthenticationLevel + 1 != authenticationProcessLevel) {
            throw new AuthenticationServiceException("Authentication level not matched: " + "you should do level "+ ((Integer)(completedAuthenticationLevel+1)).toString() + ", but you try level " + authenticationProcessLevel.toString());
        }

        UsernamePasswordMultiAuthenticationToken authRequest = UsernamePasswordMultiAuthenticationToken.unauthenticated(username, password, authenticationProcessLevel);
        this.setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }

    private String obtainAuthenticationProcessingLevel(HttpServletRequest request) {
        return request.getParameter(this.authenticationProcessLevelParameter);
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordMultiAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
        this.passwordParameter = passwordParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }
    public final String getUsernameParameter() {
        return this.usernameParameter;
    }
    public final String getPasswordParameter() {
        return this.passwordParameter;
    }
}
