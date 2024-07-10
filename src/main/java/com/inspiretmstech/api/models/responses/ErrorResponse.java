package com.inspiretmstech.api.models.responses;

public record ErrorResponse(String error, String description, String hint) {
}
