package com.inspiretmstech.api.utils;

import com.google.maps.model.LatLng;
import com.inspiretmstech.api.models.Address;
import com.inspiretmstech.api.models.ResponseException;
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

    public static ZoneId lookup(LatLng coords, Address address) {
        return TimeZones.lookup(coords, address.toString());
    }

    public static ZoneId lookup(LatLng coords, String address) {
        Optional<ZoneId> result = getEngine().query(coords.lat, coords.lng);
        if(result.isEmpty()) throw new ResponseException("Unable to Determine Time Zone", "Unable to determine time zone for address: " + address);
        return result.get();
    }

}
