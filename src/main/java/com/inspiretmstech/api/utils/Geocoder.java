package com.inspiretmstech.api.utils;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.inspiretmstech.api.constants.Environment;
import com.inspiretmstech.api.models.ResponseException;
import com.inspiretmstech.db.udt.records.AddressRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

public class Geocoder {

    private static final Logger logger = LoggerFactory.getLogger(Geocoder.class);
    private static GeoApiContext context;

    private static <T> T lookup(String address, Executor<T> executor) {
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(getContext()).address(address).await();
            if (Objects.isNull(results) || results.length == 0)
                throw new ResponseException("Geocoding Error", "Failed to Geocode Address: " + address, "Is this a valid address?");
            return executor.execute(results[0]);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Geocoding Error: {}", e.getMessage());
            throw new ResponseException("Unexpected Geocoding Error", "Failed to Geocode Address: " + address, "Is this a valid address?");
        }
    }

    private static synchronized GeoApiContext getContext() {
        if (context == null)
            context = new GeoApiContext.Builder().apiKey(Environment.get(Environment.Variables.GOOGLE_GEOCODING_API_KEY)).build();
        return context;
    }

    public static AddressRecord reverseGeocode(String address) {
        return Geocoder.reverseGeocode(address, true);
    }

    public static AddressRecord reverseGeocode(String address, boolean validate) {
        return Geocoder.lookup(address, (result) -> {
            AddressRecord record = new AddressRecord();

            LatLng coords = Geocoder.geocode(result);
            ZoneId ianaTimezoneID = TimeZones.lookup(coords, address);
            Optional<String> streetNumber = Optional.empty();
            Optional<String> streetName = Optional.empty();
            Optional<String> streetAddress2 = Optional.empty();
            Optional<String> city = Optional.empty();
            Optional<String> state = Optional.empty();
            Optional<String> postalCode = Optional.empty();

            for (AddressComponent component : result.addressComponents) {
                switch (component.types[0]) {
                    case STREET_NUMBER -> streetNumber = Optional.ofNullable(component.longName);
                    case ROUTE -> streetName = Optional.ofNullable(component.longName);
                    case SUBPREMISE -> streetAddress2 = Optional.ofNullable(component.longName);
                    case LOCALITY -> city = Optional.ofNullable(component.longName);
                    case ADMINISTRATIVE_AREA_LEVEL_1 -> state = Optional.ofNullable(component.longName);
                    case POSTAL_CODE -> postalCode = Optional.ofNullable(component.longName);
                }

                if (streetNumber.isEmpty() && validate)
                    throw new ResponseException("Unable to determine street number for address: " + address, "Is this a valid street number?");
                if (streetName.isEmpty() && validate)
                    throw new ResponseException("Unable to determine street name for address: " + address, "Is this a valid street name?");
                if (streetAddress2.isEmpty()) streetAddress2 = Optional.of("");
                if (city.isEmpty() && validate)
                    throw new ResponseException("Unable to determine city for address: " + address, "Is this a valid city?");
                if (state.isEmpty() && validate)
                    throw new ResponseException("Unable to determine state for address: " + address, "Is this a valid state?");
                if (postalCode.isEmpty() && validate)
                    throw new ResponseException("Unable to determine postal code for address: " + address, "Is this a valid postal code?");
            }

            record.setStreetAddress_1((streetNumber.orElse("") + " " + streetName.orElse("")).trim());
            record.setStreetAddress_2(streetAddress2.orElse(""));
            record.setCity(city.orElse(""));
            record.setState(state.orElse(""));
            record.setZip(postalCode.orElse(""));
            record.setLatitude(BigDecimal.valueOf(coords.lat));
            record.setLongitude(BigDecimal.valueOf(coords.lng));
            record.setIanaTimezoneId(ianaTimezoneID.getId());

            return record;
        });
    }

    private static LatLng geocode(GeocodingResult result) {
        LatLng cords = result.geometry.location;
        if (Objects.isNull(cords))
            throw new ResponseException("Geocoding Error", "Coordinates returned are null for address: " + result.formattedAddress, "Is this a valid address?");
        return result.geometry.location;
    }

    public static LatLng geocode(String address) {
        return Geocoder.lookup(address, Geocoder::geocode);
    }

    @FunctionalInterface
    interface Executor<T> {
        T execute(GeocodingResult result) throws Exception;
    }

}
