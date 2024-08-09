package com.inspiretmstech.api.src.models.requests.orbcomm;

import java.util.List;

public record GenericSensorData(
        String category,
        List<GenericSensor> sensors
) {}
