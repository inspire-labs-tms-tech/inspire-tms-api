package com.inspiretmstech.api.src.models.requests.orbcomm;

public record Data(
        AssetStatus assetStatus,
        GenericSensors genericSensors,
        PositionStatus positionStatus,
        ReeferStatus reeferStatus
) {}
