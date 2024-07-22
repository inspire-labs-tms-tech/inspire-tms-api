package com.inspiretmstech.api.models.requests.orbcomm;

import java.util.List;

public record PositionStatus(
        String address,
        List<Object> geofenceDetails, // Assuming geofenceDetails is an array of objects, adjust if necessary
        double latitude,
        double longitude
) {}
