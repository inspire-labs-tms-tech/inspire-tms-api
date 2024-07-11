package com.inspiretmstech.api.models;

import com.google.maps.model.LatLng;
import com.inspiretmstech.api.utils.Geocoder;
import com.inspiretmstech.api.utils.TimeZones;
import com.inspiretmstech.db.udt.records.AddressRecord;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Objects;

public record Address(
        @NotNull String line1,
        @Nullable String line2,
        @NotNull String city,
        @NotNull String state,
        @NotNull String zipCode
) {

    @Override
    public String toString() {
        String address = "";
        address += line1 + ", ";
        if(Objects.nonNull(line2)) address += line2 + ", ";
        address += city + ", " + state + " " + zipCode;

        return address;
    }

    public AddressRecord toAddress() {
        LatLng coords = Geocoder.geocode(this.toString());
        ZoneId zone = TimeZones.lookup(coords, this);
        return new AddressRecord(
                this.line1,
                this.line2,
                this.city,
                this.state,
                this.zipCode,
                BigDecimal.valueOf(coords.lat),
                BigDecimal.valueOf(coords.lng),
                zone.getId()
        );
    }

}
