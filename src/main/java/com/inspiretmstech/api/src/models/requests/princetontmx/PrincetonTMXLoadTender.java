package com.inspiretmstech.api.src.models.requests.princetontmx;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PrincetonTMXLoadTender(
        @Nullable String commodity,
        @Nullable List<Note> notes,
        @Nullable String respondByDateTime,
        @Nullable String quantityUOM,
        @Nullable String controlCustomerNumber,
        @Nullable String equipmentType,
        @Nullable String tenderType,
        @Nullable Double freightRate,
        @Nullable String trailerPrefix,
        @Nullable String loadID,
        @Nullable String currency,
        @Nullable String carrierID,
        @Nullable Double quantity,
        @Nullable String contract,
        @Nullable String trailerNumber,
        @Nullable List<Rate> rates,
        @Nullable String length,
        @Nullable Double weight,
        @Nullable Integer totalMiles,
        @Nullable String driver,
        @Nullable List<Reference> refs,
        @Nullable String tenderStatus,
        @Nullable String methodOfPayment,
        @Nullable List<SpecialInstruction> specialInstructions,
        @Nullable List<Order> orders,
        @Nullable List<Stop> stops,
        @Nullable String loadReadyDateTime,
        @Nullable String weightUOM,
        @Nullable List<Contact> contacts
) {

    public record Note(
            @Nullable String note,
            @Nullable String noteType,
            @Nullable Integer order
    ) {}

    public record Rate(
            @Nullable String rateType,
            @Nullable String carrier,
            @Nullable String fsUom,
            @Nullable Double rate,
            @Nullable String currency,
            @Nullable Double fuelSurcharge,
            @Nullable String rateUom,
            @Nullable Double totalCost,
            @Nullable Double additionalCost
    ) {}

    public record Reference(
            @Nullable String referenceNumber,
            @Nullable String referenceType
    ) {}

    public record SpecialInstruction(
            @Nullable String qualifier
    ) {}

    public record Order(
            @Nullable String pickupNetDateTime,
            @Nullable List<LineItem> lineItems,
            @Nullable String deliveryNltDateTime,
            @Nullable String orderNumber,
            @Nullable List<Note> notes,
            @Nullable List<Reference> refs,
            @Nullable String deliveryNetDateTime,
            @Nullable String pickupNltDateTime
    ) {}

    public record LineItem(
            @Nullable List<Measure> measures,
            @Nullable List<Note> notes,
            @Nullable List<Reference> refs,
            @Nullable Integer lineItemSequence
    ) {}

    public record Measure(
            @Nullable String uom,
            @Nullable String qualifier,
            @Nullable Double value,
            @Nullable String significanceCode
    ) {}

    public record Stop(
            @Nullable List<Note> notes,
            @Nullable String deliveryXatDateTime,
            @Nullable String quantityUOM,
            @Nullable String deliveryNetDateTime,
            @Nullable String stopType,
            @Nullable Integer stopOrder,
            @Nullable String deliveryApptDateTime,
            @Nullable String pickupNetDateTime,
            @Nullable String actArrivalDateTime,
            @Nullable String deliveryNltDateTime,
            @Nullable List<Reference> refs,
            @Nullable String actDepartDateTime,
            @Nullable String pickupNltDateTime,
            @Nullable String weightUOM,
            @Nullable String pickupXatDateTime,
            @Nullable List<LocInfo> locInfos
    ) {}

    public record LocInfo(
            @Nullable String zip,
            @Nullable String country,
            @Nullable String addr2,
            @Nullable String city,
            @Nullable String addr1,
            @Nullable String addr4,
            @Nullable String addr3,
            @Nullable String county,
            @Nullable String locationID,
            @Nullable String name,
            @Nullable String state,
            @Nullable List<Contact> contacts,
            @Nullable String customer
    ) {}

    public record Contact(
            @Nullable String contactData,
            @Nullable String contactName,
            @Nullable String contactType,
            @Nullable String dataType
    ) {}
}
