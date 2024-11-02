package com.inspiretmstech.api.src.controllers.v1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.APIKeyAuthenticationHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.requires.Requires;
import com.inspiretmstech.api.src.auth.requires.Scopes;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderActionRequest;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequest;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequestRevenueItem;
import com.inspiretmstech.api.src.models.requests.tenders.LoadTenderRequestStop;
import com.inspiretmstech.api.src.models.responses.IDResponse;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.common.microservices.dsg.ApiException;
import com.inspiretmstech.common.microservices.dsg.DicksSportingGoodsOutboundApi;
import com.inspiretmstech.common.microservices.dsg.models.*;
import com.inspiretmstech.common.microservices.gp.GeorgiaPacificOutboundApi;
import com.inspiretmstech.common.microservices.gp.models.SegmentRemarks;
import com.inspiretmstech.common.microservices.gp.models.*;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Environment;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.enums.LoadTenderStatus;
import com.inspiretmstech.db.routines.GetSecret;
import com.inspiretmstech.db.tables.records.*;
import com.inspiretmstech.db.udt.records.AddressRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.InsertResultStep;
import org.jooq.JSONB;
import org.jooq.UpdateSetMoreStep;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Tag(name = "Load Tenders", description = "Endpoints for tendering loads directly to Inspire TMS")
@RequestMapping("/v1/load-tenders")
public class LoadTendersController extends Controller {

    private final static DateTimeFormatter humanReadable = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a ZZZZ");
    private final static DateTimeFormatter apiFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    public LoadTendersController() {
        super(LoadTendersController.class);
    }

    public boolean check(@Nullable Object obj) {
        if (Objects.isNull(obj)) return false;
        if (obj instanceof String && ((String) obj).isBlank()) return false;
        return true;
    }

    public String addressToString(@NotNull AddressRecord address) {
        StringBuilder formattedAddress = new StringBuilder();

        formattedAddress.append(address.getStreetAddress_1());

        if (address.getStreetAddress_2() != null && !address.getStreetAddress_2().isEmpty()) {
            formattedAddress.append(", ").append(address.getStreetAddress_2());
        }

        formattedAddress.append(", ")
                .append(address.getCity()).append(", ")
                .append(address.getState()).append(" ")
                .append(address.getZip());

        return formattedAddress.toString();
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

            stops.add(new LoadTenderStopRecord(null, OffsetDateTime.parse(stop.appointment().earliest()), OffsetDateTime.parse(stop.appointment().latest()), stop.type(), stop.address().build(), JSONB.valueOf("{}")));
        }

        LoadTenderVersionsRecord newVersion = new LoadTenderVersionsRecord();
        newVersion.setLoadTenderId(tenderID);
        newVersion.setCustomerReferenceNumber(request.reference());
        newVersion.setAcceptWebhook(request.replyTo().accept());
        newVersion.setDeclineWebhook(request.replyTo().decline());
        newVersion.setStops(stops.toArray(new LoadTenderStopRecord[0]));
        newVersion.setRevenue(revenue.toArray(new LoadTenderRevenueItemRecord[0]));

        return database
                .insertInto(Tables.LOAD_TENDER_VERSIONS)
                .set(newVersion)
                .returning();
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.OPERATIONS)
    @Operation(summary = "Act on a load tender")
    @PostMapping("/{id}/{version}")
    public StatusResponse acceptOrDeclineLoadTender(@RequestBody LoadTenderActionRequest request, @PathVariable String id, @PathVariable String version) throws SQLException {

        Optional<LoadTendersRecord> tender = PostgresConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.LOAD_TENDERS)
                        .where(Tables.LOAD_TENDERS.ID.eq(UUID.fromString(id)))
                        .fetchOne()
        );
        if (tender.isEmpty())
            throw new ResponseException("Not Found", "Load Tender with id '" + id + "' could not be found");

        Optional<LoadTenderVersionsRecord> tenderVersion = PostgresConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.LOAD_TENDER_VERSIONS)
                        .where(Tables.LOAD_TENDER_VERSIONS.ID.eq(UUID.fromString(version)))
                        .fetchOne()
        );
        if (tenderVersion.isEmpty())
            throw new ResponseException("Not Found", "Load Tender with id '" + id + "' could not be found");

        // shared Zenbridge URLs
        String VERSION = Environment.get("VERSION");
        String baseURL = switch (VERSION) {
            case "main" -> "https://api.zenbridge.io";
            case "development" -> "https://api.sandbox.zenbridge.io";
            default -> throw new RuntimeException("VERSION '" + VERSION + "' is unhandled");
        };

        // use SMTP as a fall-through case, as this would never be used in this context
        IntegrationTypes type = Optional.ofNullable(tender.get().getIntegrationType()).orElse(IntegrationTypes.CUSTOM_SMTP);
        switch (type) {
            case GEORGIA_PACIFIC -> {
                Optional<IntegrationsRecord> gp = PostgresConnection.getInstance().with(supabase ->
                        supabase.selectFrom(Tables.INTEGRATIONS)
                                .where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.GEORGIA_PACIFIC))
                                .fetchOne()
                );
                if (gp.isEmpty())
                    throw new ResponseException("Unable to Load Georgia Pacific Integration", "Unable to Load Georgia Pacific Integration");
                if (Objects.isNull(gp.get().getGeorgiaPacificScac()) || gp.get().getGeorgiaPacificScac().isBlank())
                    throw new ResponseException("Improper Georgia Pacific Integration Configuration", "Invalid SCAC");
                if (Objects.isNull(gp.get().getGeorgiaPacificZenbridgeApiKeyId()))
                    throw new ResponseException("Improper Georgia Pacific Integration Configuration", "Zenbridge API Key is missing");

                GetSecret secret = new GetSecret();
                secret.setSecretId(gp.get().getGeorgiaPacificZenbridgeApiKeyId());
                PostgresConnection.getInstance().with(supabase -> secret.execute(supabase.configuration()));
                Optional<String> zenbridgeAPIKey = Optional.ofNullable(secret.getReturnValue());
                if (zenbridgeAPIKey.isEmpty() || zenbridgeAPIKey.get().isBlank())
                    throw new ResponseException("Unable to Load Georgia Pacific Integration", "Unable to Load Zenbridge API Key");

                com.inspiretmstech.common.microservices.gp.ApiClient client = com.inspiretmstech.common.microservices.gp.Configuration.getDefaultApiClient();
                client.setBasePath(baseURL);
                client.addDefaultHeader("Authorization", "Bearer " + zenbridgeAPIKey.get());
                GeorgiaPacificOutboundApi api = new GeorgiaPacificOutboundApi(client);

                RtsEdiSendGeorgiaPacificResponseToALoadTenderPostRequestInner body = new RtsEdiSendGeorgiaPacificResponseToALoadTenderPostRequestInner();
                RtsEdiSendGeorgiaPacificResponseToALoadTenderPostRequestInnerData data = new RtsEdiSendGeorgiaPacificResponseToALoadTenderPostRequestInnerData();
                SectionRtsGeorgiaPacificResponseToALoadTender1 section1 = new SectionRtsGeorgiaPacificResponseToALoadTender1();

                SegmentReferenceIdentificationA607926c38cfe8faa588714f3ad39cc94df1884733756cb47bf0c3a91bcfcbcd refID = new SegmentReferenceIdentificationA607926c38cfe8faa588714f3ad39cc94df1884733756cb47bf0c3a91bcfcbcd();
                refID.setReferenceIdentificationQualifier(SegmentReferenceIdentificationA607926c38cfe8faa588714f3ad39cc94df1884733756cb47bf0c3a91bcfcbcd.ReferenceIdentificationQualifierEnum.CN);
                refID.setReferenceIdentification("TENDER-" + tender.get().getNumber());
                section1.setReferenceIdentification(refID);

                SegmentBeginningSegmentForBookingOrPickupOrDeliveryFd5c9c80f5468dd49a82921bf09de44761e645537968a5d096703bf6e4147b9d beginning = new SegmentBeginningSegmentForBookingOrPickupOrDeliveryFd5c9c80f5468dd49a82921bf09de44761e645537968a5d096703bf6e4147b9d();
                beginning.setDate(apiFormat.format(LocalDate.now()));
                beginning.setShipmentIdentificationNumber(tender.get().getOriginalCustomerReferenceNumber());
                beginning.setReservationActionCode(request.accept() ? SegmentBeginningSegmentForBookingOrPickupOrDeliveryFd5c9c80f5468dd49a82921bf09de44761e645537968a5d096703bf6e4147b9d.ReservationActionCodeEnum.A : SegmentBeginningSegmentForBookingOrPickupOrDeliveryFd5c9c80f5468dd49a82921bf09de44761e645537968a5d096703bf6e4147b9d.ReservationActionCodeEnum.D);
                beginning.setStandardCarrierAlphaCode(gp.get().getGeorgiaPacificScac());
                section1.setBeginningSegmentForBookingOrPickupOrDelivery(beginning);

                SegmentRemarks remark = new SegmentRemarks();
                remark.setFreeformMessage("Load Tender " + (request.accept() ? "Accepted" : "Declined"));
                remark.setFreeformMessage1("u");
                section1.setRemarks(List.of(remark));

                data.setSection1(section1);
                body.setData(data);

                try {
                    api.rtsEdiSendGeorgiaPacificResponseToALoadTenderPost(List.of(body), "GeorgiaPacific");
                } catch (com.inspiretmstech.common.microservices.gp.ApiException e) {
                    this.logger.error(e.getMessage(), e.getResponseBody());
                    throw new ResponseException("Unable to POST Zenbridge Update", "An error occurred while sending the transaction for EDI processing", e.getMessage() + ": " + e.getResponseBody());
                }
            }
            case DSG -> {
                Optional<IntegrationsRecord> dsg = PostgresConnection.getInstance().with(supabase ->
                        supabase.selectFrom(Tables.INTEGRATIONS)
                                .where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.DSG))
                                .fetchOne()
                );
                if (dsg.isEmpty()) throw new ResponseException("Unable to Load Dicks Sporting Goods Integration");
                if (Objects.isNull(dsg.get().getDsgScac()) || dsg.get().getDsgScac().isBlank())
                    throw new ResponseException("Improper Dicks Sporting Goods Integration Configuration", "Invalid SCAC");
                if (Objects.isNull(dsg.get().getDsgApiKeyId()))
                    throw new ResponseException("Improper Dicks Sporting Goods Integration Configuration", "Zenbridge API Key is missing");

                GetSecret secret = new GetSecret();
                secret.setSecretId(dsg.get().getDsgApiKeyId());
                PostgresConnection.getInstance().with(supabase -> secret.execute(supabase.configuration()));
                Optional<String> zenbridgeAPIKey = Optional.ofNullable(secret.getReturnValue());
                if (zenbridgeAPIKey.isEmpty() || zenbridgeAPIKey.get().isBlank())
                    throw new ResponseException("Unable to Load Dicks Sporting Goods Integration", "Unable to Load Zenbridge API Key");

                com.inspiretmstech.common.microservices.dsg.ApiClient client = com.inspiretmstech.common.microservices.dsg.Configuration.getDefaultApiClient();
                client.setBasePath(baseURL);
                client.addDefaultHeader("Authorization", "Bearer " + zenbridgeAPIKey.get());
                DicksSportingGoodsOutboundApi api = new DicksSportingGoodsOutboundApi(client);

                RtsEdiSendDicksSportingGoodsResponseToLoadTenderPostRequestInner inner = new RtsEdiSendDicksSportingGoodsResponseToLoadTenderPostRequestInner();
                RtsEdiSendDicksSportingGoodsResponseToLoadTenderPostRequestInnerData data = new RtsEdiSendDicksSportingGoodsResponseToLoadTenderPostRequestInnerData();
                SectionRtsDicksSportingGoodsResponseToLoadTender1 section1 = new SectionRtsDicksSportingGoodsResponseToLoadTender1();

                SegmentReferenceIdentificationEaca2fd5ce4c5f0f0738dfda92ad3e56b7f5552d38f105a83fba8571b6bf76a0 ref = new SegmentReferenceIdentificationEaca2fd5ce4c5f0f0738dfda92ad3e56b7f5552d38f105a83fba8571b6bf76a0();
                ref.setReferenceIdentificationQualifier(SegmentReferenceIdentificationEaca2fd5ce4c5f0f0738dfda92ad3e56b7f5552d38f105a83fba8571b6bf76a0.ReferenceIdentificationQualifierEnum.CN);
                ref.setReferenceIdentification("TENDER-" + tender.get().getNumber());
                section1.setReferenceIdentification(ref);

                SegmentBeginningSegmentForBookingOrPickupOrDelivery906c00531695b2ddfe89ee321e01671195cc392c9435cee04a4b5c96608e75fc beginning = new SegmentBeginningSegmentForBookingOrPickupOrDelivery906c00531695b2ddfe89ee321e01671195cc392c9435cee04a4b5c96608e75fc();
                beginning.setDate(apiFormat.format(LocalDate.now()));
                beginning.setShipmentIdentificationNumber(tender.get().getOriginalCustomerReferenceNumber());
                beginning.setReservationActionCode(request.accept() ? SegmentBeginningSegmentForBookingOrPickupOrDelivery906c00531695b2ddfe89ee321e01671195cc392c9435cee04a4b5c96608e75fc.ReservationActionCodeEnum.A : SegmentBeginningSegmentForBookingOrPickupOrDelivery906c00531695b2ddfe89ee321e01671195cc392c9435cee04a4b5c96608e75fc.ReservationActionCodeEnum.D);
                beginning.setStandardCarrierAlphaCode(dsg.get().getDsgScac());
                section1.setBeginningSegmentForBookingOrPickupOrDelivery(beginning);

                data.setSection1(section1);
                inner.setData(data);

                try {
                    api.rtsEdiSendDicksSportingGoodsResponseToLoadTenderPost(List.of(inner), dsg.get().getDsgScac().toUpperCase());
                } catch (ApiException e) {
                    this.logger.error(e.getMessage(), e.getResponseBody());
                    throw new ResponseException("Unable to POST Zenbridge Update", "An error occurred while sending the transaction for EDI processing", e.getMessage() + ": " + e.getResponseBody());
                }
            }
            default -> {
                // send the response (using '#' as escape character)
                String escapeChar = "#";
                if (request.accept() ? !escapeChar.equals(tenderVersion.get().getAcceptWebhook()) : !escapeChar.equals(tenderVersion.get().getDeclineWebhook())) {
                    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(request.accept() ? tenderVersion.get().getAcceptWebhook() : tenderVersion.get().getDeclineWebhook());
                        try (CloseableHttpResponse response = httpClient.execute(post)) {
                            logger.trace("Webhook Response Status: {}", response.getStatusLine().getStatusCode());
                            logger.trace("Webhook Response Body: {}", new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
                            if (response.getStatusLine().getStatusCode() / 100 != 2) // non-20X status code returned
                                throw new ResponseException("Unable to Response to Webhook", "Webhook responded with status code: " + response.getStatusLine().getStatusCode());
                        } catch (HttpResponseException e) {
                            logger.error("Webhook Request Failed: {}", e.getMessage());
                            throw new ResponseException("Unable to Respond to Webhook", "Webhook responded with error", e.getMessage());
                        }
                    } catch (IOException e) {
                        logger.error("Webhook Request Failed (IOException): {}", e.getMessage());
                        throw new ResponseException("Unable to Respond to Webhook", "Webhook responded with error", e.getMessage());
                    }
                }
            }
        }

        try {
            // if declined, no further processing needed
            if (!request.accept()) {
                PostgresConnection.getInstance().unsafely(supabase -> {
                    supabase.transaction(trx -> {
                        @Nullable
                        LoadTendersRecord newTender = trx.dsl().update(Tables.LOAD_TENDERS)
                                .set(Tables.LOAD_TENDERS.STATUS, LoadTenderStatus.DECLINED)
                                .where(Tables.LOAD_TENDERS.ID.eq(tender.get().getId()))
                                .returning()
                                .fetchOne();
                        if (Objects.isNull(newTender))
                            throw new ResponseException("Unable to Update Tender", "An error occurred while declining the load tender");

                        @Nullable
                        LoadTenderVersionsRecord newTenderVersion = trx.dsl().update(Tables.LOAD_TENDER_VERSIONS)
                                .set(Tables.LOAD_TENDER_VERSIONS.STATUS, LoadTenderStatus.DECLINED)
                                .where(Tables.LOAD_TENDER_VERSIONS.ID.eq(tenderVersion.get().getId()))
                                .returning()
                                .fetchOne();
                        if (Objects.isNull(newTenderVersion))
                            throw new ResponseException("Unable to Update Tender", "An error occurred while declining the load tender version");
                    });
                    return null;
                });
                return StatusResponse.DECLINED();
            }


            // if not silent, need to update the order
            AtomicReference<UUID> orderID = new AtomicReference<>();
            if (!request.silent()) PostgresConnection.getInstance().unsafely(supabase -> {
                supabase.transaction(trx -> {

                    // no order exists
                    if (Objects.isNull(tender.get().getOrderId())) {
                        // create the order
                        OrdersRecord _order = new OrdersRecord();
                        _order.setCustomerId(tender.get().getCustomerId());
                        _order.setDate(LocalDate.now()); // handled automatically in postgres triggers
                        _order.setCustomerReferenceNumber(Optional.ofNullable(tenderVersion.get().getCustomerReferenceNumber()).orElse(""));
                        _order.setIsRequiringBols(false); // handled automatically in postgres triggers
                        _order.setPcmilerRoutingType(null); // handled automatically in postgres triggers
                        Optional<OrdersRecord> order = Optional.ofNullable(trx.dsl().insertInto(Tables.ORDERS).set(_order).returning().fetchOne());
                        if (order.isEmpty())
                            throw new ResponseException("Unable to Accept Tender", "an error occurred while creating the order");

                        // create the stops
                        for (LoadTenderStopRecord stop : tenderVersion.get().getStops()) {
                            StopsRecord _stop = new StopsRecord();
                            _stop.setAddress(stop.getAddress());
                            _stop.setOrderId(order.get().getId());
                            _stop.setStopNumber((long) -1); // handled in postgres triggers
                            _stop.setLoadTenderStopId(stop.getId());
                            _stop.setNotesShared("Earliest Arrival: " + (Objects.nonNull(stop.getEarliestArrival()) ? stop.getEarliestArrival().format(humanReadable) : "") + "\nLatest Arrival: " + (Objects.nonNull(stop.getLatestArrival()) ? stop.getLatestArrival().format(humanReadable) : ""));

                            // dynamic meta handling
                            if(Objects.nonNull(stop.getMeta()) && Objects.nonNull(stop.getMeta().data())) {
                                JsonElement e = JsonParser.parseString(stop.getMeta().data());
                                if(!e.isJsonNull() && e.isJsonObject()) {
                                    JsonObject meta = e.getAsJsonObject();
                                    if (meta.has("facility_id") && !meta.get("facility_id").isJsonNull()) {
                                        Optional<FacilitiesRecord> facility = trx.dsl().selectFrom(Tables.FACILITIES)
                                                .where(Tables.FACILITIES.ID.eq(UUID.fromString(meta.get("facility_id").getAsString())))
                                                .fetchOptional();
                                        if(facility.isPresent()) _stop.setFacilityId(facility.get().getId());
                                        else logger.error("unable to fetch facility id: {}", meta.get("facility_id").getAsString());
                                    }
                                }
                            }

                            Optional<StopsRecord> newStop = Optional.ofNullable(trx.dsl().insertInto(Tables.STOPS).set(_stop).returning().fetchOne());
                            if (newStop.isEmpty())
                                throw new ResponseException("Unable to Accept Tender", "Unable to Create Stop " + stop.getId());
                        }

                        // create the revenue
                        for (LoadTenderRevenueItemRecord line : tenderVersion.get().getRevenue()) {
                            RevenueLinesRecord _line = new RevenueLinesRecord();
                            _line.setOrderId(order.get().getId());
                            _line.setQuantity(line.getQuantity().shortValue());
                            _line.setRate(line.getRate());
                            _line.setAccountingPeriodId(0L); // handled automatically in postgres trigger
                            _line.setDate(LocalDate.now()); // handled automatically in postgres trigger
                            _line.setInvoiceId(UUID.randomUUID()); // handled automatically in postgres trigger

                            Optional<RevenueLinesRecord> newLine = Optional.ofNullable(trx.dsl().insertInto(Tables.REVENUE_LINES).set(_line).returning().fetchOne());
                            if (newLine.isEmpty())
                                throw new ResponseException("Unable to Create Revenue Line(s)", "An error occurred while creating revenue lines");
                        }

                        orderID.set(order.get().getId());
                    } else { // update the existing order
                        orderID.set(tender.get().getOrderId());

                        // update order
                        Optional<OrdersRecord> newOrder = Optional.ofNullable(trx.dsl()
                                .update(Tables.ORDERS)
                                .set(Tables.ORDERS.CUSTOMER_REFERENCE_NUMBER, Optional.ofNullable(tenderVersion.get().getCustomerReferenceNumber()).orElse(""))
                                .where(Tables.ORDERS.ID.eq(orderID.get()))
                                .returning()
                                .fetchOne());
                        if (newOrder.isEmpty())
                            throw new ResponseException("Unable to Update Existing Order", "An error occurred while updating the customer reference number", "Is the order locked?");

                        // delete existing records
                        trx.dsl().deleteFrom(Tables.STOPS).where(Tables.STOPS.ORDER_ID.eq(orderID.get())).execute();
                        trx.dsl().deleteFrom(Tables.REVENUE_LINES).where(Tables.REVENUE_LINES.ORDER_ID.eq(orderID.get())).execute();

                        // create the stops
                        for (LoadTenderStopRecord stop : tenderVersion.get().getStops()) {
                            StopsRecord _stop = new StopsRecord();
                            _stop.setAddress(stop.getAddress());
                            _stop.setOrderId(orderID.get());
                            _stop.setStopNumber((long) -1); // handled in postgres triggers
                            _stop.setLoadTenderStopId(stop.getId());
                            _stop.setNotesShared("Earliest Arrival: " + (Objects.nonNull(stop.getEarliestArrival()) ? stop.getEarliestArrival().format(humanReadable) : "") + "\nLatest Arrival: " + (Objects.nonNull(stop.getLatestArrival()) ? stop.getLatestArrival().format(humanReadable) : ""));
                            Optional<StopsRecord> newStop = Optional.ofNullable(trx.dsl().insertInto(Tables.STOPS).set(_stop).returning().fetchOne());
                            if (newStop.isEmpty())
                                throw new ResponseException("Unable to Accept Tender", "Unable to Create Stop " + stop.getId());
                        }

                        // create the revenue
                        for (LoadTenderRevenueItemRecord line : tenderVersion.get().getRevenue()) {
                            RevenueLinesRecord _line = new RevenueLinesRecord();
                            _line.setOrderId(orderID.get());
                            _line.setQuantity(line.getQuantity().shortValue());
                            _line.setRate(line.getRate());
                            _line.setAccountingPeriodId(0L); // handled automatically in postgres trigger
                            _line.setDate(LocalDate.now()); // handled automatically in postgres trigger
                            _line.setInvoiceId(UUID.randomUUID()); // handled automatically in postgres trigger

                            Optional<RevenueLinesRecord> newLine = Optional.ofNullable(trx.dsl().insertInto(Tables.REVENUE_LINES).set(_line).returning().fetchOne());
                            if (newLine.isEmpty())
                                throw new ResponseException("Unable to Create Revenue Line(s)", "An error occurred while creating revenue lines");
                        }

                    }
                });
                return null;
            });

            // update the version (regardless of whether silent)
            PostgresConnection.getInstance().unsafely(supabase -> {
                supabase.transaction(trx -> {

                    // build base query
                    UpdateSetMoreStep<LoadTendersRecord> query = trx.dsl().update(Tables.LOAD_TENDERS)
                            .set(Tables.LOAD_TENDERS.STATUS, LoadTenderStatus.ACCEPTED);
                    // dynamically add order id
                    if (Objects.nonNull(orderID.get())) query = query.set(Tables.LOAD_TENDERS.ORDER_ID, orderID.get());
                    @Nullable
                    LoadTendersRecord newTender = query
                            .where(Tables.LOAD_TENDERS.ID.eq(tender.get().getId()))
                            .returning()
                            .fetchOne();

                    if (Objects.isNull(newTender))
                        throw new ResponseException("Unable to Update Tender", "An error occurred while accepting the load tender");

                    @Nullable
                    LoadTenderVersionsRecord newTenderVersion = trx.dsl().update(Tables.LOAD_TENDER_VERSIONS)
                            .set(Tables.LOAD_TENDER_VERSIONS.STATUS, LoadTenderStatus.ACCEPTED)
                            .set(Tables.LOAD_TENDER_VERSIONS.STATUS_CHANGED_AT, OffsetDateTime.now())
                            .where(Tables.LOAD_TENDER_VERSIONS.ID.eq(tenderVersion.get().getId()))
                            .returning()
                            .fetchOne();
                    if (Objects.isNull(newTenderVersion))
                        throw new ResponseException("Unable to Update Tender", "An error occurred while accepting the load tender version");
                });
                return null;
            });

            return StatusResponse.ACCEPTED();


        } catch (Exception e) {
            if (e instanceof ResponseException) throw (ResponseException) e;
            else
                throw new ResponseException("An unhandled error occurred while declining the load tender", e.getMessage());
        }
    }

    @Secured(Authority.Authorities.CUSTOMER)
    @Operation(summary = "Update a load tender")
    @PutMapping
    public void updateLoadTender(@RequestBody LoadTenderRequest request) {

        APIKeyAuthenticationHolder holder = SecurityHolder.getAuthenticationHolder(APIKeyAuthenticationHolder.class);

        try {
            PostgresConnection.getInstance().unsafely(supabase -> {
                supabase.transaction(transaction -> {

                    // locate the tender
                    LoadTendersRecord tender = transaction.dsl()
                            .selectFrom(Tables.LOAD_TENDERS)
                            .where(Tables.LOAD_TENDERS.CUSTOMER_ID.eq(holder.getSub()))
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

        APIKeyAuthenticationHolder holder = SecurityHolder.getAuthenticationHolder(APIKeyAuthenticationHolder.class);

        AtomicReference<LoadTendersRecord> tender = new AtomicReference<>();

        try {
            PostgresConnection.getInstance().unsafely(supabase -> {
                supabase.transaction(transaction -> {
                    // create the load tender (upsertable)

                    LoadTendersRecord record = new LoadTendersRecord();
                    record.setCustomerId(holder.getSub());
                    record.setOriginalCustomerReferenceNumber(request.uniqueReferenceID());

                    tender.set(
                            transaction.dsl()
                                    .insertInto(Tables.LOAD_TENDERS)
                                    .set(record)
                                    .onConflict(Tables.LOAD_TENDERS.UID)
                                    .doUpdate()
                                    .set(record)
                                    .returning()
                                    .fetchOne()
                    );
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
