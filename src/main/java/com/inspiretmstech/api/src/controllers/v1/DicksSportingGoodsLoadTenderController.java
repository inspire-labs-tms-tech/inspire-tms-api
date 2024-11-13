package com.inspiretmstech.api.src.controllers.v1;

import com.google.maps.model.LatLng;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.constants.Constants;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.requests.tenders.dsg.DicksSportingGoodsLoadTender;
import com.inspiretmstech.api.src.models.responses.IDResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Geocoder;
import com.inspiretmstech.common.utils.TimeZones;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.enums.LoadTenderStatus;
import com.inspiretmstech.db.enums.StopTypes;
import com.inspiretmstech.db.tables.records.FacilitiesRecord;
import com.inspiretmstech.db.tables.records.IntegrationsRecord;
import com.inspiretmstech.db.tables.records.LoadTenderVersionsRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.udt.records.AddressRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.JSONB;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Tag(name = "Dicks Sporting Goods", description = "Webhook for Dicks Sporting Goods")
@RequestMapping("/v1/dicks-sporting-goods")
public class DicksSportingGoodsLoadTenderController {

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Tender a Load")
    @PostMapping
    public IDResponse webhook(@RequestBody List<DicksSportingGoodsLoadTender.Shipment> tenders) throws Exception {

        Optional<IntegrationsRecord> dsg = PostgresConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.INTEGRATIONS).where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.DSG)).fetchOne());

        if (dsg.isEmpty()) throw new ResponseException("Dicks Sporting Goods Integration Not Installed");
        if (Objects.isNull(dsg.get().getDsgCustomerId()))
            throw new ResponseException("Dicks Sporting Goods Integration Not Setup", "Customer Not Mapped");

        Optional<IDResponse> successful = Optional.empty();
        for (DicksSportingGoodsLoadTender.Shipment tender : tenders)
            successful = PostgresConnection.getInstance().unsafely(supabase -> {
                AtomicReference<IDResponse> newVersion = new AtomicReference<>();
                supabase.transaction(transaction -> {

                    LoadTendersRecord _tender = new LoadTendersRecord();
                    _tender.setCustomerId(dsg.get().getDsgCustomerId());
                    _tender.setOriginalCustomerReferenceNumber(tender.data().section1().beginningSegmentForShipmentInformationTransaction().shipmentIdentificationNumber());
                    _tender.setIntegrationType(IntegrationTypes.DSG);
                    _tender.setStatus(LoadTenderStatus.NEW);

                    // create the load tender
                    Optional<LoadTendersRecord> newTender = Optional.ofNullable(transaction.dsl()
                            .insertInto(Tables.LOAD_TENDERS)
                            .set(_tender)
                            .onConflict(Tables.LOAD_TENDERS.UID)
                            .doUpdate()
                            .set(_tender)
                            .returning()
                            .fetchOne());
                    if (newTender.isEmpty()) throw new ResponseException("Unable to Create Load Tender!");

                    // lists
                    List<LoadTenderStopRecord> stops = new ArrayList<>();
                    List<LoadTenderRevenueItemRecord> revenue = new ArrayList<>();

                    // create the revenue
                    if (Objects.nonNull(tender.data().section3().totalWeightAndCharges()) && Objects.nonNull(tender.data().section3().totalWeightAndCharges().charge()))
                        revenue.add(new LoadTenderRevenueItemRecord(1, BigDecimal.valueOf(tender.data().section3().totalWeightAndCharges().charge()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));

                    // create the stops
                    int stopNum = 0;
                    for (DicksSportingGoodsLoadTender.GroupStopOffDetails_210 tenderStop : tender.data().section2().groupStopOffDetails_210()) {
                        stopNum++;

                        LoadTenderStopRecord stop = new LoadTenderStopRecord();
                        AddressRecord a;
                        ZoneId zone;

                        // search for existing facility
                        FacilitiesRecord facility;
                        String id = tenderStop.groupName_270().name().entityIdentifierCode() + "-" + tenderStop.groupName_270().name().identificationCode() + "-" + tenderStop.groupName_270().name().identificationCodeQualifier();
                        Optional<FacilitiesRecord> existingFacility = Optional.ofNullable(transaction.dsl()
                                .selectFrom(Tables.FACILITIES)
                                .where(Tables.FACILITIES.EDI_FACILITY_ID.eq(id))
                                .and(Tables.FACILITIES.EDI_INTEGRATION_TYPE.eq(IntegrationTypes.DSG))
                                .fetchOne());
                        if (existingFacility.isPresent()) {
                            facility = existingFacility.get();
                            a = facility.getAddress();
                            zone = ZoneId.of(a.getIanaTimezoneId());
                        } else {
                            Optional<String> address = Optional.empty();

                            Optional<String> streetAddress1 = Optional.empty();
                            Optional<String> streetAddress2 = Optional.empty();
                            Optional<String> city = Optional.empty();
                            Optional<String> state = Optional.empty();
                            Optional<String> postalCode = Optional.empty();

                            if (Objects.nonNull(tenderStop.groupName_270()) && Objects.nonNull(tenderStop.groupName_270().addressInformation()) && Objects.nonNull(tenderStop.groupName_270().geographicLocation())) {
                                city = Optional.ofNullable(tenderStop.groupName_270().geographicLocation().cityName());
                                state = Optional.ofNullable(tenderStop.groupName_270().geographicLocation().stateOrProvinceCode());
                                postalCode = Optional.ofNullable(tenderStop.groupName_270().geographicLocation().postalCode());

                                for (DicksSportingGoodsLoadTender.AddressInformation addressInformation : tenderStop.groupName_270().addressInformation()) {
                                    if (Objects.nonNull(addressInformation.addressInformation()) && !addressInformation.addressInformation().isBlank())
                                        streetAddress1 = Optional.of(addressInformation.addressInformation());
                                    if (Objects.nonNull(addressInformation.addressInformation_1()) && !addressInformation.addressInformation_1().isBlank())
                                        streetAddress2 = Optional.of(addressInformation.addressInformation_1());
                                }

                                if (streetAddress1.isPresent() && city.isPresent() && state.isPresent() && postalCode.isPresent()) {
                                    String _address = streetAddress1.get().trim() + ", ";
                                    if (streetAddress2.isPresent()) _address += streetAddress2.get().trim() + ", ";
                                    _address += city.get().trim() + ", " + state.get().trim() + " " + postalCode.get().trim();
                                    address = Optional.of(_address);
                                }
                            }

                            if (address.isEmpty())
                                throw new ResponseException("Unable to Geocode Address", "Unable to geocode address: " + tenderStop.groupName_270());

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
                            facility.setEdiIntegrationType(IntegrationTypes.DSG);
                            facility.setDisplay("(DSG) " + tenderStop.groupName_270().name().name());
                            facility = transaction.dsl().insertInto(Tables.FACILITIES)
                                    .set(facility).returning().fetchAny();
                            if (Objects.isNull(facility))
                                throw new ResponseException("Stop " + stopNum + ": facility does not exist and could not be created");
                        }

                        // get the stop ID
                        Optional<DicksSportingGoodsLoadTender.BusinessInstructionsAndReferenceNumber> stopRef = tenderStop.businessInstructionsAndReferenceNumber().stream().filter(i -> i.referenceIdentificationQualifier().equalsIgnoreCase("QN")).findFirst();
                        if (stopRef.isEmpty() || stopRef.get().referenceIdentification().isBlank())
                            throw new ResponseException("Stop " + stopNum + " Missing Unique ID ('QN')");
                        String stopID = stopRef.get().referenceIdentification();


                        // get appointments
                        Optional<DicksSportingGoodsLoadTender.DateOrTime> scheduled = Objects.isNull(tenderStop.dateOrTime()) ? Optional.empty() :
                                tenderStop.dateOrTime().stream().filter(i -> i.timeQualifier().equalsIgnoreCase("X")).findFirst();
                        Optional<DicksSportingGoodsLoadTender.DateOrTime> _earliest = scheduled.isPresent() ? scheduled : Objects.isNull(tenderStop.dateOrTime()) ? Optional.empty() :
                                tenderStop.dateOrTime().stream().filter(i -> i.timeQualifier().equalsIgnoreCase("G") || i.timeQualifier().equalsIgnoreCase("I")).findFirst();
                        Optional<DicksSportingGoodsLoadTender.DateOrTime> _latest = scheduled.isPresent() ? scheduled : Objects.isNull(tenderStop.dateOrTime()) ? Optional.empty() :
                                tenderStop.dateOrTime().stream().filter(i -> i.timeQualifier().equalsIgnoreCase("K") || i.timeQualifier().equalsIgnoreCase("L")).findFirst();

                        // it is possible to only have an earliest or a latest
                        if (_earliest.isPresent() && _latest.isEmpty()) _latest = _earliest;
                        if (_earliest.isEmpty() && _latest.isPresent()) _earliest = _latest;

                        if (_earliest.isEmpty() || Objects.isNull(_earliest.get().date()) || Objects.isNull(_earliest.get().time()))
                            throw new ResponseException("Invalid Appointment", "The appointment on stop " + stopNum + " is invalid", "Earliest appointment is missing");
                        if (_latest.isEmpty() || Objects.isNull(_latest.get().date()) || Objects.isNull(_latest.get().time()))
                            throw new ResponseException("Invalid Appointment", "The appointment on stop " + stopNum + " is invalid", "Latest appointment is missing");

                        // 	ZonedDateTime.of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, ZoneId zone)

                        OffsetDateTime earliest = ZonedDateTime.of(
                                Integer.parseInt(_earliest.get().date().substring(0, 4)), // 20241218 -> 2024
                                Integer.parseInt(_earliest.get().date().substring(4, 6)), // 20241218 -> 12
                                Integer.parseInt(_earliest.get().date().substring(6, 8)), // 20241218 -> 18
                                Integer.parseInt(_earliest.get().time().substring(0, 2)), // 1345 -> 13
                                Integer.parseInt(_earliest.get().time().substring(2, 4)), // 1345 -> 45
                                0, // no seconds
                                0, // no nanoseconds
                                zone
                        ).toOffsetDateTime();

                        OffsetDateTime latest = ZonedDateTime.of(
                                Integer.parseInt(_latest.get().date().substring(0, 4)), // 20241218 -> 2024
                                Integer.parseInt(_latest.get().date().substring(4, 6)), // 20241218 -> 12
                                Integer.parseInt(_latest.get().date().substring(6, 8)), // 20241218 -> 18
                                Integer.parseInt(_latest.get().time().substring(0, 2)), // 1345 -> 13
                                Integer.parseInt(_latest.get().time().substring(2, 4)), // 1345 -> 45
                                0, // no seconds
                                0, // no nanoseconds
                                zone
                        ).toOffsetDateTime();


                        // get the type
                        StopTypes type =
                                stopNum == 1 ? StopTypes.PICKUP :
                                        stopNum == stops.size() ? StopTypes.DROPOFF :
                                                (_earliest.get().timeQualifier().equalsIgnoreCase("I") || _latest.get().timeQualifier().equalsIgnoreCase("K")) ? StopTypes.PICKUP :
                                                        StopTypes.DROPOFF;

                        // build stop
                        stop.setId(stopID);
                        stop.setEarliestArrival(earliest);
                        stop.setLatestArrival(latest);
                        stop.setType(type);
                        stop.setAddress(a);
                        stop.setMeta(JSONB.valueOf("{\"facility_id\": \"" + facility.getId().toString() + "\"}"));

                        stops.add(stop);
                    }

                    // build the notes
                    StringBuilder notes = new StringBuilder();
                    if (Objects.nonNull(tender.data().section1().noteOrSpecialInstruction()))
                        for (DicksSportingGoodsLoadTender.NoteOrSpecialInstruction note : tender.data().section1().noteOrSpecialInstruction())
                            notes.append(Objects.nonNull(note.noteReferenceCode()) ? note.noteReferenceCode() : "NOTE")
                                    .append(": ")
                                    .append(note.description())
                                    .append("\n");

                    // build the new version
                    LoadTenderVersionsRecord version = new LoadTenderVersionsRecord();
                    version.setCustomerReferenceNumber(tender.data().section1().beginningSegmentForShipmentInformationTransaction().shipmentIdentificationNumber());
                    version.setAcceptWebhook(Constants.Globals.LoadTenders.NO_WEBHOOK_CALLBACK);
                    version.setDeclineWebhook(Constants.Globals.LoadTenders.NO_WEBHOOK_CALLBACK);
                    version.setLoadTenderId(newTender.get().getId());
                    version.setRevenue(revenue.toArray(new LoadTenderRevenueItemRecord[0])); // GP does not send revenue in 204s
                    version.setStops(stops.toArray(new LoadTenderStopRecord[0]));
                    version.setNotes(notes.toString());

                    // save the new version
                    Optional<LoadTenderVersionsRecord> v = Optional.ofNullable(transaction.dsl().insertInto(Tables.LOAD_TENDER_VERSIONS)
                            .set(version)
                            .returning()
                            .fetchOne());
                    v.ifPresent(loadTenderVersionsRecord -> newVersion.set(new IDResponse(loadTenderVersionsRecord.getId().toString())));
                });
                return newVersion.get();
            });

        if (successful.isPresent()) return successful.get();
        throw new ResponseException("An Unexpected Error Occurred!", "An error was not thrown, but the load tender version could not be created.");
    }

}
