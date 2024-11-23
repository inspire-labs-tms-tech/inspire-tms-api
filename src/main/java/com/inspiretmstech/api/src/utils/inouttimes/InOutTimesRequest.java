package com.inspiretmstech.api.src.utils.inouttimes;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record InOutTimesRequest(
        @NotNull UUID orderID,
        @NotNull Long stopNumber,
        @NotNull String at // ISO 8601
) {
}
