package com.inspiretmstech.api.auth;

import org.springframework.security.core.GrantedAuthority;

public class Authorities implements GrantedAuthority {

    public enum Type {
        ANON,
        USER,
        CUSTOMER
    }

    private final Type authority;

    public Authorities(Type authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority.name();
    }

}
