package com.inspiretmstech.api.models.requests.orbcomm;

public record ReeferSensor(
        String sensorLabel,
        String sensorDataElement,
        String sensorNumber,
        String sensorValue
) {}
