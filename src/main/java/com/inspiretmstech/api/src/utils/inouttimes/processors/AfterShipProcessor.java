package com.inspiretmstech.api.src.utils.inouttimes.processors;

import com.inspiretmstech.api.src.utils.AfterShipTrackingUtility;
import com.inspiretmstech.api.src.utils.Executor;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimes;
import com.inspiretmstech.api.src.utils.inouttimes.TimeProcessor;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.tables.records.CustomersRecord;
import com.inspiretmstech.db.tables.records.IntegrationsRecord;
import com.inspiretmstech.db.tables.records.OrdersRecord;
import com.inspiretmstech.db.tables.records.StopsRecord;

import java.net.http.HttpResponse;
import java.util.Optional;

public class AfterShipProcessor extends TimeProcessor {

    public AfterShipProcessor() {
        super(AfterShipProcessor.class);
    }

    private void handle(AfterShipTrackingUtility.AfterShipMessageType type, InOutTimes request) throws Exception {

        PostgresConnection conn = PostgresConnection.getInstance();

        Optional<IntegrationsRecord> aftership = conn.unsafely(supabase ->
                supabase.selectFrom(Tables.INTEGRATIONS)
                        .where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.AFTERSHIP))
                        .fetchOne());
        if (aftership.isEmpty()) {
            logger.debug("integration not found");
            return;
        }

        if (aftership.get().getAftershipSlug().isEmpty()) {
            logger.warn("integration found, but slug is empty");
            return;
        }

        // get the order
        Optional<OrdersRecord> order = conn.unsafely(supabase ->
                supabase.selectFrom(Tables.ORDERS)
                        .where(Tables.ORDERS.ID.eq(request.orderID()))
                        .fetchOne()
        );
        if (order.isEmpty()) {
            logger.error("unable to load order");
            return;
        }

        // get the customer
        Optional<CustomersRecord> customer = conn.unsafely(supabase ->
                supabase.selectFrom(Tables.CUSTOMERS)
                        .where(Tables.CUSTOMERS.ID.eq(order.get().getCustomerId()))
                        .fetchOne()
        );
        if (customer.isEmpty()) {
            logger.error("unable to load customer");
            return;
        }
        if(!customer.get().getIsAftershipCustomer()) {
            logger.debug("aftership not enabled for customer");
            return;
        }

        // get the origin
        Optional<StopsRecord> origin = conn.unsafely(supabase ->
                supabase.selectFrom(Tables.STOPS)
                        .where(Tables.STOPS.ORDER_ID.eq(order.get().getId()))
                        .and(Tables.STOPS.STOP_NUMBER.eq((long) 1))
                        .fetchOne()
        );
        if (origin.isEmpty()) {
            logger.error("unable to load origin stop");
            return;
        }

        // get the destination
        Optional<StopsRecord> destination = conn.unsafely(supabase ->
                supabase.selectFrom(Tables.STOPS)
                        .where(Tables.STOPS.ORDER_ID.eq(order.get().getId()))
                        .orderBy(Tables.STOPS.STOP_NUMBER.desc())
                        .limit(1)
                        .fetchOne()
        );
        if (destination.isEmpty()) {
            logger.error("unable to load destination stop");
            return;
        }

        // get the stop
        Optional<StopsRecord> stop = request.stopNumber().equals(origin.get().getStopNumber()) ? origin :
                request.stopNumber().equals(destination.get().getStopNumber()) ? destination :
                        conn.unsafely(supabase ->
                                supabase.selectFrom(Tables.STOPS)
                                        .where(Tables.STOPS.ORDER_ID.eq(order.get().getId()))
                                        .and(Tables.STOPS.STOP_NUMBER.eq(request.stopNumber()))
                                        .fetchOne()
                        );
        if (stop.isEmpty()) {
            logger.error("unable to load current stop");
            return;
        }

        HttpResponse<?> resp = AfterShipTrackingUtility.sendAfterShipTrackingUpdate(new AfterShipTrackingUtility.AfterShipTrackingUpdate(
                aftership.get().getAftershipSlug().trim(),
                new AfterShipTrackingUtility.AfterShipTrackingUpdate.AfterShipTrackingUpdateBasic(
                        customer.get(),
                        order.get(),
                        origin.get(),
                        destination.get()
                ),
                new AfterShipTrackingUtility.AfterShipTrackingUpdate.AfterShipTrackingUpdateDetails(
                        request.at(),
                        type,
                        stop.get()
                )
        ));

        if (resp.statusCode() != 200 && resp.statusCode() != 201) {
            logger.debug(resp.body().toString());
            logger.error("unable to send after ship tracking (status code {})", resp.statusCode());
            return;
        }

        // TODO: need to create/save the request and response as a (supabase) log to db
        logger.debug("successfully sent after ship tracking (status code {})", resp.statusCode());
    }

    protected Executor<InOutTimes> getArrivalProcessor() {
        return (request) -> this.handle(AfterShipTrackingUtility.AfterShipMessageType.ARRIVED, request);
    }

    protected Executor<InOutTimes> getDepartureProcessor() {
        return (request) -> this.handle(AfterShipTrackingUtility.AfterShipMessageType.DEPARTED, request);
    }
}
