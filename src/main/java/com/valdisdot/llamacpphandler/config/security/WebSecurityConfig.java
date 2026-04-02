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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * Security configuration for the Llama.cpp Handler application.
 * <p>
 * This configuration class sets up Spring Security with two distinct security chains:
 * <ol>
 *   <li><strong>API Security Chain</strong> (Order 1): Protects all {@code /api/v1/**} endpoints
 *       requiring API key authentication via {@link ApiKeyFilter}</li>
 *   <li><strong>UI Security Chain</strong> (Order 2): Protects web interface endpoints
 *       with selective public access for static resources</li>
 * </ol>
 * </p>
 *
 * <h2>Security Architecture</h2>
 * <p>
 * The application uses a stateless authentication approach with the following components:
 * </p>
 * <ul>
 *   <li>{@link ApiKeyFilter}: Validates API key from request headers</li>
 *   <li>{@link UserDetailsManager}: In-memory user details manager (currently empty)</li>
 *   <li>Stateless session policy: No server-side sessions are created</li>
 * </ul>
 *
 * <h2>API Security Chain</h2>
 * <p>
 * All requests to {@code /api/v1} and {@code /api/v1/**} paths require authentication.
 * The {@link ApiKeyFilter} is inserted before the {@link UsernamePasswordAuthenticationFilter}
 * to validate the API key header.
 * </p>
 *
 * <h2>UI Security Chain</h2>
 * <p>
 * The web interface allows public access to:
 * <ul>
 *   <li>Root path {@code /}</li>
 *   <li>Index page {@code /index.html}</li>
 *   <li>Static resources {@code /js/**}, {@code /css/**}</li>
 *   <li>Favicon {@code /favicon.ico}</li>
 * </ul>
 * All other UI requests require authentication.
 * </p>
 *
 * <h2>Configuration Properties</h2>
 * <table border="1">
 *   <tr>
 *     <th>Property</th>
 *     <th>Description</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td>{@code server.api_key}</td>
 *     <td>API key for authentication</td>
 *     <td>{@code "an-api-key"}</td>
 *   </tr>
 * </table>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // API request with valid API key
 * GET /api/v1/model
 * api-key: an-api-key
 *
 * // UI request (public)
 * GET /
 *
 * // UI request (authenticated)
 * GET /api/v1/model/all
 * api-key: an-api-key
 * }</pre>
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer {

    /**
     * Registers the API key filter as a servlet filter.
     * <p>
     * This bean configures the {@link ApiKeyFilter} with a disabled state
     * by default. The filter is enabled through the {@link #apiSecurityChain}
     * method where it's added to the security filter chain.
     * </p>
     *
     * @param filter The API key filter to register
     * @return Filter registration bean with the filter configured
     */
    @Bean
    public FilterRegistrationBean<ApiKeyFilter> registration(ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Provides an in-memory user details manager for Spring Security.
     * <p>
     * This bean creates an {@link InMemoryUserDetailsManager} instance
     * which can be used to store and retrieve user details. Currently,
     * the manager is empty as the application uses API key authentication
     * instead of traditional username/password authentication.
     * </p>
     *
     * @return In-memory user details manager instance
     */
    @Bean
    public UserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    /**
     * Configures the security filter chain for API endpoints.
     * <p>
     * This filter chain (Order 1) protects all API endpoints under
     * {@code /api/v1} and {@code /api/v1/**} paths. It requires
     * authentication via the {@link ApiKeyFilter} and uses stateless
     * session management.
     * </p>
     *
     * <h3>Security Configuration</h3>
     * <ul>
     *     <li><strong>Matcher:</strong> {@code /api/v1, /api/v1/**}</li>
     *     <li><strong>Authentication:</strong> Required for all requests</li>
     *     <li><strong>Filter:</strong> {@link ApiKeyFilter} added before {@link UsernamePasswordAuthenticationFilter}</li>
     *     <li><strong>Session Policy:</strong> STATELESS (no server-side sessions)</li>
     *     <li><strong>CSRF:</strong> Disabled</li>
     * </ul>
     * </p>
     *
     * @param http The HttpSecurity configuration builder
     * @param apiKeyFilter The API key filter to insert into the chain
     * @return Configured SecurityFilterChain for API endpoints
     * @throws Exception if security configuration fails
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityChain(HttpSecurity http, ApiKeyFilter apiKeyFilter) throws Exception {
        return http
                .securityMatcher("/api/v1", "/api/v1/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    return config;
                }))
                .build();
    }

    /**
     * Configures the security filter chain for UI endpoints.
     * <p>
     * This filter chain (Order 2) protects web interface endpoints.
     * It allows public access to static resources and the root path,
     * while requiring authentication for other UI requests.
     * </p>
     *
     * <h3>Security Configuration</h3>
     * <ul>
     *     <li><strong>Public Paths:</strong> {@code /, /index.html, /js/**, /css/**, /favicon.ico}</li>
     *     <li><strong>Authentication:</strong> Required for all other requests</li>
     *     <li><strong>Form Login:</strong> Disabled</li>
     *     <li><strong>CSRF:</strong> Disabled</li>
     * </ul>
     * </p>
     *
     * @param http The HttpSecurity configuration builder
     * @return Configured SecurityFilterChain for UI endpoints
     * @throws Exception if security configuration fails
     */
    @Bean
    @Order(2)
    public SecurityFilterChain uiSecurityChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/js/**", "/css/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(ss -> ss.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}