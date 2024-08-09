package com.inspiretmstech.api.src.auth.bearer;

import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.routines.ValidateApiKey;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Optional;

public class APIKeyAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        ValidateApiKey validator = new ValidateApiKey();
        APIKey key = (APIKey) authentication.getPrincipal();
        validator.setPId(key.getID());
        validator.setPSecret(authentication.getCredentials().toString());

        try {
            PostgresConnection.getInstance().with(supabase -> validator.execute(supabase.configuration()));
        } catch (Exception e) {
            throw new AccessDeniedException("unable to validate credentials");
        }

        authentication.setAuthenticated(Optional.ofNullable(validator.getReturnValue()).orElse(false));
        if (!authentication.isAuthenticated()) throw new AccessDeniedException("invalid API credentials");

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
