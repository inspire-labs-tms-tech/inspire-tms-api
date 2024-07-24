package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.models.ResponseException;
import com.inspiretmstech.api.models.requests.orbcomm.Data;
import com.inspiretmstech.api.models.requests.orbcomm.OrbcommDataPushRequest;
import com.inspiretmstech.api.models.responses.StatusResponse;
import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.api.utils.Geocoder;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.tables.records.EldAssetsRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@Tag(name = "Orbcomm Integration", description = "Orbcomm endpoints for managing integration-specific data")
@RequestMapping("/v1/orbcomm")
public class OrbcommWebhookController {

    private final Logger logger = LoggerFactory.getLogger(OrbcommWebhookController.class);

    private Optional<EldAssetsRecord> safely(Executor executor) {
        try {
            return Optional.ofNullable(executor.execute());
        } catch (Exception e) {
            logger.error("An error occurred while building record: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Secured(Authority.Authorities.ORBCOMM)
    @Operation(summary = "POST tracking updates")
    @PostMapping
    public StatusResponse sendTrackingUpdates(@RequestBody OrbcommDataPushRequest request) {
        this.logger.trace("Received Request: {}", request.data().size());

        DatabaseConnection db;
        try {
             db = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            logger.error("An error occurred while connecting to the database: {}", e.getMessage());
            throw new ResponseException("An error occurred while connecting to the database", e.getMessage());
        }

        for (Data data : request.data()) {
            // build the new snapshot
            Optional<EldAssetsRecord> record = this.safely(() -> {
                EldAssetsRecord updatedRecord = new EldAssetsRecord();
                updatedRecord.setType(IntegrationTypes.ORBCOMM);
                updatedRecord.setEldProviderId(data.assetStatus().deviceSN());
                updatedRecord.setAddressUpdatedAt(OffsetDateTime.now());
                updatedRecord.setLastAddress(Geocoder.reverseGeocode(data.positionStatus().address(), false));
                return updatedRecord;
            });

            // insert the snapshot
            record.ifPresent(eldAssetsRecord -> db.with(supabase -> supabase
                    .insertInto(Tables.ELD_ASSETS)
                    .set(eldAssetsRecord)
                    .onConflict().doUpdate().set(eldAssetsRecord)
                    .execute()
            ));
        }

        return new StatusResponse("accepted");
    }

    @FunctionalInterface
    private interface Executor {
        EldAssetsRecord execute() throws Exception;
    }
}
