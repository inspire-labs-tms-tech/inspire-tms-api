package com.inspiretmstech.api.src.models.responses.about;

import jakarta.validation.constraints.NotNull;

public record AboutResponse(
        @NotNull String version,
        @NotNull AboutCompanyResponse company,
        @NotNull AboutSupabaseResponse supabase
) {
}
