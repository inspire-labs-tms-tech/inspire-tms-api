package com.inspiretmstech.api.utils;

import com.google.maps.model.LatLng;
import net.iakovlev.timeshape.TimeZoneEngine;

import java.time.ZoneId;
import java.util.Optional;

public class TimeZones {

    private static TimeZoneEngine engine;

    private static synchronized TimeZoneEngine getEngine() {
        if (engine == null)
            engine = TimeZoneEngine.initialize();
        return engine;
    }

    public static Optional<ZoneId> lookup(LatLng coords) {
        return getEngine().query(coords.lat, coords.lng);
    }

}
