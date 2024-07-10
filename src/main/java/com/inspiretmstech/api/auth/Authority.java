package com.inspiretmstech.api.auth;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {

    ANON(Authorities.ANON),
    USER(Authorities.USER),
    CUSTOMER(Authorities.CUSTOMER);

    private final String authority;

    Authority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }

    public static class Authorities {
        public static final String ANON = "ANON";
        public static final String USER = "USER";
        public static final String CUSTOMER = "CUSTOMER";
    }
}
