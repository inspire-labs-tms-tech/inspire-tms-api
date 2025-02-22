package com.inspiretmstech.api.src.utils.inouttimes;

import com.inspiretmstech.db.tables.records.OrdersRecord;
import org.jetbrains.annotations.NotNull;

public record InOutTimes(
        @NotNull OrdersRecord order,
        @NotNull Long stopNumber,
        @NotNull String at // ISO 8601
) {
}
