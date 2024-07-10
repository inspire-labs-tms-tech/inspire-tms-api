package com.inspiretmstech.api.models.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestReplyTo(

        @Schema(description = "A HTTPS URL where a POST request can be made to accept the load tender (must return a 2XX status code)")
        @NotNull String accept,

        @Schema(description = "A HTTPS URL where a POST request can be made to decline the load tender (must return a 2XX status code)")
        @NotNull String decline
) {
}
