package com.inspiretmstech.api.models.requests;

import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestAppointment(
        @NotNull String earliest,
        @NotNull String latest
) {
}
