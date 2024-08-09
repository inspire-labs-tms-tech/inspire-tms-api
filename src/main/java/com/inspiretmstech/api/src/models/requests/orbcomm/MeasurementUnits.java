package com.inspiretmstech.api.src.models.requests.orbcomm;

public record MeasurementUnits(
        String dateFormat,
        String distanceUOM,
        String fuelUOM,
        String pressureUOM,
        String temperatureUOM,
        String timeDuration,
        String weightUOM
) {}
