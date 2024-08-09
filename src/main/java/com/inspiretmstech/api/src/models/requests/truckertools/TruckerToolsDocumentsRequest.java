package com.inspiretmstech.api.src.models.requests.truckertools;

public record TruckerToolsDocumentsRequest(
        int partnerId,
        String accountId,
        int loadTrackId,
        int loadTrackExternalId,
        String ltExternalId,
        String carrierExternalId,
        String driverPhone,
        String loadNumber,
        String eventType,
        LatestLocation latestLocation,
        LatestStatus latestStatus,
        Document document
) {
    public record LatestLocation(
            String lat,
            String lon,
            double accuracy,
            String timestampSec,
            String timestamp,
            String timestampUTC,
            String city,
            String state,
            String country
    ) {}

    public record LatestStatus(
            String name,
            String code,
            String timestamp,
            String timestampSec,
            String timestampUTC
    ) {}

    public record Document(
            String type,
            Location location,
            String timestamp,
            String url,
            String stopExternalId,
            int stopOrderNumber
    ) {}

    public record Location(
            String lat,
            String lon,
            String accuracy,
            String city,
            String state,
            String country
    ) {}
}
