package com.inspiretmstech.api.src.utils.apis;

import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.common.microservices.dsg.DicksSportingGoodsOutboundApi;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.routines.GetSecret;
import com.inspiretmstech.db.tables.records.IntegrationsRecord;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class DicksSportingGoodsAPI {

    private final IntegrationsRecord integration;
    private final PostgresConnection conn;

    public DicksSportingGoodsAPI() {
        try {
            this.conn = PostgresConnection.getInstance();
        } catch (SQLException e) {
            throw new ResponseException("Unable to connect to database", "A database connection error occurred");
        }

        Optional<IntegrationsRecord> dsg = conn.with(supabase ->
                supabase.selectFrom(Tables.INTEGRATIONS)
                        .where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.DSG))
                        .fetchOne()
        );
        if (dsg.isEmpty()) throw new ResponseException("Unable to Load Dicks Sporting Goods Integration");
        this.integration = dsg.get();
    }

    public IntegrationsRecord getIntegration() {
        return this.integration;
    }

    public DicksSportingGoodsOutboundApi outbound() {
        if (Objects.isNull(this.integration.getDsgScac()) || this.integration.getDsgScac().isBlank())
            throw new ResponseException("Improper Dicks Sporting Goods Integration Configuration", "Invalid SCAC");
        if (Objects.isNull(this.integration.getDsgApiKeyId()))
            throw new ResponseException("Improper Dicks Sporting Goods Integration Configuration", "Zenbridge API Key is missing");

        GetSecret secret = new GetSecret();
        secret.setSecretId(this.integration.getDsgApiKeyId());
        this.conn.with(supabase -> secret.execute(supabase.configuration()));
        Optional<String> zenbridgeAPIKey = Optional.ofNullable(secret.getReturnValue());
        if (zenbridgeAPIKey.isEmpty() || zenbridgeAPIKey.get().isBlank())
            throw new ResponseException("Unable to Load Dicks Sporting Goods Integration", "Unable to Load Zenbridge API Key");

        com.inspiretmstech.common.microservices.dsg.ApiClient client = com.inspiretmstech.common.microservices.dsg.Configuration.getDefaultApiClient();
        client.setBasePath(ZenbridgeAPI.getBaseURL());
        client.addDefaultHeader("Authorization", "Bearer " + zenbridgeAPIKey.get());
        return new DicksSportingGoodsOutboundApi(client);
    }

}
