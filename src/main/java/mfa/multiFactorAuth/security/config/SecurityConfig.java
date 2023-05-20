package mfa.multiFactorAuth.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfa.multiFactorAuth.security.common.MfaAuthenticationEntryPoint;
import mfa.multiFactorAuth.security.factory.UrlResourceMapFactoryBean;
import mfa.multiFactorAuth.security.handler.MfaAccessDeniedHandler;
import mfa.multiFactorAuth.security.handler.MfaAuthenticationSuccessHandler;
import mfa.multiFactorAuth.security.interceptor.MfaFilterSecurityInterceptor;
import mfa.multiFactorAuth.security.manager.MfaAuthenticationManager;
import mfa.multiFactorAuth.security.metasource.UrlFilterInvocationSecurityMetadataSource;
import mfa.multiFactorAuth.security.provider.FormAuthenticationProvider;
import mfa.multiFactorAuth.security.provider.SubAuthenticationProvider;
import mfa.multiFactorAuth.security.service.FormUserDetailsService;
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final FormUserDetailsService formUserDetailsService;
    private final MfaAuthenticationSuccessHandler mfaAuthenticationSuccessHandler;
    private final SecurityResourceService securityResourceService;

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
        return new SubAuthenticationProvider(passwordEncoder());
    }

    @Bean
    public AuthenticationManager mfaAuthenticationManager() throws Exception {
        List<AuthenticationProvider> providerList = new ArrayList<>();
        providerList.add(formAuthenticationProvider());
        providerList.add(subAuthenticationProvider());
        return new MfaAuthenticationManager(providerList);
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
        return new MfaAccessDeniedHandler("/denied");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        //httpSecurity.csrf().disable();
        httpSecurity.authorizeRequests()
                .anyRequest()
                .authenticated();

        httpSecurity
                .authenticationManager(mfaAuthenticationManager());

        httpSecurity.formLogin(
                form -> form.loginPage("/login")
                        .successHandler(mfaAuthenticationSuccessHandler)
                        .permitAll()
        );

        httpSecurity
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedPage("/accessDenied")
                .accessDeniedHandler(mfaAccessDeniedHandler());


        httpSecurity.addFilterBefore(filterSecurityInterceptor(), FilterSecurityInterceptor.class);
        return httpSecurity.build();
    }
}
