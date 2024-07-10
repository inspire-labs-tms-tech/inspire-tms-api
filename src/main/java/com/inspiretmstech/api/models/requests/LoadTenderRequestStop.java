package com.inspiretmstech.api.models.requests;

import com.inspiretmstech.api.models.Address;
import com.inspiretmstech.db.enums.StopTypes;
import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestStop(
        @NotNull String uniqueReferenceID,
        @NotNull StopTypes type,
        @NotNull Address address,
        @NotNull LoadTenderRequestAppointment appointment
) {
}
