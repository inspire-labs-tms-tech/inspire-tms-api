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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter extends OncePerRequestFilter {

    // order by precedent (lower index = earlier/higher precedent => first supported is used)
    private final List<AuthenticationProvider<?>> providers = Arrays.asList(
            new SessionAuthenticationProvider(),
            new APIKeyAuthenticationProvider(),
            new AnonymousAuthenticationProvider()
    );

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
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
            (new ResponseException("Access Denied", "An access denied exception has occurred", e.getMessage())).respondWith(response);
        }
    }
}
