package com.inspiretmstech.api.src.auth.methods.apikey.anon;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class AnonymousAuthenticationHolder extends AuthenticationHolder {

    public AnonymousAuthenticationHolder() {
        super(AnonymousAuthenticationHolder.class, "00000000-0000-0000-0000-000000000000");
    }

    @Override
    public String getEncryptedSecret() {
        return "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(Authority.ANON);
    }

}
