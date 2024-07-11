package com.inspiretmstech.api.auth;

import com.inspiretmstech.api.auth.bearer.APIKey;
import com.inspiretmstech.api.models.ResponseException;
import com.inspiretmstech.api.models.exceptions.InsufficientPrivilegesException;
import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.RolesRecord;
import com.inspiretmstech.db.tables.records.UsersRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
public class RequiresProcessor {

    private void fail(Scopes scope) {
        throw new InsufficientPrivilegesException(scope);
    }

    public void requires(Scopes scope) throws SQLException {

        APIKey key = (APIKey) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // load the user
        Optional<UsersRecord> user = DatabaseConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.USERS).where(Tables.USERS.ID.eq(key.getSub())).fetchOne());
        if(user.isEmpty())
            throw new ResponseException("Unable to Load User", "The server was unable to load a user object for the current user");
        if(Objects.isNull(user.get().getRoleId())) this.fail(scope);

        // load the role
        Optional<RolesRecord> role = DatabaseConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.ROLES).where(Tables.ROLES.ID.eq(user.get().getRoleId())).fetchOne());
        if (role.isEmpty())
            throw new ResponseException("Unable to Load Role", "The server was unable to load a role for the current user");

        // check the scope
        switch (scope) {
            case FACILITIES -> {
                if (!role.get().getScopeFacilities()) this.fail(scope);
            }
            case NEVER -> this.fail(scope);
            default -> throw new ResponseException("Unhandled Scope", "The server is improperly configured to handle scope " + scope, "Contact Inspire TMS Support to resolve this issue");
        }
    }

    @Around("@annotation(Requires)")
    public Object process(ProceedingJoinPoint joinPoint, Requires Requires) throws Throwable {
        for (Scopes scopes : Requires.value())
            this.requires(scopes);
        return joinPoint.proceed();
    }

}
