package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.auth.bearer.APIKey;
import com.inspiretmstech.api.models.requests.LoadTenderRequest;
import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.LoadTenderVersionsRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/v1/load-tenders")
public class LoadTendersController {

    @Secured(Authority.Authorities.CUSTOMER)
    @PostMapping
    public String createLoadTender(@RequestBody LoadTenderRequest request) throws SQLException {

        APIKey key = (APIKey) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ArrayList<LoadTenderStopRecord> stops = new ArrayList<>();
        ArrayList<LoadTenderRevenueItemRecord> revenue = new ArrayList<>();

        AtomicReference<LoadTendersRecord> tender = new AtomicReference<>();

        DatabaseConnection.getInstance().with(supabase -> {
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
                LoadTenderVersionsRecord version = transaction.dsl().insertInto(Tables.LOAD_TENDER_VERSIONS,
                        Tables.LOAD_TENDER_VERSIONS.LOAD_TENDER_ID,
                        Tables.LOAD_TENDER_VERSIONS.CUSTOMER_REFERENCE_NUMBER,
                        Tables.LOAD_TENDER_VERSIONS.ACCEPT_WEBHOOK,
                        Tables.LOAD_TENDER_VERSIONS.DECLINE_WEBHOOK,
                        Tables.LOAD_TENDER_VERSIONS.STOPS,
                        Tables.LOAD_TENDER_VERSIONS.REVENUE
                ).values(
                        tender.get().getId(),
                        request.reference(),
                        request.replyTo().accept(),
                        request.replyTo().decline(),
                        stops.toArray(new LoadTenderStopRecord[0]),
                        revenue.toArray(new LoadTenderRevenueItemRecord[0])
                ).returning().fetchOne();

            });
            return null;
        });

        if(Objects.isNull(tender.get())) throw new RuntimeException("Unable to Create Load Tender!");

        return "";
    }

}
