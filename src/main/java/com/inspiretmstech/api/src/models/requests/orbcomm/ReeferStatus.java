package com.inspiretmstech.api.src.models.requests.orbcomm;

import java.util.List;

public record ReeferStatus(
        List<Object> activeAlarms, // Assuming activeAlarms is an array of objects, adjust if necessary
        ReeferSensorData otherSensorData,
        List<Object> profileList, // Assuming profileList is an array of objects, adjust if necessary
        String tractorId
) {}
