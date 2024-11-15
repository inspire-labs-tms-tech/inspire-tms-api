package com.inspiretmstech.api.src.controllers.v1;

import com.google.gson.Gson;
import com.google.maps.model.LatLng;
import com.inspiretmstech.api.src.auth.methods.SecurityHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.APIKeyAuthenticationHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.constants.Constants;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.princetontmx.PrincetonTMXLoadTender;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Geocoder;
import com.inspiretmstech.common.utils.TimeZones;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.enums.LoadTenderStatus;
import com.inspiretmstech.db.enums.StopTypes;
import com.inspiretmstech.db.tables.records.CustomersRecord;
import com.inspiretmstech.db.tables.records.FacilitiesRecord;
import com.inspiretmstech.db.tables.records.LoadTenderVersionsRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.udt.records.AddressRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.Configuration;
import org.jooq.JSONB;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

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
                LoadTenderVersionsRecord version = this.buildLoadTenderVersion(tender, record.getId(), transaction);
                version = transaction.dsl()
                        .insertInto(Tables.LOAD_TENDER_VERSIONS)
                        .set(version)
                        .returning()
                        .fetchOne();
                if (Objects.isNull(version))
                    throw new ResponseException("Unable to Create Load Tender Version!", "Load tender version was empty");

            });
            return null;
        });

        return StatusResponse.ACCEPTED();
    }

    private LoadTenderVersionsRecord buildLoadTenderVersion(PrincetonTMXLoadTender tender, UUID tenderID, Configuration transaction) {

        LoadTenderVersionsRecord version = new LoadTenderVersionsRecord();
        version.setLoadTenderId(tenderID);
        version.setCustomerReferenceNumber((Objects.nonNull(tender.orders()) && !tender.orders().isEmpty())
                ? tender.orders().get(0).orderNumber()
                : ""
        );
        version.setAcceptWebhook(Constants.Globals.LoadTenders.NO_WEBHOOK_CALLBACK);
        version.setDeclineWebhook(Constants.Globals.LoadTenders.NO_WEBHOOK_CALLBACK);
        version.setRawRequest(JSONB.valueOf((new Gson()).toJson(tender)));


        ArrayList<LoadTenderStopRecord> stops = new ArrayList<>();
        List<PrincetonTMXLoadTender.Stop> sortedStops = new ArrayList<>(Objects.isNull(tender.stops()) ? Collections.emptyList() : tender.stops());
        sortedStops.sort(Comparator.comparing(PrincetonTMXLoadTender.Stop::stopOrder, Comparator.nullsLast(Integer::compareTo)));
        int stopNumber = 0;
        for (PrincetonTMXLoadTender.Stop s : sortedStops) {
            stopNumber++;
            LoadTenderStopRecord stop = new LoadTenderStopRecord();
            AddressRecord a;
            ZoneId zone;

            PrincetonTMXLoadTender.LocInfo loc = Objects.isNull(s.locInfos()) ? null : !s.locInfos().isEmpty() ? s.locInfos().get(0) : null;
            if (Objects.isNull(loc))
                throw new ResponseException("Missing Location", "Stop " + s.stopOrder() + " is missing a location");

            // search for existing facility
            FacilitiesRecord facility;
            String id = Objects.isNull(loc.locationID()) ? null : loc.locationID();
            Optional<FacilitiesRecord> existingFacility = Objects.isNull(id) ? Optional.empty() : Optional.ofNullable(transaction.dsl()
                    .selectFrom(Tables.FACILITIES)
                    .where(Tables.FACILITIES.EDI_FACILITY_ID.eq(id))
                    .and(Tables.FACILITIES.EDI_INTEGRATION_TYPE.eq(IntegrationTypes.PRINCETON_TMX))
                    .fetchOne());
            if (existingFacility.isPresent()) {
                facility = existingFacility.get();
                a = facility.getAddress();
                zone = ZoneId.of(a.getIanaTimezoneId());
            } else {
                Optional<String> address = Optional.empty();

                Optional<String> streetAddress1 = Optional.ofNullable(loc.addr1());
                Optional<String> streetAddress2 = Optional.ofNullable(loc.addr2());
                Optional<String> city = Optional.ofNullable(loc.city());
                Optional<String> state = Optional.ofNullable(loc.state());
                Optional<String> postalCode = Optional.ofNullable(loc.zip());

                if (
                        streetAddress1.isPresent() &&
                        city.isPresent() &&
                        state.isPresent() &&
                        postalCode.isPresent()
                ) address = Optional.of(streetAddress1.get() +
                                        (streetAddress2.isPresent() && !streetAddress2.get().isBlank() ? ", " + streetAddress2.get() : "") +
                                        ", " + city.get() +
                                        ", " + state.get() +
                                        " " + postalCode.get());

                if (address.isEmpty())
                    throw new ResponseException("Unable to Geocode Address", "Unable to geocode address: " + s.stopOrder());

                LatLng coords = Geocoder.geocode(address.get());
                zone = TimeZones.lookup(coords, address.get());
                String ianaTimezoneID = zone.getId();

                a = new AddressRecord();
                a.setStreetAddress_1(streetAddress1.get().trim());
                a.setStreetAddress_2(streetAddress2.map(String::trim).orElse(""));
                a.setCity(city.get());
                a.setState(state.get());
                a.setZip(postalCode.get());
                a.setLatitude(BigDecimal.valueOf(coords.lat));
                a.setLongitude(BigDecimal.valueOf(coords.lng));
                a.setIanaTimezoneId(ianaTimezoneID);

                facility = new FacilitiesRecord();
                facility.setAddress(a);
                facility.setEdiFacilityId(id);
                facility.setEdiIntegrationType(IntegrationTypes.PRINCETON_TMX);
                facility.setDisplay(Objects.isNull(loc.name()) ? "Unnamed Facility " + UUID.randomUUID() : loc.name());
                facility = transaction.dsl().insertInto(Tables.FACILITIES)
                        .set(facility).returning().fetchAny();
                if (Objects.isNull(facility))
                    throw new ResponseException("Stop " + s.stopOrder() + ": facility does not exist and could not be created");
            }

            Optional<PrincetonTMXLoadTender.Reference> ref = Objects.isNull(s.refs()) ? Optional.empty() : s.refs().stream()
                    .filter(r -> "OQ".equals(r.referenceType()))
                    .findFirst();

            Map<String, String> meta = new HashMap<>();
            meta.put(Constants.Globals.LoadTenders.Meta.FACILITY, facility.getId().toString());
            ref.ifPresent(reference -> meta.put(Constants.Globals.LoadTenders.Meta.CUSTOMER_REF, reference.referenceNumber()));

            // handles appointments
            StopTypes type = switch (Objects.nonNull(s.stopType()) ? s.stopType() : "") {
                case "CL", "PL" -> StopTypes.PICKUP;
                case "CU", "PU" -> StopTypes.DROPOFF;
                default ->
                        throw new ResponseException("Stop " + s.stopOrder() + ": stop " + stopNumber + " does not have a valid type ('" + s.stopType() + "')");
            };
            List<String> dateList = Stream.of(
                            type == StopTypes.PICKUP ? s.pickupNetDateTime() : s.deliveryNetDateTime(),
                            type == StopTypes.PICKUP ? s.pickupNltDateTime() : s.deliveryNltDateTime(),
                            type == StopTypes.PICKUP ? s.pickupXatDateTime() : s.deliveryXatDateTime()
                    )
                    .filter(Objects::nonNull) // Filter out nulls
                    .sorted() // Sort the remaining dates (ISO strings can be sorted lexicographically)
                    .toList();

            // Get the earliest and latest dates
            String earliestDate = dateList.isEmpty() ? null : dateList.get(0);
            String latestDate = dateList.isEmpty() ? null : dateList.get(dateList.size() - 1);

            this.logger.debug(dateList.toString());
            this.logger.debug(earliestDate);
            this.logger.debug(latestDate);

            // build stop
            stop.setId(Integer.toString(stopNumber));
            stop.setEarliestArrival(Objects.isNull(earliestDate) ? null : OffsetDateTime.parse(earliestDate));
            stop.setLatestArrival(Objects.isNull(latestDate) ? null : OffsetDateTime.parse(latestDate));
            stop.setType(type);
            stop.setAddress(a);
            stop.setMeta(JSONB.valueOf((new Gson()).toJson(meta)));

            stops.add(stop);
        }
        version.setStops(stops.toArray(new LoadTenderStopRecord[0]));

        ArrayList<LoadTenderRevenueItemRecord> revenue = new ArrayList<>();
        if(Objects.nonNull(tender.rates())) for (PrincetonTMXLoadTender.Rate rate : tender.rates()) {
            LoadTenderRevenueItemRecord line = new LoadTenderRevenueItemRecord();
            line.setQuantity(1);
            line.setRate(Objects.nonNull(rate.totalCost()) ? BigDecimal.valueOf(rate.totalCost()) : BigDecimal.valueOf(0));
            revenue.add(line);
        }
        version.setRevenue(revenue.toArray(new LoadTenderRevenueItemRecord[0]));

        // done
        return version;
    }

}
