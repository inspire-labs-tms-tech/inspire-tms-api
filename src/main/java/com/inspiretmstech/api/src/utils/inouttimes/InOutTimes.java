package com.inspiretmstech.api.src.utils.inouttimes;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.UUID;

public record InOutTimes(
        @NotNull UUID orderID,
        @NotNull Long stopNumber,
        @NotNull DateTime at
) {
}
