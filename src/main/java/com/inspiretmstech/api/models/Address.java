package com.inspiretmstech.api.models;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record Address(
        @NotNull String line1,
        @Nullable String line2,
        @NotNull String city,
        @NotNull String state,
        @NotNull String zipCode
) {
}
