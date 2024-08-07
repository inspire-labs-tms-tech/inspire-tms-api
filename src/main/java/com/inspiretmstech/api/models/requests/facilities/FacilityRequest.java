package com.inspiretmstech.api.models.requests.facilities;

import com.inspiretmstech.api.models.Address;
import com.inspiretmstech.db.udt.records.AddressRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FacilityRequest(
        @Schema(description = "The display name to use for the facility")
        @NotNull String displayName,

        @Schema(description = "Whether to mark the facility as active")
        @NotNull boolean isActive,

        @Schema(description = "The location of the facility (must specify address or fullyQualifiedAddress)")
        Address address,

        @Schema(description = "The fully-qualified location of the facility (must specify address or fullyQualifiedAddress)")
        AddressRecord fullyQualifiedAddress,

        @Schema(description = "(optional) External ID of the facility")
        String externalID
) {
}
