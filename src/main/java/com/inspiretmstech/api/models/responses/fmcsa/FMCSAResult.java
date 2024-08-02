package com.inspiretmstech.api.models.responses.fmcsa;

import com.google.gson.annotations.SerializedName;

// Root class
public record FMCSAResult(
        Links _links,
        Carrier carrier
) {}

