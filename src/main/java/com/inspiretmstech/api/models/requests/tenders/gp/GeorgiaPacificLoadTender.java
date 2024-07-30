package com.inspiretmstech.api.models.requests.tenders.gp;

import java.util.List;

public class GeorgiaPacificLoadTender {

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
            List<BillOfLadingHandlingRequirements> billOfLadingHandlingRequirements,
            List<BusinessInstructionsAndReferenceNumber> businessInstructionsAndReferenceNumber,
            DateOrTime dateOrTime,
            List<GroupEquipmentDetails1200> groupEquipmentDetails_1200,
            List<GroupName1140> groupName_1140,
            List<HazardousCertification> hazardousCertification,
            InterlineInformation interlineInformation,
            List<NoteOrSpecialInstruction> noteOrSpecialInstruction,
            PalletInformation palletInformation,
            SetPurpose setPurpose
    ) {}

    public record BeginningSegmentForShipmentInformationTransaction(
            String customsDocumentationHandlingCode,
            String shipmentIdentificationNumber,
            String shipmentMethodOfPayment,
            String standardCarrierAlphaCode,
            String weightUnitCode
    ) {}

    public record BillOfLadingHandlingRequirements(
            String specialHandlingCode,
            String specialHandlingDescription,
            String specialServicesCode
    ) {}

    public record BusinessInstructionsAndReferenceNumber(
            String description,
            String referenceIdentification,
            String referenceIdentificationQualifier
    ) {}

    public record DateOrTime(
            String date,
            String dateQualifier,
            String time,
            String timeCode,
            String timeQualifier
    ) {}

    public record GroupEquipmentDetails1200(
            EquipmentDetails equipmentDetails,
            List<SealNumbers> sealNumbers
    ) {}

    public record EquipmentDetails(
            String equipmentDescriptionCode,
            int equipmentLength,
            String equipmentNumber,
            double height
    ) {}

    public record SealNumbers(
            String sealNumber,
            String sealNumber_1,
            String sealNumber_2,
            String sealNumber_3
    ) {}

    public record GroupName1140(
            List<AddressInformation> addressInformation,
            List<Contact> contact,
            GeographicLocation geographicLocation
            // String name // BUG FIX: REMOVED (NOT NEEDED ANYWAY)
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
            String countryCode,
            String postalCode,
            String stateOrProvinceCode
    ) {}

    public record HazardousCertification(
            String name
    ) {}

    public record InterlineInformation(
            String routingSequenceCode,
            String standardCarrierAlphaCode,
            String transportationMethodOrTypeCode
    ) {}

    public record NoteOrSpecialInstruction(
            String description
    ) {}

    public record PalletInformation(
            int quantityOfPalletsShipped
    ) {}

    public record SetPurpose(
            String applicationType,
            String transactionSetPurposeCode
    ) {}

    public record Section2(
            List<GroupStopOffDetails210> groupStopOffDetails_210
    ) {}

    public record GroupStopOffDetails210(
            List<BillOfLadingHandlingRequirements> billOfLadingHandlingRequirements,
            List<BusinessInstructionsAndReferenceNumber> businessInstructionsAndReferenceNumber,
            List<DateOrTime> dateOrTime,
            GroupName270 groupName_270,
            List<GroupOrderIdentificationDetail2150> groupOrderIdentificationDetail_2150,
            List<NoteOrSpecialInstruction> noteOrSpecialInstruction,
            PalletInformation palletInformation,
            ShipmentWeightPackagingAndQuantityData shipmentWeightPackagingAndQuantityData,
            StopOffDetails stopOffDetails
    ) {}

    public record GroupName270(
            List<AddressInformation> addressInformation,
            List<Contact> contact,
            GeographicLocation geographicLocation,
            Name name
    ) {}

    public record Name(
            String entityIdentifierCode,
            String identificationCode,
            String identificationCodeQualifier,
            String name
    ) {}

    public record GroupOrderIdentificationDetail2150(
            List<GroupDescriptionMarksAndNumbers2190> groupDescriptionMarksAndNumbers_2190,
            OrderIdentificationDetail orderIdentificationDetail
    ) {}

    public record GroupDescriptionMarksAndNumbers2190(
            DescriptionMarksAndNumbers descriptionMarksAndNumbers
    ) {}

    public record DescriptionMarksAndNumbers(
            String ladingDescription,
            int ladingLineItemNumber
    ) {}

    public record OrderIdentificationDetail(
            String purchaseOrderNumber,
            double quantity,
            String unitOrBasisForMeasurementCode,
            double volume,
            String volumeUnitQualifier,
            double weight,
            String weightUnitCode
    ) {}

    public record ShipmentWeightPackagingAndQuantityData(
            int ladingQuantity,
            int ladingQuantity_1,
            double volume,
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
            int ladingQuantity,
            double volume,
            String volumeUnitQualifier,
            double weight,
            String weightQualifier,
            String weightUnitCode
    ) {}

    public record Meta(
            String file_name,
            String group_functional_identifier,
            String group_seq_number,
            String message_seq_number,
            String set_seq_number
    ) {}
}
