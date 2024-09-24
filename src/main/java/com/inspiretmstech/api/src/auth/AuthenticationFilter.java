package com.inspiretmstech.api.src.auth;

import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationProvider;
import com.inspiretmstech.api.src.auth.methods.apikey.APIKeyAuthenticationProvider;
import com.inspiretmstech.api.src.auth.methods.apikey.anon.AnonymousAuthenticationProvider;
import com.inspiretmstech.api.src.auth.methods.session.SessionAuthenticationProvider;
import com.inspiretmstech.api.src.models.ResponseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    // order by precedent (lower index = earlier/higher precedent => first supported is used)
    private final List<AuthenticationProvider<?>> providers = Arrays.asList(
            new SessionAuthenticationProvider(),
            new APIKeyAuthenticationProvider(),
            new AnonymousAuthenticationProvider()
    );

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        logger.trace("Handling Inbound Authentication Request: {}", request);

        try {
            for (AuthenticationProvider<?> provider : providers) {
                if (provider.supports(request)) {
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    provider.authenticate(request).getAuthentication()
                            );
                    break; // use the first supported method
                }
            }

            filterChain.doFilter(request, response);
        } catch (AccessDeniedException e) {
            logger.trace("Authentication Failure Exception: {}", e.getMessage());
            (new ResponseException("Access Denied", "An access denied exception has occurred", e.getMessage())).respondWith(response);
        }
    }
}
