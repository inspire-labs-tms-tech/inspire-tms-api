package com.inspiretmstech.api.models.requests.orbcomm;

public record Data(
        AssetStatus assetStatus,
        GenericSensors genericSensors,
        PositionStatus positionStatus,
        ReeferStatus reeferStatus
) {}
