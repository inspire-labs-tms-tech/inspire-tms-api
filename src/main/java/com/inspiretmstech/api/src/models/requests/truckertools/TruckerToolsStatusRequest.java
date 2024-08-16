package com.inspiretmstech.api.src.models.requests.truckertools;

public record TruckerToolsStatusRequest(
        int partnerId,
        String accountId,
        int loadTrackId,
        String loadTrackExternalId,
        String carrierExternalId,
        String driverPhone,
        String loadNumber,
        String eventType,
        LatestLocation latestLocation,
        LatestStatus latestStatus,
        Status status
) {
    public record LatestLocation(
            String lat,
            String lon,
            double accuracy,
            String timestamp,
            String timestampSec,
            String timestampUTC,
            String city,
            String state,
            String country
    ) {}

    public record LatestStatus(
            int id,
            Location location,
            String timestamp,
            String timestampSec,
            String timestampUTC,
            String name,
            String code,
            String stopExternalId,
            int stopOrderNumber
    ) {}

    public record Status(
            Location location,
            String timestamp,
            String timestampSec,
            String timestampUTC,
            String name,
            String code,
            String stopExternalId,
            int stopOrderNumber
    ) {}

    public record Location(
            String lat,
            String lon,
            double accuracy
    ) {}
}
