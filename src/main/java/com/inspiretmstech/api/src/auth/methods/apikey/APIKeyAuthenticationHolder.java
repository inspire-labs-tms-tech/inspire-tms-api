package com.inspiretmstech.api.src.auth.methods.apikey;

import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationHolder;
import com.inspiretmstech.db.tables.records.ApiKeysRecord;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class APIKeyAuthenticationHolder extends AuthenticationHolder {

    private final ApiKeysRecord key;

    public APIKeyAuthenticationHolder(String id, ApiKeysRecord key) {
        super(APIKeyAuthenticationHolder.class, id);
        this.key = key;
    }

    public UUID getSub() {
        return this.key.getSubId();
    }

    @Override
    public String getEncryptedSecret() {
        return this.key.getHash();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(Authority.ANON); // everyone gets anon

        switch (this.key.getType()) {
            case USER -> authorities.add(Authority.USER);
            case CUSTOMER -> authorities.add(Authority.CUSTOMER);
            case ORBCOMM -> authorities.add(Authority.ORBCOMM);
            case PRINCETON_TMX -> authorities.add(Authority.PRINCETONTMX);
            default -> this.logger().warn("unhandled API key type: {}", this.key.getType());
        }

        return authorities;
    }

}
