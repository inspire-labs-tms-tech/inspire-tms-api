package com.inspiretmstech.api.src.auth;

import com.inspiretmstech.api.src.auth.anon.AnonymousAuthenticationProvider;
import com.inspiretmstech.api.src.auth.anon.AnonymousAuthenticationToken;
import com.inspiretmstech.api.src.auth.bearer.APIKeyAuthenticationProvider;
import com.inspiretmstech.api.src.auth.bearer.APIKeyDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthenticationFilter extends OncePerRequestFilter {

    private final APIKeyDetailsService service = new APIKeyDetailsService();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = new AnonymousAuthenticationToken();

        String auth = Optional.ofNullable(request.getHeader("Authorization")).orElse("");
        if (Pattern.compile("^Bearer [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}:itms_api_v1_[0-9A-z]{32}$").matcher(auth).matches()) {
            List<String> parts = List.of(List.of(auth.split(" ")).get(1).split(":"));
            UserDetails key = service.loadUserByUsername(parts.get(0));
            authentication = new PreAuthenticatedAuthenticationToken(key, parts.get(1), key.getAuthorities());

        }

        Authentication authenticated = null;
        for (AuthenticationProvider provider : Arrays.asList(new APIKeyAuthenticationProvider(), new AnonymousAuthenticationProvider())) {
            if (provider.supports(authentication.getClass())) {
                authenticated = provider.authenticate(authentication);
                break;
            }
        }


        SecurityContextHolder.getContext().setAuthentication(Objects.nonNull(authenticated) ? authenticated : new AnonymousAuthenticationToken());
        filterChain.doFilter(request, response);
    }
}
