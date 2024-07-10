package com.inspiretmstech.api.models.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LoadTenderRequest(
        @NotNull LoadTenderRequestReplyTo replyTo,
        @NotNull String uniqueReferenceID,
        @Nullable List<String> notes,
        @Nullable List<LoadTenderRequestRevenueItem> revenue,
        @NotNull List<LoadTenderRequestStop> stops
) {
}
