package com.inspiretmstech.api.auth.bearer;

import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.db.routines.ValidateApiKey;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

public class APIKeyAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        ValidateApiKey validator = new ValidateApiKey();
        validator.setPId(UUID.fromString(authentication.getPrincipal().toString()));
        validator.setPSecret(authentication.getCredentials().toString());

        try {
            DatabaseConnection.getInstance().with(supabase -> validator.execute(supabase.configuration()));
        } catch (Exception e) {
            throw new AccessDeniedException("unable to validate credentials");
        }

        authentication.setAuthenticated(Optional.ofNullable(validator.getReturnValue()).orElse(false));
        if(!authentication.isAuthenticated()) throw new AccessDeniedException("invalid API credentials");

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
