package com.inspiretmstech.api.src.models.requests.orders;

import org.jetbrains.annotations.NotNull;

public record SubmitOrderTimeRequest(
        @NotNull String timestamp,
        @NotNull Boolean saveToDatabase
) {
}
