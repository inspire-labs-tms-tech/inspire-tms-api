package com.inspiretmstech.api.src.auth.methods.session;

import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationProvider;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Environment;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.RolesRecord;
import com.inspiretmstech.db.tables.records.UsersRecord;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class SessionAuthenticationProvider implements AuthenticationProvider<SessionAuthenticationHolder> {
    @Override
    public boolean supports(@NotNull HttpServletRequest request) {
        Optional<String> session = Optional.ofNullable(request.getHeader("inspire-access-token-cookie"));
        return session.isPresent() && !session.get().isBlank();
    }

    @Override
    public SecurityHolder<SessionAuthenticationHolder> authenticate(@NotNull HttpServletRequest request) {

        String session = request.getHeader("inspire-access-token-cookie");
        if (Objects.isNull(session))
            throw new RuntimeException(SessionAuthenticationProvider.class + " does not support this request!");

        List<String> parts = Arrays.stream(session.split("\\.")).toList();
        if (parts.size() != 3) throw new AccessDeniedException("invalid session format");

        try {
            SecretKey jwtSecret = Keys.hmacShaKeyFor(Environment.get("JWT_SECRET").getBytes(StandardCharsets.UTF_8));
            JwtParser parser = Jwts.parser().verifyWith(jwtSecret).build(); // verifies JWT against signature
            Claims payload = parser.parseSignedClaims(session).getPayload();

            String sessionID = (String) payload.get("session_id");
            String subject = payload.getSubject();
            if (Objects.isNull(subject)) throw new AccessDeniedException("sub missing");

            Optional<UsersRecord> user = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.USERS).where(Tables.USERS.UID.eq(UUID.fromString(subject))).fetchOne());
            if(user.isEmpty()) throw new AccessDeniedException("user missing");
            if(Objects.isNull(user.get().getRoleId())) throw new AccessDeniedException("user not assigned role");

            Optional<RolesRecord> role = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.ROLES).where(Tables.ROLES.ID.eq(user.get().getRoleId())).fetchOne());
            if(role.isEmpty()) throw new AccessDeniedException("role missing");

            return new SecurityHolder<>(new SessionAuthenticationHolder(sessionID, user.get(), role.get()));
        } catch (SignatureException e) {
            throw new AccessDeniedException("invalid JWT");
        } catch (SQLException e) {
            throw new AccessDeniedException("unable to look-up user/role");
        }
    }
}
