package com.inspiretmstech.api.src.models.requests.orbcomm;

public record ReeferSensor(
        String sensorLabel,
        String sensorDataElement,
        String sensorNumber,
        String sensorValue
) {}
