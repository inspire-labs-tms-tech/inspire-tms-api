package com.inspiretmstech.api.src.controllers.v1;

import com.google.gson.Gson;
import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.APIKeyAuthenticationHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.constants.Constants;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.princetontmx.PrincetonTMXLoadTender;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequestRevenueItem;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequestStop;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.enums.LoadTenderStatus;
import com.inspiretmstech.db.tables.records.CustomersRecord;
import com.inspiretmstech.db.tables.records.LoadTenderVersionsRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@RestController
@Tag(name = "Princeton TMX", description = "Webhook for Princeton TMX")
@RequestMapping("/v1/princeton-tmx")
public class PrincetonTMXController extends Controller {

    public PrincetonTMXController() {
        super(PrincetonTMXController.class);
    }

    @Secured(Authority.Authorities.PRINCETONTMX)
    @Operation(summary = "Receive a Load Tender")
    @PostMapping
    public StatusResponse princetonTMXLoadTenderWebhook(@RequestBody PrincetonTMXLoadTender tender) throws Exception {

        this.logger.debug("princeton tmx load tender received: {}", tender.loadID());
        this.logger.trace("{}", tender);

        if (Objects.isNull(tender.loadID()) || tender.loadID().isBlank())
            throw new ResponseException("Missing Load ID", "The loadID field cannot be empty", Objects.isNull(tender.loadID()) ? "The loadID was null" : "The loadID was empty");

        APIKeyAuthenticationHolder holder = SecurityHolder.getAuthenticationHolder(APIKeyAuthenticationHolder.class);

        PostgresConnection.getInstance().unsafely(supabase -> {
            supabase.transaction(transaction -> {
                CustomersRecord customer = transaction.dsl().selectFrom(Tables.CUSTOMERS)
                        .where(Tables.CUSTOMERS.ID.eq(holder.getSub()))
                        .fetchOne();
                if (Objects.isNull(customer)) throw new ResponseException(
                        "Customer Error",
                        "Unable to load customer",
                        "A customer with id '" + holder.getSub().toString() + "' was not found"
                );

                // upsert the load tender record
                LoadTendersRecord record = new LoadTendersRecord();
                record.setCustomerId(holder.getSub());
                record.setOriginalCustomerReferenceNumber(tender.loadID().trim());
                record.setIntegrationType(IntegrationTypes.PRINCETON_TMX);
                record.setStatus(LoadTenderStatus.NEW);

                record = transaction.dsl()
                        .insertInto(Tables.LOAD_TENDERS)
                        .set(record)
                        .onConflict(Tables.LOAD_TENDERS.UID)
                        .doUpdate()
                        .set(record)
                        .returning()
                        .fetchOne();
                if (Objects.isNull(record))
                    throw new ResponseException("Unable to Create Load Tender!", "Load tender was empty");

                // create the load tender version
                LoadTenderVersionsRecord version = this.buildLoadTenderVersion(tender, record.getId());
                version = transaction.dsl()
                        .insertInto(Tables.LOAD_TENDER_VERSIONS)
                        .set(version)
                        .returning()
                        .fetchOne();
                if (Objects.isNull(version)) throw new ResponseException("Unable to Create Load Tender Version!", "Load tender version was empty");

            });
            return null;
        });

        return StatusResponse.ACCEPTED();
    }

    private LoadTenderVersionsRecord buildLoadTenderVersion(PrincetonTMXLoadTender tender, UUID tenderID) {

        LoadTenderVersionsRecord version = new LoadTenderVersionsRecord();
        version.setLoadTenderId(tenderID);
        version.setCustomerReferenceNumber(""); // TODO: add customer reference number
        version.setAcceptWebhook(Constants.Globals.LoadTenders.NO_WEBHOOK_CALLBACK);
        version.setDeclineWebhook(Constants.Globals.LoadTenders.NO_WEBHOOK_CALLBACK);
        version.setRawRequest(JSONB.valueOf((new Gson()).toJson(tender)));

        ArrayList<LoadTenderStopRecord> stops = new ArrayList<>();
        // TODO: build stops
        version.setStops(stops.toArray(new LoadTenderStopRecord[0]));

        ArrayList<LoadTenderRevenueItemRecord> revenue = new ArrayList<>();
        // TODO: build revenue
        version.setRevenue(revenue.toArray(new LoadTenderRevenueItemRecord[0]));

        // done
        return version;
    }

}
