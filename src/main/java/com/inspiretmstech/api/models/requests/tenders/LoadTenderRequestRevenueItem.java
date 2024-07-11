package com.inspiretmstech.api.models.requests.tenders;

import jakarta.validation.constraints.NotNull;

public record LoadTenderRequestRevenueItem(
        @NotNull Integer quantity,
        @NotNull Float rate
) {
}
