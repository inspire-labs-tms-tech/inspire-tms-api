package com.inspiretmstech.api.src.auth.methods.session;

import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.db.tables.records.RolesRecord;
import com.inspiretmstech.db.tables.records.UsersRecord;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class SessionAuthenticationHolder extends AuthenticationHolder {

    private final UsersRecord user;
    private final RolesRecord role;

    public SessionAuthenticationHolder(@NotNull String id, @NotNull UsersRecord user, @NotNull RolesRecord role) {
        super(SessionAuthenticationHolder.class, id);
        this.user = user;
        this.role = role;
    }

    public UsersRecord getUser() {
        return user;
    }

    public RolesRecord getRole() {
        return role;
    }

    @Override
    public String getEncryptedSecret() {
        return "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(Authority.USER, Authority.ANON);
    }
}
