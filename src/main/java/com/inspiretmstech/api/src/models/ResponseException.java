package com.inspiretmstech.api.src.models;

import com.google.gson.Gson;
import com.inspiretmstech.api.src.models.responses.ErrorResponse;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class ResponseException extends RuntimeException {

    @Nullable
    private String description = null;

    @Nullable
    private String hint = null;

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(String message, @NotNull String description) {
        super(message);
        this.description = description;
    }

    public ResponseException(String message, @Nullable String description, @Nullable String hint) {
        super(message);
        this.description = description;
        this.hint = hint;
    }

    public ResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.getMessage(), this.getDescription(), this.getHint());
    }

    public void respondWith(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        Gson gson = new Gson();
        String json = gson.toJson(this.getErrorResponse());

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
