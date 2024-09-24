package com.inspiretmstech.api.src.auth.methods.apikey.anon;

import com.inspiretmstech.api.src.auth.methods.abc.AuthenticationProvider;
import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AnonymousAuthenticationProvider implements AuthenticationProvider<AnonymousAuthenticationHolder> {

    @Override
    public boolean supports(@NotNull HttpServletRequest request) {
        return true;
    }

    @Override
    public SecurityHolder<AnonymousAuthenticationHolder> authenticate(@NotNull HttpServletRequest request) {
        return new SecurityHolder<>(new AnonymousAuthenticationHolder());
    }

}
