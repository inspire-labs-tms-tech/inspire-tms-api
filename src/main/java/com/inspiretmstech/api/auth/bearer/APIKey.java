package com.inspiretmstech.api.auth.bearer;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.db.tables.records.ApiKeysRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class APIKey implements UserDetails {

    private final Logger logger = LoggerFactory.getLogger(APIKey.class);

    private final ApiKeysRecord key;

    public APIKey(ApiKeysRecord key) {
        this.key = key;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(Authority.ANON); // everyone gets anon

        switch (this.key.getType()) {
            case USER -> authorities.add(Authority.USER);
            case CUSTOMER -> authorities.add(Authority.CUSTOMER);
            default -> logger.warn("unhandled API key type: {}", this.key.getType());
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.key.getHash();
    }

    @Override
    public String getUsername() {
        return this.key.getId().toString();
    }

    public UUID getID() {
        return this.key.getId();
    }

    public UUID getSub() {
        return this.key.getSubId();
    }
}
