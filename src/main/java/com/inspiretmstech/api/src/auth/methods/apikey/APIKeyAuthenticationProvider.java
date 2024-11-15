package com.inspiretmstech.api.src.auth.methods.apikey;

import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationProvider;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.routines.ValidateApiKey;
import com.inspiretmstech.db.tables.records.ApiKeysRecord;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class APIKeyAuthenticationProvider implements AuthenticationProvider<APIKeyAuthenticationHolder> {

    private String getAuth(@NotNull HttpServletRequest request) {

        String Authorization = request.getHeader("Authorization");
        String xApiKey = request.getHeader("x-api-key");
        if (Objects.nonNull(Authorization)) return Authorization;
        if (Objects.nonNull(xApiKey)) return xApiKey;
        return "";
    }

    @Override
    public boolean supports(@NotNull HttpServletRequest request) {
        return Pattern.compile("^Bearer [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}:itms_api_v1_[0-9A-z]{32}$").matcher(this.getAuth(request)).matches();
    }

    @Override
    public SecurityHolder<APIKeyAuthenticationHolder> authenticate(@NotNull HttpServletRequest request) {
        if (!this.supports(request))
            throw new RuntimeException(APIKeyAuthenticationHolder.class + " does not support this request!");
        List<String> parts = List.of(List.of(this.getAuth(request).split(" ")).get(1).split(":"));
        UUID apiKeyID = UUID.fromString(parts.get(0));
        String apiKeySecret = parts.get(1);

        // validate the credentials
        ValidateApiKey validator = new ValidateApiKey();
        validator.setPId(apiKeyID);
        validator.setPSecret(apiKeySecret);

        try {
            PostgresConnection.getInstance().unsafely(supabase -> validator.execute(supabase.configuration()));
        } catch (Exception e) {
            throw new AccessDeniedException("unable to validate credentials");
        }

        boolean authenticated = Optional.ofNullable(validator.getReturnValue()).orElse(false);
        if (!authenticated) throw new AccessDeniedException("invalid credentials");

        // get the API key
        Optional<ApiKeysRecord> key;
        try {
            key = PostgresConnection.getInstance().unsafely(supabase ->
                    supabase.selectFrom(Tables.API_KEYS).where(Tables.API_KEYS.ID.eq(apiKeyID)).fetchOne());
            if (key.isEmpty()) throw new AccessDeniedException("unable to load api key");
        } catch (Exception e) {
            throw new AccessDeniedException("an unexpected error occurred while loading api key");
        }

        // return the holder
        return new SecurityHolder<>(new APIKeyAuthenticationHolder(apiKeyID.toString(), key.get()));
    }
}
