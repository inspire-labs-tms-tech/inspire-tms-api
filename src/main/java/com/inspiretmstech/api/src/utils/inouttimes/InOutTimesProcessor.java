package com.inspiretmstech.api.src.utils.inouttimes;

import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.utils.WithLogger;
import com.inspiretmstech.api.src.utils.inouttimes.processors.*;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.tables.records.OrdersRecord;

import java.sql.SQLException;
import java.util.*;

public class InOutTimesProcessor extends WithLogger {

    private List<TimeProcessor> processors;

    private static final List<TimeProcessor> DEFAULT_PROCESSORS = Arrays.asList(
            new SaveToDatabaseProcessor(),
            new AfterShipProcessor(),
            new PrincetonTMXProcessor(),
            new DSGProcessor()
    );

    public InOutTimesProcessor(List<TimeProcessor> processors) {
        super(InOutTimesProcessor.class);
        this.processors = processors;
    }

    public InOutTimesProcessor() {
        super(InOutTimesProcessor.class);
        this.processors = DEFAULT_PROCESSORS;
    }

    public void remove(Class<? extends TimeProcessor> processorClass) {
        this.processors = this.processors.stream()
                .filter(p -> !processorClass.isInstance(p))
                .toList();
    }

    public void arrived(InOutTimesRequest request) {
        this.process(Processes.ARRIVED, request);
    }

    public void departed(InOutTimesRequest request) {
        this.process(Processes.DEPARTED, request);
    }

    private void process(Processes type, InOutTimesRequest request) {
        this.logger.info("processing {} time ({} active processors)", type, this.processors.size());

        try {
            OrdersRecord order = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.ORDERS).where(Tables.ORDERS.ID.eq(request.orderID())).fetchOne()).orElse(null);
            if(Objects.isNull(order)) {
                logger.error("unable to load order");
                throw new ResponseException("Unable to load Order");
            }

            LoadTendersRecord tender = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.LOAD_TENDERS).where(Tables.LOAD_TENDERS.ORDER_ID.eq(request.orderID())).fetchOne()).orElse(null);
            if(Objects.isNull(tender)) logger.debug("no load tender for order {}", order.getId());

            List<TimeProcessor> processors = this.processors.stream().filter(p -> p.supports(Objects.isNull(tender) ? null : tender.getIntegrationType())).toList();
            for (TimeProcessor processor : processors) {
                try {
                    this.logger.trace("processing {}", processor.getClass().getSimpleName());
                    processor.process(type, new InOutTimes(order, request.stopNumber(), request.at()));
                    this.logger.trace("processed {}", processor.getClass().getSimpleName());
                } catch (Exception e) {
                    this.logger.error("an unhandled exception occurred while processing {} time using {}: {}", type, processor.getClass().getSimpleName(), e.getMessage());
                }
            } // end for-loop
        } catch (SQLException e) {
            logger.error("a SQL exception occurred: {}", e.getMessage());
        }
    }

}
