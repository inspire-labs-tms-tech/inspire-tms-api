package com.inspiretmstech.api.controllers.v1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.inspiretmstech.api.models.requests.tenders.gp.GeorgiaPacificLoadTender;
import org.jooq.meta.derby.sys.Sys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootTest
@AutoConfigureMockMvc
public class TestGeorgiaPacificLoadTenderController {

    @Autowired
    private MockMvc server;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(server);
    }

    @Test
    void testPing() throws Exception {
        this.server
                .perform(MockMvcRequestBuilders.get("/v1/ping"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("\"pong\""));
    }

    @Test
    void bulkTest() throws Exception {
        Resource[] testFiles = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:/gp/*.json");
        List<JsonArray> tests = new ArrayList<>();
        for (Resource test : testFiles) {
            File file = new File(test.getFile().getAbsolutePath());
            JsonArray data = JsonParser.parseString(readFile(file)).getAsJsonArray();
            tests.add(data);
        }

        // needs to load 149 tests
        Assertions.assertEquals(149, tests.size());

        // ensure all test-cases parse
        Gson gson = new Gson();
        for (JsonArray test : tests) gson.fromJson(test, GeorgiaPacificLoadTender.Shipment[].class);

    }

    private String readFile(File file) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        StringBuilder data = new StringBuilder();
        while (reader.hasNextLine()) data.append(reader.nextLine());
        reader.close();
        return data.toString();
    }

//    @Test
//    void testGeorgiaPacificLoadTender() throws Exception {
//        this.server
//                .perform(MockMvcRequestBuilders
//                        .post("/v1/georgia-pacific")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("[{\"data\":{\"section1\":{\"beginningSegmentForShipmentInformationTransaction\":{\"customsDocumentationHandlingCode\":null,\"shipmentIdentificationNumber\":\"6882911\",\"shipmentMethodOfPayment\":\"PP\",\"standardCarrierAlphaCode\":\"RTQR\",\"weightUnitCode\":\"L\"},\"billOfLadingHandlingRequirements\":[{\"specialHandlingCode\":null,\"specialHandlingDescription\":\"53FT102INTRAILER\",\"specialServicesCode\":null}],\"businessInstructionsAndReferenceNumber\":[{\"description\":\"ed09e5b5-6ea7-4339-bb92-68240cdd8099\",\"referenceIdentification\":\"00304006882911000\",\"referenceIdentificationQualifier\":\"BM\"},{\"description\":null,\"referenceIdentification\":\"KBXLEGACY\",\"referenceIdentificationQualifier\":\"4F\"}],\"dateOrTime\":{\"date\":\"20240729\",\"dateQualifier\":\"10\",\"time\":\"183800\",\"timeCode\":\"LT\",\"timeQualifier\":\"Y\"},\"groupEquipmentDetails_1200\":[{\"equipmentDetails\":{\"equipmentDescriptionCode\":\"TF\",\"equipmentLength\":5300,\"equipmentNumber\":\"22-53036\",\"height\":102},\"sealNumbers\":[]}],\"groupName_1140\":[{\"addressInformation\":[{\"addressInformation\":\"DONOTSUBMITINVOICE\",\"addressInformation_1\":null}],\"contact\":[],\"geographicLocation\":null,\"name\":{\"entityIdentifierCode\":\"BT\",\"name\":\"FREIGHTAUTOPAYBYGEORGIAPACIFICCPG\"}},{\"addressInformation\":[],\"contact\":[],\"geographicLocation\":null,\"name\":{\"entityIdentifierCode\":\"SH\",\"name\":\"GEORGIA-PACIFICCPGFREIGHT\"}}],\"hazardousCertification\":[],\"interlineInformation\":{\"routingSequenceCode\":\"B\",\"standardCarrierAlphaCode\":\"RTQR\",\"transportationMethodOrTypeCode\":\"J\"},\"noteOrSpecialInstruction\":[{\"description\":\"DONTCONTACTDOLLARGENERAL,CONTACTGPONLY.\"},{\"description\":\"GPPAYSSIGNIFICANTPENALTYFEESFORANYLATEORRESCHEDULEDLOADS\"},{\"description\":\"LIVE07/30AT0800CONF855178290\"},{\"description\":\"LUMPERFEESAREAUTHORIZED.\"}],\"palletInformation\":null,\"setPurpose\":{\"applicationType\":\"LT\",\"transactionSetPurposeCode\":\"04\"}},\"section2\":{\"groupStopOffDetails_210\":[{\"billOfLadingHandlingRequirements\":[{\"specialHandlingCode\":null,\"specialHandlingDescription\":\"NOAPPTNECESSARY\",\"specialServicesCode\":null}],\"businessInstructionsAndReferenceNumber\":[{\"referenceIdentification\":\"1ZGDM0\",\"referenceIdentificationQualifier\":\"PO\"},{\"referenceIdentification\":\"DIXIE\",\"referenceIdentificationQualifier\":\"PRT\"},{\"referenceIdentification\":\"GEORGIA-PACIFIC\",\"referenceIdentificationQualifier\":\"PRT\"},{\"referenceIdentification\":\"8003006425\",\"referenceIdentificationQualifier\":\"VN\"}],\"dateOrTime\":[{\"date\":\"20240729\",\"dateQualifier\":\"CL\",\"time\":\"155600\",\"timeCode\":\"LT\",\"timeQualifier\":\"W\"}],\"groupName_270\":{\"addressInformation\":[{\"addressInformation\":\"21837WESTMISSISSIPPIAVENUE\",\"addressInformation_1\":null}],\"contact\":[{\"communicationNumber\":\"815-423-9990\",\"communicationNumberQualifier\":\"TE\",\"contactFunctionCode\":\"IC\",\"contactInquiryReference\":null,\"name\":\"SHIPPING\"}],\"geographicLocation\":{\"cityName\":\"ELWOOD\",\"countryCode\":\"USA\",\"postalCode\":\"60421\",\"stateOrProvinceCode\":\"IL\"},\"name\":{\"entityIdentifierCode\":\"SF\",\"identificationCode\":\"A072\",\"identificationCodeQualifier\":\"93\",\"name\":\"CHICAGODC\"}},\"groupOrderIdentificationDetail_2150\":[{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":1}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":84,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":84,\"volumeUnitQualifier\":\"E\",\"weight\":773,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":2}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":108,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":292,\"volumeUnitQualifier\":\"E\",\"weight\":1484,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":3}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":160,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":104,\"volumeUnitQualifier\":\"E\",\"weight\":1056,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":4}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":192,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":355,\"volumeUnitQualifier\":\"E\",\"weight\":1817,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":5}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":192,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":655,\"volumeUnitQualifier\":\"E\",\"weight\":2395,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":6}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":252,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":680,\"volumeUnitQualifier\":\"E\",\"weight\":3459,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":7}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":288,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":510,\"volumeUnitQualifier\":\"E\",\"weight\":2630,\"weightUnitCode\":\"L\"}}],\"noteOrSpecialInstruction\":[{\"description\":\"NOAPPTNECESSARY\"}],\"palletInformation\":null,\"shipmentWeightPackagingAndQuantityData\":{\"ladingQuantity\":1276,\"ladingQuantity_1\":0,\"volume\":2680,\"volumeUnitQualifier\":\"E\",\"weight\":13614,\"weightQualifier\":\"G\",\"weightUnitCode\":\"L\"},\"stopOffDetails\":{\"stopReasonCode\":\"CL\",\"stopSequenceNumber\":1}},{\"billOfLadingHandlingRequirements\":[{\"specialHandlingCode\":null,\"specialHandlingDescription\":null,\"specialServicesCode\":\"CC\"},{\"specialHandlingCode\":\"OTD\",\"specialHandlingDescription\":null,\"specialServicesCode\":null},{\"specialHandlingCode\":null,\"specialHandlingDescription\":\"NONEEDTOCONFIRMAPPT\",\"specialServicesCode\":null}],\"businessInstructionsAndReferenceNumber\":[{\"referenceIdentification\":\"1ZGDM0\",\"referenceIdentificationQualifier\":\"PO\"},{\"referenceIdentification\":\"DIXIE\",\"referenceIdentificationQualifier\":\"PRT\"},{\"referenceIdentification\":\"GEORGIA-PACIFIC\",\"referenceIdentificationQualifier\":\"PRT\"},{\"referenceIdentification\":\"8003006425\",\"referenceIdentificationQualifier\":\"VN\"}],\"dateOrTime\":[{\"date\":\"20240730\",\"dateQualifier\":\"70\",\"time\":\"080000\",\"timeCode\":\"LT\",\"timeQualifier\":\"X\"}],\"groupName_270\":{\"addressInformation\":[{\"addressInformation\":\"101INNOVATIONDR\",\"addressInformation_1\":null}],\"contact\":[],\"geographicLocation\":{\"cityName\":\"JANESVILLE\",\"countryCode\":\"USA\",\"postalCode\":\"53546\",\"stateOrProvinceCode\":\"WI\"},\"name\":{\"entityIdentifierCode\":\"ST\",\"identificationCode\":\"2258492\",\"identificationCodeQualifier\":\"93\",\"name\":\"DOLGEN-96130JANESVILLE\"}},\"groupOrderIdentificationDetail_2150\":[{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":1}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":84,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":84,\"volumeUnitQualifier\":\"E\",\"weight\":773,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":2}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":108,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":292,\"volumeUnitQualifier\":\"E\",\"weight\":1484,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":3}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":160,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":104,\"volumeUnitQualifier\":\"E\",\"weight\":1056,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":4}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":192,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":355,\"volumeUnitQualifier\":\"E\",\"weight\":1817,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":5}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":192,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":655,\"volumeUnitQualifier\":\"E\",\"weight\":2395,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":6}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":252,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":680,\"volumeUnitQualifier\":\"E\",\"weight\":3459,\"weightUnitCode\":\"L\"}},{\"groupDescriptionMarksAndNumbers_2190\":[{\"descriptionMarksAndNumbers\":{\"ladingDescription\":\"PAPERPRODUCTS\",\"ladingLineItemNumber\":7}}],\"orderIdentificationDetail\":{\"purchaseOrderNumber\":\"1ZGDM0\",\"quantity\":288,\"unitOrBasisForMeasurementCode\":\"PC\",\"volume\":510,\"volumeUnitQualifier\":\"E\",\"weight\":2630,\"weightUnitCode\":\"L\"}}],\"noteOrSpecialInstruction\":[{\"description\":\"NONEEDTOCONFIRMAPPT\"},{\"description\":\"DONTCONTACTDOLLARGENERAL,CONTACTGPONLY.\"},{\"description\":\"LUMPERFEESAREAUTHORIZED.\"},{\"description\":\"GPPAYSSIGNIFICANTPENALTYFEESFORANYLATEORRESCHEDULEDLOADS\"},{\"description\":\"LIVE07/30AT0800CONF855178290\"}],\"palletInformation\":null,\"shipmentWeightPackagingAndQuantityData\":{\"ladingQuantity\":1276,\"ladingQuantity_1\":0,\"volume\":2680,\"volumeUnitQualifier\":\"E\",\"weight\":13614,\"weightQualifier\":\"G\",\"weightUnitCode\":\"L\"},\"stopOffDetails\":{\"stopReasonCode\":\"CU\",\"stopSequenceNumber\":2}}]},\"section3\":{\"totalWeightAndCharges\":{\"ladingQuantity\":1276,\"volume\":2680,\"volumeUnitQualifier\":\"E\",\"weight\":13614,\"weightQualifier\":\"G\",\"weightUnitCode\":\"L\"}}}}]")
//                )
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isOk());
//    }

}
