package com.inspiretmstech.api.models.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LoadTenderRequest(
        @NotNull LoadTenderRequestReplyTo replyTo,
        @NotNull String uniqueReferenceID, // an immutable reference for the order in the sending-party's system
        @Nullable String reference, // a mutable, human-readable reference number
        @Nullable List<String> notes,
        @Nullable List<LoadTenderRequestRevenueItem> revenue,
        @NotNull List<LoadTenderRequestStop> stops
) {
}
