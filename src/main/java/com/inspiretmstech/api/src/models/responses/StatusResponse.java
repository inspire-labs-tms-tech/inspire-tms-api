package com.inspiretmstech.api.src.models.responses;

import jakarta.validation.constraints.NotNull;

public record StatusResponse(@NotNull String status) {

    public static StatusResponse ACCEPTED() {
        return new StatusResponse("ACCEPTED");
    }

}
