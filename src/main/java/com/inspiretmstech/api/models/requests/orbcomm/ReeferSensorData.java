package com.inspiretmstech.api.models.requests.orbcomm;

import java.util.List;

public record ReeferSensorData(
        String category,
        List<ReeferSensor> sensors
) {}
