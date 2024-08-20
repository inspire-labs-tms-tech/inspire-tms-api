package com.inspiretmstech.api.src.auth;

import com.inspiretmstech.api.src.auth.bearer.APIKey;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.exceptions.InsufficientPrivilegesException;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.RolesRecord;
import com.inspiretmstech.db.tables.records.UsersRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
public class RequiresProcessor {

    private void fail(Scopes scope) {
        throw new InsufficientPrivilegesException(scope);
    }

    private void test(Scopes scope, boolean predicate) {
        if (!predicate) this.fail(scope);
    }

    public void requires(Scopes scope) throws SQLException {

        APIKey key = (APIKey) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // load the user
        Optional<UsersRecord> user = PostgresConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.USERS).where(Tables.USERS.ID.eq(key.getSub())).fetchOne());
        if (user.isEmpty())
            throw new ResponseException("Unable to Load User", "The server was unable to load a user object for the current user");
        if (Objects.isNull(user.get().getRoleId())) this.fail(scope);

        // load the role
        Optional<RolesRecord> role = PostgresConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.ROLES).where(Tables.ROLES.ID.eq(user.get().getRoleId())).fetchOne());
        if (role.isEmpty())
            throw new ResponseException("Unable to Load Role", "The server was unable to load a role for the current user");

        // check the scope
        switch (scope) {
            case FACILITIES -> this.test(scope, role.get().getScopeFacilities());
            case CARRIERS -> this.test(scope, role.get().getScopeCarriers());
            case CUSTOMERS -> this.test(scope, role.get().getScopeCustomers());
            case OPERATIONS -> this.test(scope, role.get().getScopeOperations());
            case NEVER -> this.test(scope, false); // NEVER is always false
            default ->
                    throw new ResponseException("Unhandled Scope", "The server is improperly configured to handle scope " + scope, "Contact Inspire TMS Support to resolve this issue");
        }
    }

    @Around("@annotation(Requires)")
    public Object process(ProceedingJoinPoint joinPoint, Requires Requires) throws Throwable {
        for (Scopes scopes : Requires.value())
            this.requires(scopes);
        return joinPoint.proceed();
    }

}
