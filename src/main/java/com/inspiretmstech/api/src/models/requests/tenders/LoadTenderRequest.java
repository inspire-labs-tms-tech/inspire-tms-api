package com.inspiretmstech.api.src.models.requests.tenders;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LoadTenderRequest(
        @NotNull LoadTenderRequestReplyTo replyTo,

        @Schema(description = "An immutable reference for the order in the sending-party's system, usable in future transactions")
        @NotNull String uniqueReferenceID,

        @Schema(description = "A mutable, human-readable reference number that can be updated in future tenders of the same uniqueReferenceID")
        @Nullable String reference,

        @Nullable List<String> notes,

        @Nullable List<LoadTenderRequestRevenueItem> revenue,

        @NotNull List<LoadTenderRequestStop> stops
) {
}
