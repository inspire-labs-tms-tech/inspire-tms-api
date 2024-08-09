package com.inspiretmstech.api.src.models.responses;

import jakarta.validation.constraints.NotNull;

public record VersionResponse(@NotNull String version) {
}
