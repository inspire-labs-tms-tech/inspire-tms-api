package com.inspiretmstech.api.models.responses.about;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record AboutCompanyResponse(@NotNull String name, @NotNull boolean published, @Nullable String logo) {
}
