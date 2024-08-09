package com.inspiretmstech.api.src.models.responses;

public record ErrorResponse(String error, String description, String hint) {
}
