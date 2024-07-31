package com.inspiretmstech.api.auth.bearer;

import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.ApiKeysRecord;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class APIKeyDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID id = UUID.fromString(username);
        try {
            Optional<ApiKeysRecord> key = PostgresConnection.getInstance().with(supabase ->
                    supabase.selectFrom(Tables.API_KEYS).where(Tables.API_KEYS.ID.eq(id)).fetchOne());
            if (key.isEmpty()) throw new UsernameNotFoundException("API key not found");
            return new APIKey(key.get());
        } catch (SQLException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
}
