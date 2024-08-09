package com.inspiretmstech.api.src.models.requests.truckertools;

public record TruckerToolsLocationRequest(
        int partnerId,
        String accountId,
        int loadTrackId,
        int loadTrackExternalId,
        String ltExternalId,
        String carrierExternalId,
        String driverPhone,
        String loadNumber,
        String eventType,
        LatestStatus latestStatus,
        LatestLocation latestLocation
) {

    public record LatestStatus(
            String name,
            String code,
            String timestamp,
            String timestampSec,
            String timestampUTC
    ) {}

    public record LatestLocation(
            String lat,
            String lon,
            double accuracy,
            String timestampSec,
            String timestamp,
            String timestampUTC,
            String provider,
            String city,
            String state,
            String country
    ) {}

}
