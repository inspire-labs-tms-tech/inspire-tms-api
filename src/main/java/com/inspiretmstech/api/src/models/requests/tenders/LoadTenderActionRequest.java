package com.inspiretmstech.api.src.models.requests.tenders;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

public record LoadTenderActionRequest(
        @Schema(description = "Whether the tender is accepted")
        @NotNull Boolean accept,

        @Schema(description = "Whether to update the order (silent will send a response without acting on the order)")
        @NotNull Boolean silent
) {
}
