package com.inspiretmstech.api.models.requests;

import com.inspiretmstech.api.models.Address;
import com.inspiretmstech.db.enums.StopTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestStop(

        @Schema(description = "A unique reference to the specific stop in the sending party's system (if no such ID exists, use the stop-number)")
        @NotNull String uniqueReferenceID,

        @Schema(description = "The type of stop")
        @NotNull StopTypes type,

        @Schema(description = "The location of the stop")
        @NotNull Address address,

        @NotNull LoadTenderRequestAppointment appointment
) {
}
