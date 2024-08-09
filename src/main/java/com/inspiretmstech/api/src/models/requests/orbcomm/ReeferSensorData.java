package com.inspiretmstech.api.src.models.requests.orbcomm;

import java.util.List;

public record ReeferSensorData(
        String category,
        List<ReeferSensor> sensors
) {}
