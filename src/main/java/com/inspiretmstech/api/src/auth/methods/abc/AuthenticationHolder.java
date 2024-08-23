package com.inspiretmstech.api.src.auth.methods.abc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public abstract class AuthenticationHolder {

    private final String id;
    private final Logger logger;

    public AuthenticationHolder(Class<?> cls, String id) {
        this.logger = LoggerFactory.getLogger(cls);
        this.id = id;
    }

    public abstract String getEncryptedSecret();

    public abstract Collection<? extends GrantedAuthority> getAuthorities();

    public String getId() {
        return this.id;
    }

    public Logger logger() {
        return this.logger;
    }

    public Logger getLogger() {
        return this.logger();
    }
}
