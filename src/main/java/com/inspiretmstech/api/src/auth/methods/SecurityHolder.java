package com.inspiretmstech.api.src.auth.methods;

import com.inspiretmstech.api.src.auth.AuthenticatedAuthenticationToken;
import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityHolder<T extends AuthenticationHolder> {

    private final T object;

    public SecurityHolder(T object) {
        this.object = object;
    }

    private static AuthenticationHolder getAuthenticationHolder() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (AuthenticationHolder.class.isAssignableFrom(principal.getClass())) return (AuthenticationHolder) principal;
        throw new RuntimeException("Security Holder type is not inherited from " + AuthenticationHolder.class.getName() + " ('" + principal.getClass() + "')");
    }

    public static <V extends AuthenticationHolder> boolean isAuthenticationHolder(Class<V> toCls) {
        return toCls.isInstance(SecurityHolder.getAuthenticationHolder());
    }

    public static <V extends AuthenticationHolder> V getAuthenticationHolder(Class<V> toCls) {
        if (SecurityHolder.isAuthenticationHolder(toCls)) return (V) SecurityHolder.getAuthenticationHolder();
        throw new RuntimeException("'" + SecurityHolder.getAuthenticationHolder().getClass().getName() + "' is not an instance of " + toCls.getName());
    }

    public Authentication getAuthentication() {
        Authentication token = new AuthenticatedAuthenticationToken(this.object, this.object.getEncryptedSecret(), this.object.getAuthorities());
        token.setAuthenticated(true);
        return token;
    }

}
