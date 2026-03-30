package com.valdisdot.llamacpphandler.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Security filter for validating API key authentication.
 * <p>
 * This filter intercepts all incoming HTTP requests and validates the
 * {@code api-key} header against a configured API key. If the API key is
 * valid, it sets the authentication context using Spring Security's
 * {@link PreAuthenticatedAuthenticationToken}. If invalid or missing,
 * it returns a {@code 401 Unauthorized} response.
 * </p>
 *
 * <h2>Authentication Flow</h2>
 * <ol>
 *   <li>Client sends request with {@code api-key} header</li>
 *   <li>Filter extracts the header value</li>
 *   <li>Filter compares against configured API key</li>
 *   <li>If valid: sets authentication in {@link SecurityContextHolder}</li>
 *   <li>If invalid: returns {@code 401 Unauthorized} JSON response</li>
 * </ol>
 *
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li>API key must be configured via {@code server.api_key} property</li>
 *   <li>Only requests with valid API key will be processed</li>
 *   <li>Authentication token is set once per request</li>
 *   <li>Invalid API keys are rejected immediately</li>
 * </ul>
 *
 * <h2>Example Request</h2>
 * <pre>{@code
 * GET /api/v1/model
 * api-key: an-api-key
 *
 * Response: 200 OK (if valid)
 * Response: 401 Unauthorized (if invalid)
 * }</pre>
 *
 * <h2>Example Response (Unauthorized)</h2>
 * <pre>{@code
 * {
 *   "error": "Unauthorized: Invalid or missing API key"
 * }
 * }</pre>
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    /**
     * The configured API key for authentication.
     * <p>
     * This value is injected from the {@code server.api_key} property
     * and is used to validate incoming requests.
     * </p>
     */
    private final String apiKey;

    /**
     * Constructs a new ApiKeyFilter instance.
     *
     * @param apiKey The API key to validate incoming requests against
     */
    public ApiKeyFilter(@Value("${server.api_key}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Filters incoming HTTP requests and validates API key authentication.
     * <p>
     * This method is called once per request and performs the following steps:
     * <ol>
     *     <li>Extracts the {@code api-key} header from the request</li>
     *     <li>Compares the header value against the configured API key</li>
     *     <li>If valid: creates a {@link PreAuthenticatedAuthenticationToken} and sets it in {@link SecurityContextHolder}</li>
     *     <li>If invalid: returns a {@code 401 Unauthorized} response with JSON error message</li>
     *     <li>Continues the filter chain if authentication succeeds</li>
     * </ol>
     * </p>
     *
     * @param request      The HTTP request being processed
     * @param response     The HTTP response to send
     * @param filterChain  The filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String headerApiKey = request.getHeader("api-key");

        if (headerApiKey == null || !headerApiKey.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API key\"}");
        } else {
            PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(
                    headerApiKey,
                    null,
                    Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}