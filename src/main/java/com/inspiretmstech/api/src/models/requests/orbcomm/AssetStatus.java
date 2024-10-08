package com.inspiretmstech.api.src.models.requests.orbcomm;

public record AssetStatus(
        String assetName,
        String assetType,
        String deviceSN,
        String eventHasCurrentGPS,
        String messageReceivedStamp,
        String messageStamp,
        String messageType,
        String productType
) {}
