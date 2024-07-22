package com.inspiretmstech.api.models.responses;

import jakarta.validation.constraints.NotNull;

public record StatusResponse(@NotNull String status) {
}
