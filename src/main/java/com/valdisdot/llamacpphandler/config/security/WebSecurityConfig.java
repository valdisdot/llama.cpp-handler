package com.valdisdot.llamacpphandler.config.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean<ApiKeyFilter> registration(ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public UserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityChain(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
        return http
                .securityMatcher("/api/v1", "/api/v1/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain uiSecurityChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/js/**", "/css/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}
