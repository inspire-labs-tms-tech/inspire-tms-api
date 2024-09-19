package com.inspiretmstech.api.src.models.requests.timezone;

import com.google.maps.model.LatLng;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TimeZoneLookupRequest(
        @NotNull BigDecimal lat,
        @NotNull BigDecimal lng
) {

    public LatLng toLatLng() {
        return new LatLng(this.lat.doubleValue(), this.lng.doubleValue());
    }

    @Override
    public String toString() {
        return "TimeZoneLookupRequest{" +
               "lat=" + lat +
               ", lng=" + lng +
               '}';
    }
}
