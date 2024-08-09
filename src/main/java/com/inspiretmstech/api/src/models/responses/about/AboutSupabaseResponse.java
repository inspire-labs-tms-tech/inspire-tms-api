package com.inspiretmstech.api.src.models.responses.about;

import jakarta.validation.constraints.NotNull;

public record AboutSupabaseResponse(@NotNull String url, @NotNull AboutSupabaseKeysResponse keys) {
}
