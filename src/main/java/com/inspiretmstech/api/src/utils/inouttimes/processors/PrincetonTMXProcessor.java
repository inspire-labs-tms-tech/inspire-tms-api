package com.inspiretmstech.api.src.utils.inouttimes.processors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.utils.Executor;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimes;
import com.inspiretmstech.api.src.utils.inouttimes.TimeProcessor;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.routines.GetSecret;
import com.inspiretmstech.db.tables.records.*;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class PrincetonTMXProcessor extends TimeProcessor {

    public PrincetonTMXProcessor() {
        super(PrincetonTMXProcessor.class);
    }

    private void send(InOutTimes processor, boolean isArrived) {

        PostgresConnection conn;
        try {
            conn = PostgresConnection.getInstance();
        } catch (SQLException e) {
            logger.error("Unable to connect to PostgreSQL: {}", e.getMessage());
            throw new ResponseException("Unable to connect to PostgreSQL");
        }
        Optional<IntegrationsRecord> integration = conn.with(supabase -> supabase.selectFrom(Tables.INTEGRATIONS).where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.PRINCETON_TMX)).fetchOne());

        if (integration.isEmpty()) throw new ResponseException("Unable to load PrincetonTMX Integration");

        if (Objects.isNull(integration.get().getPricetonTmxApiKey()))
            throw new ResponseException("Improper Integration Configuration", "PRINCETON_TMX_apy_key is null");

        GetSecret secret = new GetSecret();
        secret.setSecretId(integration.get().getDsgApiKeyId());
        conn.with(supabase -> secret.execute(supabase.configuration()));
        Optional<String> apiKey = Optional.ofNullable(secret.getReturnValue());
        if (apiKey.isEmpty() || apiKey.get().isBlank())
            throw new ResponseException("Unable to Load Integration", "Unable to load API Key from Vault");

        OrdersRecord order = conn.with(supabase -> supabase.selectFrom(Tables.ORDERS).where(Tables.ORDERS.ID.eq(processor.orderID())).fetchOne()).orElse(null);
        if (Objects.isNull(order)) throw new ResponseException("Unable to load Order");

        // get the customer
        Optional<CustomersRecord> customer = conn.with(supabase -> supabase
                .selectFrom(Tables.CUSTOMERS)
                .where(Tables.CUSTOMERS.ID.eq(order.getCustomerId()))
                .fetchOne()
        );
        if (customer.isEmpty()) {
            logger.error("unable to load customer");
            return;
        }
        if (!customer.get().getIsPrincetonTmxCustomer()) {
            logger.debug("princeton tmx not enabled for customer");
            return;
        }

        LoadTendersRecord tender = conn.with(supabase -> supabase.selectFrom(Tables.LOAD_TENDERS).where(Tables.LOAD_TENDERS.ORDER_ID.eq(order.getId())).fetchOne()).orElse(null);
        if (Objects.isNull(tender)) throw new ResponseException("Unable to load Tender");

        StopsRecord stop = conn.with(supabase -> supabase.selectFrom(Tables.STOPS).where(Tables.STOPS.ORDER_ID.eq(order.getId())).and(Tables.STOPS.STOP_NUMBER.eq(processor.stopNumber())).fetchOne()).orElse(null);
        if (Objects.isNull(stop)) throw new ResponseException("Unable to load Stop");

        Optional<EquipmentRecord> truck = Objects.isNull(stop.getTruckId()) ? Optional.empty() : conn.with(supabase -> supabase.selectFrom(Tables.EQUIPMENT).where(Tables.EQUIPMENT.ID.eq(stop.getTruckId())).fetchOne());
        Optional<EquipmentRecord> trailer = Objects.isNull(stop.getTrailerId()) ? Optional.empty() : conn.with(supabase -> supabase.selectFrom(Tables.EQUIPMENT).where(Tables.EQUIPMENT.ID.eq(stop.getTrailerId())).fetchOne());

        String url = "https://carrier.qa.ptmx.io/events/loads/" + tender.getOriginalCustomerReferenceNumber();

        Optional<EquipmentRecord> trackable = Stream.of(
                        truck.orElse(null),
                        trailer.orElse(null)
                )
                .filter(Objects::nonNull)
                .filter(e -> Objects.nonNull(e.getEldProvider()) && Objects.nonNull(e.getEldConnection()) && Objects.nonNull(e.getEldConnection().getKey()))
                .findFirst();

        Optional<EldAssetsRecord> snapshot = Optional.empty();
        if (trackable.isPresent()) snapshot = conn.with(supabase -> supabase.selectFrom(Tables.ELD_ASSETS)
                .where(Tables.ELD_ASSETS.TYPE.eq(trackable.get().getEldProvider()))
                .and(Tables.ELD_ASSETS.ELD_PROVIDER_ID.eq(trackable.get().getEldConnection().getKey()))
                .fetchOne()
        );

        // Create JSON payload using GSON
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("stopOrder", Integer.valueOf(stop.getLoadTenderStopId()));
        jsonPayload.addProperty("lastLocationLatitude", (snapshot.isEmpty() || Objects.isNull(snapshot.get().getLastAddress())) ? null : snapshot.get().getLastAddress().getLatitude());
        jsonPayload.addProperty("lastLocationLongitude", (snapshot.isEmpty() || Objects.isNull(snapshot.get().getLastAddress())) ? null : snapshot.get().getLastAddress().getLongitude());
        jsonPayload.addProperty("lastLocationCity", (snapshot.isEmpty() || Objects.isNull(snapshot.get().getLastAddress())) ? null : snapshot.get().getLastAddress().getCity());
        jsonPayload.addProperty("lastLocationState", (snapshot.isEmpty() || Objects.isNull(snapshot.get().getLastAddress())) ? null : snapshot.get().getLastAddress().getState());
        jsonPayload.addProperty("truckID", truck.map(EquipmentRecord::getUnitNumber).orElse(null));
        jsonPayload.addProperty("trailerNumber", trailer.map(EquipmentRecord::getUnitNumber).orElse(null));
        jsonPayload.addProperty("earlyLateApptReason", (String) null);
        jsonPayload.addProperty("arrival", isArrived ? processor.at() : (Objects.isNull(stop.getDriverArrivedAt()) ? null : stop.getDriverArrivedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        jsonPayload.addProperty("departure", isArrived ? Objects.isNull(stop.getDriverDepartedAt()) ? null : stop.getDriverDepartedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : processor.at());
        jsonPayload.addProperty("xat", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // Create an HTTP client
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create an HTTP POST request
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity((new Gson()).toJson(jsonPayload), ContentType.APPLICATION_JSON));

            // Set headers if needed
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-api-key", apiKey.get());

            // Execute the request and handle the response
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getCode() != 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    logger.error(responseBody);
                } else logger.debug("request sent successfully");
            } catch (ParseException e) {
                logger.error("an error occurred sending update, but the response body could not be parsed as a string");
            }
        } catch (IOException e) {
            logger.error("An error occurred while processing request: {}", e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) logger.debug(el.toString());
        }
    }

    @Override
    protected Executor<InOutTimes> getArrivalProcessor() {
        return (processor) -> this.send(processor, true);
    }

    @Override
    protected Executor<InOutTimes> getDepartureProcessor() {
        return (processor) -> this.send(processor, false);
    }
}
