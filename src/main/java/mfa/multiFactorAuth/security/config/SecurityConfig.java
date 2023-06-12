package mfa.multiFactorAuth.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.common.MfaAuthenticationEntryPoint;
import mfa.multiFactorAuth.security.factory.UrlResourceMapFactoryBean;
import mfa.multiFactorAuth.security.filter.MfaAuthenticationFilter;
import mfa.multiFactorAuth.security.handler.MfaAccessDeniedHandler;
import mfa.multiFactorAuth.security.handler.MfaAuthenticationFailureHandler;
import mfa.multiFactorAuth.security.handler.MfaAuthenticationSuccessHandler;
import mfa.multiFactorAuth.security.interceptor.MfaFilterSecurityInterceptor;
import mfa.multiFactorAuth.security.manager.MfaAuthenticationManager;
import mfa.multiFactorAuth.security.metasource.UrlFilterInvocationSecurityMetadataSource;
import mfa.multiFactorAuth.security.provider.FormAuthenticationProvider;
import mfa.multiFactorAuth.security.provider.SubAuthenticationProvider;
import mfa.multiFactorAuth.security.service.FormUserDetailsService;
import mfa.multiFactorAuth.security.utils.SecurityContextUtils;
import mfa.multiFactorAuth.security.voter.MfaAccessDecisionManager;
import mfa.multiFactorAuth.service.EmailAuthService;
import mfa.multiFactorAuth.service.SecurityResourceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final FormUserDetailsService formUserDetailsService;
    private final SecurityResourceService securityResourceService;
    private final SecurityContextUtils securityContextUtils;

    private String[] permitAllResources = {"/login", "/pin", "/css/**", "/js/**"};


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean(name="formAuthenticationProvider")
    public AuthenticationProvider formAuthenticationProvider() {
        return new FormAuthenticationProvider(passwordEncoder(), formUserDetailsService);
    }

    @Bean(name="subAuthenticationProvider")
    public AuthenticationProvider subAuthenticationProvider() {
        return new SubAuthenticationProvider(securityContextUtils);
    }

    @Bean
    public AuthenticationManager mfaAuthenticationManager() throws Exception {
        List<AuthenticationProvider> providerList = new ArrayList<>();
        providerList.add(formAuthenticationProvider());
        providerList.add(subAuthenticationProvider());
        return new MfaAuthenticationManager(securityContextUtils, providerList);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new MfaAuthenticationEntryPoint("/login");
    }

    public MfaFilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
        MfaFilterSecurityInterceptor mfaFilterSecurityInterceptor = new MfaFilterSecurityInterceptor(permitAllResources);
        mfaFilterSecurityInterceptor.setAccessDecisionManager(mfaAccessDecisionManager());
        mfaFilterSecurityInterceptor.setSecurityMetadataSource(urlFilterInvocationSecurityMetadataSource());
        return mfaFilterSecurityInterceptor;
    }

    private AccessDecisionManager mfaAccessDecisionManager() {
        List<AccessDecisionVoter<? extends Object> > decisionVoters = new ArrayList<>();
        decisionVoters.add(new RoleVoter());
        return new MfaAccessDecisionManager(decisionVoters);
    }

    @Bean
    public FilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource() throws Exception {
        return new UrlFilterInvocationSecurityMetadataSource(urlResourceMapFactoryBean().getObject(), securityResourceService);
    }
    private UrlResourceMapFactoryBean urlResourceMapFactoryBean() {
        return new UrlResourceMapFactoryBean(securityResourceService);
    }

    @Bean
    public AccessDeniedHandler mfaAccessDeniedHandler() {
        return new MfaAccessDeniedHandler("/denied", securityContextUtils, "/second-login");
    }
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(String defaultTargetUrl, String secondAuthAuthenticationUrl) {
        MfaAuthenticationSuccessHandler mfaAuthenticationSuccessHandler = new MfaAuthenticationSuccessHandler();
        mfaAuthenticationSuccessHandler.setDefaultTargetUrl(defaultTargetUrl);
        mfaAuthenticationSuccessHandler.setSecondAuthenticationUrl(secondAuthAuthenticationUrl);
        return mfaAuthenticationSuccessHandler;
    }
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(String defaultUrl, String secondAuthenticationUrl) {
        return new MfaAuthenticationFailureHandler(defaultUrl, secondAuthenticationUrl);
    }

    @Bean
    public MfaAuthenticationFilter mfaAuthenticationFilter() throws Exception {
        MfaAuthenticationFilter authenticationFilter = new MfaAuthenticationFilter();
        authenticationFilter.setAuthenticationManager(mfaAuthenticationManager());
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler("/", "/second-login"));
        authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler("/login?error", "/second-login"));
        return authenticationFilter;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        //httpSecurity.csrf().disable();
        httpSecurity.authorizeRequests()
                .anyRequest()
                .authenticated();
/*
        httpSecurity
                .authenticationManager(mfaAuthenticationManager());
        httpSecurity.formLogin(
                form -> form.loginPage("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler("/login?error"))
                        //.permitAll()
        );
 */
        httpSecurity
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(mfaAccessDeniedHandler());

        httpSecurity.addFilterBefore(filterSecurityInterceptor(), FilterSecurityInterceptor.class);
        httpSecurity.addFilterBefore(mfaAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
