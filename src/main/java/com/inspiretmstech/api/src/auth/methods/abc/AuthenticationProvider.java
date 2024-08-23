package com.inspiretmstech.api.src.auth.methods.abc;

import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;

public interface AuthenticationProvider<T extends AuthenticationHolder> {

    boolean supports(@NotNull HttpServletRequest request);

    SecurityHolder<T> authenticate(@NotNull HttpServletRequest request);

}
