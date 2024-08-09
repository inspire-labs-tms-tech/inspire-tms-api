package com.inspiretmstech.api.src.models.responses.fmcsa;

// Root class
public record FMCSAResult(
        Links _links,
        Carrier carrier
) {}

