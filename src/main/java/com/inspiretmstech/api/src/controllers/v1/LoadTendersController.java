package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.Authority;
import com.inspiretmstech.api.src.auth.bearer.APIKey;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequest;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequestRevenueItem;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequestStop;
import com.inspiretmstech.api.src.models.responses.IDResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.LoadTenderVersionsRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.DSLContext;
import org.jooq.InsertResultStep;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Tag(name = "Load Tenders", description = "Customer endpoints for tendering loads directly to Inspire TMS")
@RequestMapping("/v1/load-tenders")
public class LoadTendersController extends Controller {

    public LoadTendersController() {
        super(LoadTendersController.class);
    }

    /**
     * Handle a load tender exception
     *
     * @param e the exception to handle
     */
    private void handle(Exception e) {
        if (e.getClass() == IntegrityConstraintViolationException.class) {
            IntegrityConstraintViolationException ex = (IntegrityConstraintViolationException) e;

            String search = "Detail: ";
            int index = ex.getMessage().indexOf(search);
            throw new ResponseException("Invalid Request", "Request could not be validated", index != -1 ? ex.getMessage().substring(index + search.length()) : null);
        }

        if (e.getClass() == ResponseException.class) throw (ResponseException) e;

        logger.error(e.getMessage());
        throw new ResponseException("Invalid Request", "Request could not be validated", "Unknown Exception");
    }

    /**
     * Create a fetchable LoadTenderVersionsRecord
     *
     * @param database the database object to build against
     * @param tenderID the ID of the LoadTendersRecord to search for
     * @param request  the details of the request
     * @return the fetchable LoadTenderVersionsRecord
     */
    private InsertResultStep<LoadTenderVersionsRecord> buildLoadTenderVersion(DSLContext database, UUID tenderID, LoadTenderRequest request) {

        ArrayList<LoadTenderStopRecord> stops = new ArrayList<>();
        ArrayList<LoadTenderRevenueItemRecord> revenue = new ArrayList<>();

        if (Objects.nonNull(request.revenue()))
            for (LoadTenderRequestRevenueItem item : request.revenue())
                revenue.add(new LoadTenderRevenueItemRecord(item.quantity(), BigDecimal.valueOf(item.rate())));

        for (LoadTenderRequestStop stop : request.stops()) {
            if (Objects.isNull(stop.appointment()))
                throw new ResponseException("Invalid Appointment", "Appointment cannot be empty");
            if (Objects.isNull(stop.appointment().earliest()))
                throw new ResponseException("Invalid Appointment", "The earliest appointment cannot be empty");
            if (Objects.isNull(stop.appointment().latest()))
                throw new ResponseException("Invalid Appointment", "The latest appointment cannot be empty");

            stops.add(new LoadTenderStopRecord(null, OffsetDateTime.parse(stop.appointment().earliest()), OffsetDateTime.parse(stop.appointment().latest()), stop.type(), stop.address().build()));
        }

        return database.insertInto(Tables.LOAD_TENDER_VERSIONS,
                Tables.LOAD_TENDER_VERSIONS.LOAD_TENDER_ID,
                Tables.LOAD_TENDER_VERSIONS.CUSTOMER_REFERENCE_NUMBER,
                Tables.LOAD_TENDER_VERSIONS.ACCEPT_WEBHOOK,
                Tables.LOAD_TENDER_VERSIONS.DECLINE_WEBHOOK,
                Tables.LOAD_TENDER_VERSIONS.STOPS,
                Tables.LOAD_TENDER_VERSIONS.REVENUE
        ).values(
                tenderID,
                request.reference(),
                request.replyTo().accept(),
                request.replyTo().decline(),
                stops.toArray(new LoadTenderStopRecord[0]),
                revenue.toArray(new LoadTenderRevenueItemRecord[0])
        ).returning();
    }

    @Secured(Authority.Authorities.CUSTOMER)
    @Operation(summary = "Update a load tender")
    @PutMapping
    public void updateLoadTender(@RequestBody LoadTenderRequest request) {

        APIKey key = (APIKey) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            PostgresConnection.getInstance().unsafely(supabase -> {
                supabase.transaction(transaction -> {

                    // locate the tender
                    LoadTendersRecord tender = transaction.dsl()
                            .selectFrom(Tables.LOAD_TENDERS)
                            .where(Tables.LOAD_TENDERS.CUSTOMER_ID.eq(key.getSub()))
                            .and(Tables.LOAD_TENDERS.ORIGINAL_CUSTOMER_REFERENCE_NUMBER.eq(request.uniqueReferenceID()))
                            .fetchOne();
                    if (Objects.isNull(tender)) throw new RuntimeException("Unable to Locate Load Tender!");

                    // create the load tender version
                    LoadTenderVersionsRecord version = this.buildLoadTenderVersion(transaction.dsl(), tender.getId(), request).fetchOne();
                    if (Objects.isNull(version)) throw new RuntimeException("Unable to Create Load Tender Version!");
                });
                return null;
            });
        } catch (Exception e) {
            this.handle(e);
        }
    }

    @Secured(Authority.Authorities.CUSTOMER)
    @Operation(summary = "Create a new load tender")
    @PostMapping
    public IDResponse createLoadTender(@RequestBody LoadTenderRequest request) throws SQLException {

        APIKey key = (APIKey) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AtomicReference<LoadTendersRecord> tender = new AtomicReference<>();

        try {
            PostgresConnection.getInstance().unsafely(supabase -> {
                supabase.transaction(transaction -> {
                    // create the load tender
                    tender.set(transaction.dsl().insertInto(Tables.LOAD_TENDERS,
                            Tables.LOAD_TENDERS.CUSTOMER_ID,
                            Tables.LOAD_TENDERS.ORIGINAL_CUSTOMER_REFERENCE_NUMBER
                    ).values(
                            key.getSub(),
                            request.uniqueReferenceID()
                    ).returning().fetchOne());
                    if (Objects.isNull(tender.get())) throw new RuntimeException("Unable to Create Load Tender!");

                    // create the load tender version
                    LoadTenderVersionsRecord version = this.buildLoadTenderVersion(transaction.dsl(), tender.get().getId(), request).fetchOne();
                    if (Objects.isNull(version)) throw new RuntimeException("Unable to Create Load Tender Version!");
                });
                return null;
            });
        } catch (Exception e) {
            handle(e);
        }

        if (Objects.isNull(tender.get()))
            throw new ResponseException("Unable to Create Load Tender", "No error was thrown, but no load tender was created");

        return IDResponse.from(tender.get().getId());
    }

}
