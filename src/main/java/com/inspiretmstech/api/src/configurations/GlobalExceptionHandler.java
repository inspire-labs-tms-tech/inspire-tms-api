package com.inspiretmstech.api.src.configurations;

import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.exceptions.InsufficientPrivilegesException;
import com.inspiretmstech.api.src.models.responses.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    public final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({RuntimeException.class, SQLException.class, ResponseException.class})
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        logger.trace("A Global Exception Has Occurred: {}", e.getMessage());

        if (e.getClass() == InsufficientPrivilegesException.class)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(((InsufficientPrivilegesException) e).getErrorResponse());

        if (e.getClass() == ResponseException.class)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(((ResponseException) e).getErrorResponse());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage(), "An unexpected server error occurred: " + e.getMessage(), "N/A"));
    }
}
