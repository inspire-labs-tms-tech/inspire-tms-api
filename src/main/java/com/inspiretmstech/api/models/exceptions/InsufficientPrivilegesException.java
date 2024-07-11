package com.inspiretmstech.api.models.exceptions;

import com.inspiretmstech.api.auth.Scopes;
import com.inspiretmstech.api.models.ResponseException;

public class InsufficientPrivilegesException extends ResponseException {

    public InsufficientPrivilegesException(Scopes requiredScope) {
        super("Insufficient Privileges", "Authenticated user does not have sufficient privileges to perform this action", "This operation requires the " + requiredScope.name() + " scope, but the authenticated user does not have sufficient privileges.");
    }

}
