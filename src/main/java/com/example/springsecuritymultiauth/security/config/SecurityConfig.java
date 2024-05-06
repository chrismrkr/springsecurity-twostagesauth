package com.example.springsecuritymultiauth.security.config;

import com.example.springsecuritymultiauth.security.filter.MultiStageAuthenticationFilter;
import com.example.springsecuritymultiauth.security.manager.MultiStageAuthenticationManager;
import com.example.springsecuritymultiauth.security.manager.port.MemberAuthenticationLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {
    private final AuthenticationSuccessHandler multiStageAuthenticationSuccessHandler;
    private final AuthenticationFailureHandler multiStageAuthenticationFailureHandler;
    private final AuthenticationProvider firstStepAuthenticationProvider;
    private final AuthenticationProvider secondStepAuthenticationProvider;
    private final MemberAuthenticationLevelRepository memberAuthenticationLevelRepository;

    @Autowired
    public SecurityConfig(AuthenticationSuccessHandler multiStageAuthenticationSuccessHandler,
                          AuthenticationFailureHandler multiStageAuthenticationFailureHandler,
                          @Qualifier("firstStepAuthenticationProvider") AuthenticationProvider firstStepAuthenticationProvider,
                          @Qualifier("secondStepAuthenticationProvider") AuthenticationProvider secondStepAuthenticationProvider,
                          MemberAuthenticationLevelRepository memberAuthenticationLevelRepository) {
        this.multiStageAuthenticationSuccessHandler = multiStageAuthenticationSuccessHandler;
        this.multiStageAuthenticationFailureHandler = multiStageAuthenticationFailureHandler;
        this.firstStepAuthenticationProvider = firstStepAuthenticationProvider;
        this.secondStepAuthenticationProvider = secondStepAuthenticationProvider;
        this.memberAuthenticationLevelRepository = memberAuthenticationLevelRepository;
    }
    @Bean
    public AuthenticationManager multiStageAuthenticationManager() throws Exception {
        List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(firstStepAuthenticationProvider);
        providers.add(secondStepAuthenticationProvider);
        return new MultiStageAuthenticationManager(providers);
    }
    @Bean
    public MultiStageAuthenticationFilter multiStageAuthenticationFilter() throws Exception {
        MultiStageAuthenticationFilter multiStageAuthenticationFilter = new MultiStageAuthenticationFilter(multiStageAuthenticationManager(), memberAuthenticationLevelRepository);
        multiStageAuthenticationFilter.setAuthenticationSuccessHandler(multiStageAuthenticationSuccessHandler);
        multiStageAuthenticationFilter.setAuthenticationFailureHandler(multiStageAuthenticationFailureHandler);
        return multiStageAuthenticationFilter;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors()
                .and()
                .csrf().disable();
        // httpSecurity.addFilterBefore(multiStageAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.authorizeHttpRequests()
                        .requestMatchers("/login/success/**").permitAll()
                        .requestMatchers("/login/failure/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/member/register/**").permitAll()
                        .anyRequest().authenticated();
        httpSecurity.addFilterBefore(multiStageAuthenticationFilter(), LogoutFilter.class);
        return httpSecurity.build();
    }
}
