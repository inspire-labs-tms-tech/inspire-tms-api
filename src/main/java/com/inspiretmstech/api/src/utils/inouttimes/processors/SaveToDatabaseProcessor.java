package com.inspiretmstech.api.src.utils.inouttimes.processors;

import com.inspiretmstech.api.src.utils.Executor;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimes;
import com.inspiretmstech.api.src.utils.inouttimes.TimeProcessor;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.tables.records.StopsRecord;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

public class SaveToDatabaseProcessor extends TimeProcessor {

    public SaveToDatabaseProcessor() {
        super(SaveToDatabaseProcessor.class);
    }

    @Override
    protected boolean supports(@Nullable IntegrationTypes type) {
        return true;
    }

    /**
     * Set the arrival time for a stop in the database
     *
     * @return the callback to set the arrival time
     */
    protected Executor<InOutTimes> getArrivalProcessor() {
        return (request) -> {
            Optional<StopsRecord> stop = PostgresConnection.getInstance().with(supabase ->
                    supabase.update(Tables.STOPS)
                            .set(Tables.STOPS.DRIVER_ARRIVED_AT, OffsetDateTime.now())
                            .where(Tables.STOPS.ORDER_ID.eq(request.order().getId())
                                    .and(Tables.STOPS.STOP_NUMBER.eq(request.stopNumber())))
                            .returning()
                            .fetchOne()
            );

            if (stop.isEmpty()) {
                throw new NullPointerException("Unable to Locate Stop to Update");
            } else if (Objects.isNull(stop.get().getDriverArrivedAt())) {
                throw new NullPointerException("Unable to Update Arrival Time");
            }
        };
    }

    /**
     * Set the departure time for a stop in the database
     *
     * @return the callback to set the departure time
     */
    protected Executor<InOutTimes> getDepartureProcessor() {
        return (request) -> {
            Optional<StopsRecord> stop = PostgresConnection.getInstance().with(supabase ->
                    supabase.update(Tables.STOPS)
                            .set(Tables.STOPS.DRIVER_DEPARTED_AT, OffsetDateTime.now())
                            .where(Tables.STOPS.ORDER_ID.eq(request.order().getId())
                                    .and(Tables.STOPS.STOP_NUMBER.eq(request.stopNumber())))
                            .returning()
                            .fetchOne()
            );

            if (stop.isEmpty()) {
                throw new NullPointerException("Unable to Locate Stop to Update");
            } else if (Objects.isNull(stop.get().getDriverDepartedAt())) {
                throw new NullPointerException("Unable to Update Departure Time");
            }
        };
    }
}
