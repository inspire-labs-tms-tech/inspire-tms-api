package com.inspiretmstech.api.src.models.requests.princetontmx;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PrincetonTMXLoadTender(
        @Nullable String commodity,
        @Nullable List<Note> notes,
        @Nullable String respondByDate,
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
        @Nullable Integer totalDistance,
        @Nullable String driver,
        @Nullable List<Reference> refs,
        @Nullable String tenderStatus,
        @Nullable String methodOfPayment,
        @Nullable List<SpecialInstruction> specialInstructions,
        @Nullable List<Order> orders,
        @Nullable List<Stop> stops,
        @Nullable String loadReadyDate,
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
            @Nullable String pickupNetDate,
            @Nullable List<LineItem> lineItems,
            @Nullable String deliveryNltDate,
            @Nullable String orderNumber,
            @Nullable List<Note> notes,
            @Nullable List<Reference> refs,
            @Nullable String deliveryNetDate,
            @Nullable String pickupNltDate
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
            @Nullable String deliveryXatDate,
            @Nullable String quantityUOM,
            @Nullable String deliveryNetDate,
            @Nullable String stopType,
            @Nullable Integer stopOrder,
            @Nullable String deliveryApptDate,
            @Nullable String pickupNetDate,
            @Nullable String actArrivalDateTime,
            @Nullable String deliveryNltDate,
            @Nullable List<Reference> refs,
            @Nullable String actDepartDateTime,
            @Nullable String pickupNltDate,
            @Nullable String weightUOM,
            @Nullable String pickupXatDate,
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
