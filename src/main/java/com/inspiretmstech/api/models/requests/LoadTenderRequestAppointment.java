package com.inspiretmstech.api.models.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestAppointment(

        @Schema(description = "The earliest a driver can arrive at the stop")
        @NotNull String earliest,

        @Schema(description = "The latest a driver can arrive at the stop")
        @NotNull String latest
) {
}
