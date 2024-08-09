package com.inspiretmstech.api.src.models.requests.truckertools;

public record TruckerToolsCommentsRequest(
        int partnerId,
        String accountId,
        int loadTracklId,
        int loadTrackExternalId,
        String ltExternalId,
        String carrierExternalId,
        String driverPhone,
        String loadNumber,
        String eventType,
        LatestLocation latestLocation,
        LatestStatus latestStatus,
        Comments comments
) {
    public record LatestLocation(
            String lat,
            String lon,
            double accuracy,
            String timestampSec,
            String timestamp,
            String timestampUTC
    ) {}

    public record LatestStatus(
            String name,
            String code,
            String timestamp,
            String timestampSec,
            String timestampUTC
    ) {}

    public record Comments(
            String comment,
            String commentBy,
            Location location,
            String timestamp,
            String timestampSec,
            String timestampUTC,
            String stopExternalId
    ) {}

    public record Location(
            String lat,
            String lon,
            double accuracy,
            String city,
            String state,
            String country
    ) {}
}
