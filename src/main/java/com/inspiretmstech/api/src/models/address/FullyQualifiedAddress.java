package com.inspiretmstech.api.src.models.address;

import com.inspiretmstech.db.udt.records.AddressRecord;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FullyQualifiedAddress(
        @NotNull String streetAddress1,
        String streetAddress2,
        @NotNull String city,
        @NotNull String state,
        @NotNull String zip,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        @NotNull String ianaTimeZoneID
) implements AddressObjectModel {

    @Override
    public AddressRecord build() {
        AddressRecord address = new AddressRecord();
        address.setStreetAddress_1(streetAddress1);
        address.setStreetAddress_2(streetAddress2);
        address.setCity(city);
        address.setState(state);
        address.setZip(zip);
        address.setLatitude(latitude);
        address.setLongitude(longitude);
        address.setIanaTimezoneId(ianaTimeZoneID);
        return address;
    }

}
