package com.inspiretmstech.api.models.requests;

import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestReplyTo(
        @NotNull String accept,
        @NotNull String decline
) {
}
