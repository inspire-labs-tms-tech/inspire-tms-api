package com.inspiretmstech.api.src.models.requests.tenders.dsg;

import java.util.List;

public class DicksSportingGoodsLoadTender {

    public record Shipment(
            Data data,
            Meta meta
    ) {}

    public record Data(
            Section1 section1,
            Section2 section2,
            Section3 section3
    ) {}

    public record Section1(
            BeginningSegmentForShipmentInformationTransaction beginningSegmentForShipmentInformationTransaction,
            DateOrTime dateOrTime,
            List<Object> groupEquipmentDetails_1200,
            List<Object> groupName_1140,
            List<NoteOrSpecialInstruction> noteOrSpecialInstruction,
            SetPurpose setPurpose
    ) {}

    public record BeginningSegmentForShipmentInformationTransaction(
            String shipmentIdentificationNumber,
            String shipmentMethodOfPayment,
            String standardCarrierAlphaCode,
            String standardPointLocationCode,
            String tariffServiceCode,
            String weightUnitCode
    ) {}

    public record DateOrTime(
            String date,
            String dateQualifier,
            String time,
            String timeQualifier
    ) {}

    public record NoteOrSpecialInstruction(
            String description,
            String noteReferenceCode
    ) {}

    public record SetPurpose(
            String transactionSetPurposeCode
    ) {}

    public record Section2(
            List<GroupStopOffDetails_210> groupStopOffDetails_210
    ) {}

    public record GroupStopOffDetails_210(
            List<BusinessInstructionsAndReferenceNumber> businessInstructionsAndReferenceNumber,
            List<DateOrTime> dateOrTime,
            GroupName_270 groupName_270,
            List<Object> noteOrSpecialInstruction,
            ShipmentWeightPackagingAndQuantityData shipmentWeightPackagingAndQuantityData,
            StopOffDetails stopOffDetails
    ) {}

    public record BusinessInstructionsAndReferenceNumber(
            String referenceIdentification,
            String referenceIdentificationQualifier
    ) {}

    public record GroupName_270(
            List<AddressInformation> addressInformation,
            List<Contact> contact,
            GeographicLocation geographicLocation,
            Name name
    ) {}

    public record AddressInformation(
            String addressInformation,
            String addressInformation_1
    ) {}

    public record Contact(
            String communicationNumber,
            String communicationNumberQualifier,
            String contactFunctionCode,
            String name
    ) {}

    public record GeographicLocation(
            String cityName,
            String postalCode,
            String stateOrProvinceCode
    ) {}

    public record Name(
            String entityIdentifierCode,
            String identificationCode,
            String identificationCodeQualifier,
            String name
    ) {}

    public record ShipmentWeightPackagingAndQuantityData(
            int ladingQuantity,
            int ladingQuantity_1,
            Double volume,
            String volumeUnitQualifier,
            double weight,
            String weightQualifier,
            String weightUnitCode
    ) {}

    public record StopOffDetails(
            String stopReasonCode,
            int stopSequenceNumber
    ) {}

    public record Section3(
            TotalWeightAndCharges totalWeightAndCharges
    ) {}

    public record TotalWeightAndCharges(
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

    public record Meta(
            String file_name,
            String group_functional_identifier,
            String group_seq_number,
            String message_seq_number,
            String set_seq_number
    ) {}
}
