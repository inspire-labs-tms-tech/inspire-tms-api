package com.inspiretmstech.api.utils;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.inspiretmstech.api.constants.Environment;
import com.inspiretmstech.api.models.Address;
import com.inspiretmstech.api.models.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class Geocoder {

    private static GeoApiContext context;
    private static final Logger logger = LoggerFactory.getLogger(Geocoder.class);

    private static synchronized GeoApiContext getContext() {
        if (context == null)
            context = new GeoApiContext.Builder().apiKey(Environment.get(Environment.Variables.GOOGLE_GEOCODING_API_KEY)).build();
        return context;
    }

    public static LatLng geocode(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(getContext()).address(address).await();
            if (Objects.isNull(results) || results.length == 0)
                throw new ResponseException("Geocoding Error", "Failed to Geocode Address: " + address, "Is this a valid address?");
            GeocodingResult result = results[0];
            LatLng cords = result.geometry.location;
            if(Objects.isNull(cords)) throw new ResponseException("Geocoding Error", "Coordinates returned are null for address: " + address, "Is this a valid address?");
            return result.geometry.location;
        } catch (Exception e) {
            logger.error("Geocoding Error: {}", e.getMessage());
            throw new ResponseException("Unexpected Geocoding Error", "Failed to Geocode Address: " + address, "Is this a valid address?");
        }
    }

}
