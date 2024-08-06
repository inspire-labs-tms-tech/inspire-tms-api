package com.inspiretmstech.api.models.requests.facilities;

import com.inspiretmstech.api.models.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FacilityRequest(
        @Schema(description = "The display name to use for the facility")
        @NotNull String displayName,

        @Schema(description = "Whether to mark the facility as active")
        @NotNull boolean isActive,

        @Schema(description = "The location of the facility")
        @NotNull Address address,

        @Schema(description = "(optional) External ID of the facility")
        String externalID
) {
}
