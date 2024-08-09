package com.inspiretmstech.api.src.models.responses;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IDResponse(@NotNull String id) {

    public static IDResponse from(@NotNull UUID uuid) {
        return new IDResponse(uuid.toString());
    }

}
