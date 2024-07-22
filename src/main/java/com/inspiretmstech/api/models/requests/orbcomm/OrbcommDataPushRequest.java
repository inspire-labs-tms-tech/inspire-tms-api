package com.inspiretmstech.api.models.requests.orbcomm;

import java.util.List;

public record OrbcommDataPushRequest(
        int code,
        String message,
        boolean exception,
        List<Data> data,
        MeasurementUnits measurementUnits
) {}

