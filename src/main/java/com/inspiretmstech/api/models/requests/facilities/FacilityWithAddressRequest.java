package com.inspiretmstech.api.models.requests.facilities;

import com.inspiretmstech.db.udt.records.AddressRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FacilityWithAddressRequest(
        @Schema(description = "The display name to use for the facility")
        @NotNull String displayName,

        @Schema(description = "Whether to mark the facility as active")
        @NotNull boolean isActive,

        @Schema(description = "The fully-qualified location of the facility")
        @NotNull AddressRecord address,

        @Schema(description = "(optional) External ID of the facility")
        String externalID
) {
}
