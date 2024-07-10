package com.inspiretmstech.api.auth.bearer;

import com.inspiretmstech.api.auth.Authorities;
import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.ApiKeysRecord;
import org.jooq.meta.derby.sys.Sys;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class APIKeyDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID id = UUID.fromString(username);
        try {
            Optional<ApiKeysRecord> key = DatabaseConnection.getInstance().with(supabase ->
                    supabase.selectFrom(Tables.API_KEYS).where(Tables.API_KEYS.ID.eq(id)).fetchOne());
            if(key.isEmpty()) throw new UsernameNotFoundException("API key not found");

            User.UserBuilder builder = User.builder()
                    .username(id.toString())
                    .password(key.get().getHash());

            ArrayList<String> authorities = new ArrayList<>();
            authorities.add(Authorities.Type.ANON.name());

            switch (key.get().getType()) {
                case USER -> authorities.add(Authorities.Type.USER.name());
                case CUSTOMER -> authorities.add(Authorities.Type.CUSTOMER.name());
                default -> throw new AccessDeniedException("unhandled API key type: " + key.get().getType());
            }

            return builder
                    .authorities(authorities.toArray(new String[0]))
                    .build();
        } catch (SQLException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
}
