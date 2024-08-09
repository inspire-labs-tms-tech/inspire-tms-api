package com.inspiretmstech.api.src.auth.anon;

import com.inspiretmstech.api.src.auth.Authority;

import java.util.List;

public class AnonymousAuthenticationToken extends org.springframework.security.authentication.AnonymousAuthenticationToken {

    public AnonymousAuthenticationToken() {
        super("00000000-0000-0000-0000-000000000000", "anon", List.of(Authority.ANON));
    }

}
