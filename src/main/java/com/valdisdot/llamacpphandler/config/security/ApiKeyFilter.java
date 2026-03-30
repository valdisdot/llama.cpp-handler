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

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final String apiKey;

    public ApiKeyFilter(@Value("${server.api_key}") String apiKey) {
        this.apiKey = apiKey;
    }

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
