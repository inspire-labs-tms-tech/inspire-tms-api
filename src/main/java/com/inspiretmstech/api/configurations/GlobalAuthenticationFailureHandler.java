package com.inspiretmstech.api.configurations;

import com.google.gson.Gson;
import com.inspiretmstech.api.models.responses.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class GlobalAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        ErrorResponse r = new ErrorResponse(exception.getMessage(), "An authorization error occurred: " + exception.getMessage(), "");

        Gson gson = new Gson();
        String json = gson.toJson(r);

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
