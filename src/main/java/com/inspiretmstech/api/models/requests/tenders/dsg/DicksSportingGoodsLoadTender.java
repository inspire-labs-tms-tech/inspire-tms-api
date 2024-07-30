package com.inspiretmstech.api.models.requests.tenders.dsg;

import java.util.List;

public class DicksSportingGoodsLoadTender {

    public static record Shipment(
            Data data,
            Meta meta
    ) {}

    public static record Data(
            Section1 section1,
            Section2 section2,
            Section3 section3
    ) {}

    public static record Section1(
            BeginningSegmentForShipmentInformationTransaction beginningSegmentForShipmentInformationTransaction,
            DateOrTime dateOrTime,
            List<Object> groupEquipmentDetails_1200,
            List<Object> groupName_1140,
            List<NoteOrSpecialInstruction> noteOrSpecialInstruction,
            SetPurpose setPurpose
    ) {}

    public static record BeginningSegmentForShipmentInformationTransaction(
            String shipmentIdentificationNumber,
            String shipmentMethodOfPayment,
            String standardCarrierAlphaCode,
            String standardPointLocationCode,
            String tariffServiceCode,
            String weightUnitCode
    ) {}

    public static record DateOrTime(
            String date,
            String dateQualifier,
            String time,
            String timeQualifier
    ) {}

    public static record NoteOrSpecialInstruction(
            String description,
            String noteReferenceCode
    ) {}

    public static record SetPurpose(
            String transactionSetPurposeCode
    ) {}

    public static record Section2(
            List<GroupStopOffDetails_210> groupStopOffDetails_210
    ) {}

    public static record GroupStopOffDetails_210(
            List<BusinessInstructionsAndReferenceNumber> businessInstructionsAndReferenceNumber,
            List<DateOrTime> dateOrTime,
            GroupName_270 groupName_270,
            List<Object> noteOrSpecialInstruction,
            ShipmentWeightPackagingAndQuantityData shipmentWeightPackagingAndQuantityData,
            StopOffDetails stopOffDetails
    ) {}

    public static record BusinessInstructionsAndReferenceNumber(
            String referenceIdentification,
            String referenceIdentificationQualifier
    ) {}

    public static record GroupName_270(
            List<AddressInformation> addressInformation,
            List<Contact> contact,
            GeographicLocation geographicLocation,
            Name name
    ) {}

    public static record AddressInformation(
            String addressInformation,
            String addressInformation_1
    ) {}

    public static record Contact(
            String communicationNumber,
            String communicationNumberQualifier,
            String contactFunctionCode,
            String name
    ) {}

    public static record GeographicLocation(
            String cityName,
            String postalCode,
            String stateOrProvinceCode
    ) {}

    public static record Name(
            String entityIdentifierCode,
            String identificationCode,
            String identificationCodeQualifier,
            String name
    ) {}

    public static record ShipmentWeightPackagingAndQuantityData(
            int ladingQuantity,
            int ladingQuantity_1,
            Double volume,
            String volumeUnitQualifier,
            double weight,
            String weightQualifier,
            String weightUnitCode
    ) {}

    public static record StopOffDetails(
            String stopReasonCode,
            int stopSequenceNumber
    ) {}

    public static record Section3(
            TotalWeightAndCharges totalWeightAndCharges
    ) {}

    public static record TotalWeightAndCharges(
            int advances,
            long charge,
            double freightRate,
            int ladingQuantity,
            int prepaidAmount,
            String rateOrValueQualifier,
            String specialChargeOrAllowanceCode,
            double volume,
            String volumeUnitQualifier,
            double weight,
            String weightQualifier
    ) {}

    public static record Meta(
            String file_name,
            String group_functional_identifier,
            String group_seq_number,
            String message_seq_number,
            String set_seq_number
    ) {}
}
