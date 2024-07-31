package com.inspiretmstech.api.controllers.v1;

import com.google.maps.model.LatLng;
import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.models.ResponseException;
import com.inspiretmstech.api.models.requests.tenders.gp.GeorgiaPacificLoadTender;
import com.inspiretmstech.api.models.responses.IDResponse;
import com.inspiretmstech.api.utils.Geocoder;
import com.inspiretmstech.api.utils.TimeZones;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.enums.LoadTenderStatus;
import com.inspiretmstech.db.enums.StopTypes;
import com.inspiretmstech.db.tables.records.IntegrationsRecord;
import com.inspiretmstech.db.tables.records.LoadTenderVersionsRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.udt.records.AddressRecord;
import com.inspiretmstech.db.udt.records.LoadTenderRevenueItemRecord;
import com.inspiretmstech.db.udt.records.LoadTenderStopRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Tag(name = "Georgia Pacific", description = "Webhook for Georgia Pacific")
@RequestMapping("/v1/georgia-pacific")
public class GeorgiaPacificLoadTenderController {

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Tender a Load")
    @PostMapping
    public IDResponse webhook(@RequestBody List<GeorgiaPacificLoadTender.Shipment> shipments) throws Exception {

        Optional<IntegrationsRecord> gp = PostgresConnection.getInstance().with(supabase ->
                supabase.selectFrom(Tables.INTEGRATIONS).where(Tables.INTEGRATIONS.TYPE.eq(IntegrationTypes.GEORGIA_PACIFIC)).fetchOne());

        if (gp.isEmpty()) throw new ResponseException("Georgia Pacific Integration Not Installed");
        if (Objects.isNull(gp.get().getGeorgiaPacificCustomerId()))
            throw new ResponseException("Georgia Pacific Integration Not Setup", "Customer Not Mapped");

        Optional<IDResponse> successful = Optional.empty();
        for (GeorgiaPacificLoadTender.Shipment shipment : shipments)
            successful = PostgresConnection.getInstance().unsafely(supabase -> {
                AtomicReference<IDResponse> newVersion = new AtomicReference<>();
                supabase.transaction(transaction -> {

                    LoadTendersRecord _tender = new LoadTendersRecord();
                    _tender.setCustomerId(gp.get().getGeorgiaPacificCustomerId());
                    _tender.setOriginalCustomerReferenceNumber(shipment.data().section1().beginningSegmentForShipmentInformationTransaction().shipmentIdentificationNumber());
                    _tender.setIntegrationType(IntegrationTypes.GEORGIA_PACIFIC);
                    _tender.setStatus(LoadTenderStatus.NEW);

                    // create the load tender
                    Optional<LoadTendersRecord> tender = Optional.ofNullable(transaction.dsl()
                            .insertInto(Tables.LOAD_TENDERS)
                            .set(_tender)
                            .onConflict(Tables.LOAD_TENDERS.ORIGINAL_CUSTOMER_REFERENCE_NUMBER)
                            .doUpdate()
                            .set(_tender)
                            .returning()
                            .fetchOne());
                    if (tender.isEmpty()) throw new ResponseException("Unable to Create Load Tender!");

                    // create the stops
                    int stopNum = 0;
                    List<LoadTenderStopRecord> stops = new ArrayList<>();
                    for (GeorgiaPacificLoadTender.GroupStopOffDetails210 stop : shipment.data().section2().groupStopOffDetails_210()) {
                        stopNum++;

                        LoadTenderStopRecord _stop = new LoadTenderStopRecord();

                        Optional<String> address = Optional.empty();
                        Optional<String> streetAddress1 = Optional.empty();
                        Optional<String> streetAddress2 = Optional.empty();
                        Optional<String> city = Optional.empty();
                        Optional<String> state = Optional.empty();
                        Optional<String> postalCode = Optional.empty();
                        if (Objects.nonNull(stop.groupName_270()) && Objects.nonNull(stop.groupName_270().addressInformation()) && Objects.nonNull(stop.groupName_270().geographicLocation())) {

                            city = Optional.ofNullable(stop.groupName_270().geographicLocation().cityName());
                            state = Optional.ofNullable(stop.groupName_270().geographicLocation().stateOrProvinceCode());
                            postalCode = Optional.ofNullable(stop.groupName_270().geographicLocation().postalCode());

                            for (GeorgiaPacificLoadTender.AddressInformation addressInformation : stop.groupName_270().addressInformation()) {
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
                            throw new ResponseException("Unable to Geocode Address", "Unable to geocode address: " + stop.groupName_270());

                        LatLng coords = Geocoder.geocode(address.get());
                        ZoneId zone = TimeZones.lookup(coords, address.get());
                        String ianaTimezoneID = zone.getId();

                        AddressRecord a = new AddressRecord();
                        a.setStreetAddress_1(streetAddress1.get().trim());
                        a.setStreetAddress_2(streetAddress2.map(String::trim).orElse(""));
                        a.setCity(city.get());
                        a.setState(state.get());
                        a.setZip(postalCode.get());
                        a.setLatitude(BigDecimal.valueOf(coords.lat));
                        a.setLongitude(BigDecimal.valueOf(coords.lng));
                        a.setIanaTimezoneId(ianaTimezoneID);

                        // use stop number by default
                        GeorgiaPacificLoadTender.BusinessInstructionsAndReferenceNumber num = new GeorgiaPacificLoadTender.BusinessInstructionsAndReferenceNumber("", "" + stopNum, "");
                        if (Objects.nonNull(stop.businessInstructionsAndReferenceNumber()))
                            for (GeorgiaPacificLoadTender.BusinessInstructionsAndReferenceNumber ref : stop.businessInstructionsAndReferenceNumber())
                                if (Objects.nonNull(ref.referenceIdentificationQualifier()) &&
                                    ref.referenceIdentificationQualifier().equalsIgnoreCase("QN") &&
                                    Objects.nonNull(ref.referenceIdentification())
                                ) num = ref;

                        _stop.setId(num.referenceIdentification());

                        // get appointment times
                        List<String> scheduledCodes = Arrays.asList("10", "78", "70", "68", "69");
                        List<String> earliestCodes = Arrays.asList("37", "53", "EP", "LC");
                        List<String> latestCodes = Arrays.asList("54", "LP", "CL", "38");
                        List<String> pickupCodes = Arrays.asList("37", "38", "CL", "LC", "10", "EP", "LP", "69");
                        List<String> deliveryCodes = Arrays.asList("53", "54", "68", "70", "78");
                        Optional<GeorgiaPacificLoadTender.DateOrTime> scheduled = Objects.isNull(stop.dateOrTime()) ? Optional.empty() :
                                stop.dateOrTime().stream().filter(i -> {
                                            for (String code : scheduledCodes)
                                                if (code.equalsIgnoreCase(i.dateQualifier()))
                                                    return true;
                                            return false;
                                        }
                                ).findFirst();
                        Optional<GeorgiaPacificLoadTender.DateOrTime> _earliest = scheduled.isPresent() ? scheduled : Objects.isNull(stop.dateOrTime()) ? Optional.empty() :
                                stop.dateOrTime().stream().filter(i -> {
                                            for (String code : earliestCodes)
                                                if (code.equalsIgnoreCase(i.dateQualifier()))
                                                    return true;
                                            return false;
                                        }
                                ).findFirst();
                        Optional<GeorgiaPacificLoadTender.DateOrTime> _latest = scheduled.isPresent() ? scheduled : Objects.isNull(stop.dateOrTime()) ? Optional.empty() :
                                stop.dateOrTime().stream().filter(i -> {
                                            for (String code : latestCodes)
                                                if (code.equalsIgnoreCase(i.dateQualifier()))
                                                    return true;
                                            return false;
                                        }
                                ).findFirst();

                        // it is possible to only have an earliest or a latest
                        if(_earliest.isPresent() && _latest.isEmpty()) _latest = _earliest;
                        if(_earliest.isEmpty() && _latest.isPresent()) _earliest = _latest;

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

                        Optional<GeorgiaPacificLoadTender.DateOrTime> final_earliest = _earliest;
                        StopTypes type =
                                stopNum == 1 ? StopTypes.PICKUP :
                                        stopNum == stops.size() ? StopTypes.DROPOFF :
                                                pickupCodes.stream().anyMatch(code -> code.equalsIgnoreCase(final_earliest.get().dateQualifier())) ? StopTypes.PICKUP :
                                                        StopTypes.DROPOFF;

                        _stop.setEarliestArrival(earliest);
                        _stop.setLatestArrival(latest);
                        _stop.setType(type);
                        _stop.setAddress(a);

                        stops.add(_stop);
                    }

                    // build the notes
                    StringBuilder notes = new StringBuilder();
                    if (Objects.nonNull(shipment.data().section1().groupEquipmentDetails_1200()))
                        for (GeorgiaPacificLoadTender.GroupEquipmentDetails1200 groupEquipmentDetails1200 : shipment.data().section1().groupEquipmentDetails_1200())
                            if (Objects.nonNull(groupEquipmentDetails1200.equipmentDetails()) && Objects.nonNull(groupEquipmentDetails1200.equipmentDetails().equipmentLength()))
                                notes.append("Equipment Length: ").append(groupEquipmentDetails1200.equipmentDetails().equipmentLength() / 100).append("\n");
                    if (Objects.nonNull(shipment.data().section1().noteOrSpecialInstruction()))
                        for (GeorgiaPacificLoadTender.NoteOrSpecialInstruction note : shipment.data().section1().noteOrSpecialInstruction())
                            notes.append(note.description()).append("\n");


                    // Build the Version
                    LoadTenderVersionsRecord version = new LoadTenderVersionsRecord();
                    version.setCustomerReferenceNumber(shipment.data().section1().beginningSegmentForShipmentInformationTransaction().shipmentIdentificationNumber());
                    version.setAcceptWebhook("#");
                    version.setDeclineWebhook("#");
                    version.setLoadTenderId(tender.get().getId());
                    version.setRevenue(new LoadTenderRevenueItemRecord[0]); // GP does not send revenue in 204s
                    version.setStops(stops.toArray(new LoadTenderStopRecord[0]));
                    version.setNotes(notes.toString());

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
