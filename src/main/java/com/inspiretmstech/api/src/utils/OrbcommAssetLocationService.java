package com.inspiretmstech.api.src.utils;

import com.inspiretmstech.api.src.models.requests.orbcomm.Data;
import com.inspiretmstech.api.src.models.requests.orbcomm.OrbcommDataPushRequest;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Geocoder;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.tables.records.EldAssetsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrbcommAssetLocationService {

    private final Logger logger = LoggerFactory.getLogger(OrbcommAssetLocationService.class);
    private final Map<String, Data> assetCache = new ConcurrentHashMap<>();

    public void enqueue(OrbcommDataPushRequest request) {
        if (Objects.isNull(request.data())) return;
        for (Data record : request.data()) assetCache.put(record.assetStatus().deviceSN(), record);
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // Runs every 10 minutes
    public void processAndClearCache() {
        if (assetCache.isEmpty()) return;

        PostgresConnection db;
        try {
            db = PostgresConnection.getInstance();
        } catch (SQLException e) {
            logger.error("An error occurred while connecting to the database: {}", e.getMessage());
            return;
        }

        // avoids race conditions
        Map<String, Data> assetCache = new ConcurrentHashMap<>(this.assetCache);
        this.assetCache.clear();

        for (Data data : assetCache.values())
            try {
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
                        .onConflict(Tables.ELD_ASSETS.UID).doUpdate().set(eldAssetsRecord)
                        .execute()
                ));
            } catch (Exception e) {
                this.logger.error("{}: {}", data.assetStatus().deviceSN(), e.getMessage());
            }
    }

    private Optional<EldAssetsRecord> safely(Executor executor) {
        try {
            return Optional.ofNullable(executor.execute());
        } catch (Exception e) {
            logger.error("An error occurred while building record: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface Executor {
        EldAssetsRecord execute() throws Exception;
    }
}
