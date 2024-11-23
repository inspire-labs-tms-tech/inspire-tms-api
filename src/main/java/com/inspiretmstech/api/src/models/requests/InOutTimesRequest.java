package com.inspiretmstech.api.src.models.requests;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record InOutTimesRequest(
        @NotNull UUID orderID,
        @NotNull Long stopNumber,
        @NotNull String at // ISO 8601
) {
}
