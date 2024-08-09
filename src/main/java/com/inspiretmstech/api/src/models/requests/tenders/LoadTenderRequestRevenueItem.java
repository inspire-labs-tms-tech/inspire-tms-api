package com.inspiretmstech.api.src.models.requests.tenders;

import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestRevenueItem(
        @NotNull Integer quantity,
        @NotNull Float rate
) {
}
