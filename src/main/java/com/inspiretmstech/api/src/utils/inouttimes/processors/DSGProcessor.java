package com.inspiretmstech.api.src.utils.inouttimes.processors;

import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.utils.Executor;
import com.inspiretmstech.api.src.utils.apis.DicksSportingGoodsAPI;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimes;
import com.inspiretmstech.api.src.utils.inouttimes.TimeProcessor;
import com.inspiretmstech.common.microservices.dsg.ApiException;
import com.inspiretmstech.common.microservices.dsg.models.*;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.StateConverter;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.IntegrationTypes;
import com.inspiretmstech.db.tables.records.EquipmentRecord;
import com.inspiretmstech.db.tables.records.LoadTendersRecord;
import com.inspiretmstech.db.tables.records.OrdersRecord;
import com.inspiretmstech.db.tables.records.StopsRecord;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DSGProcessor extends TimeProcessor {

    public DSGProcessor() {
        super(DSGProcessor.class);
    }

    @Override
    protected Executor<InOutTimes> getArrivalProcessor() {
        logger.debug("processing arrival time");
        return (processor) -> this.send(processor, true);
    }

    @Override
    protected Executor<InOutTimes> getDepartureProcessor() {
        logger.debug("processing departure time");
        return (processor) -> this.send(processor, false);
    }

    private void send(InOutTimes processor, boolean isArrival) throws SQLException {

        DicksSportingGoodsAPI dsg = new DicksSportingGoodsAPI();

        OrdersRecord order = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.ORDERS).where(Tables.ORDERS.ID.eq(processor.orderID())).fetchOne()).orElse(null);
        if(Objects.isNull(order)) {
            logger.error("unable to load order");
            throw new ResponseException("Unable to load Order");
        }

        StopsRecord stop = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.STOPS).where(Tables.STOPS.ORDER_ID.eq(order.getId())).and(Tables.STOPS.STOP_NUMBER.eq(processor.stopNumber())).fetchOne()).orElse(null);
        if(Objects.isNull(stop)) {
            logger.error("unable to load stop");
            throw new ResponseException("Unable to load Stop");
        }

        LoadTendersRecord tender = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.LOAD_TENDERS).where(Tables.LOAD_TENDERS.ORDER_ID.eq(order.getId())).fetchOne()).orElse(null);
        if(Objects.isNull(tender)) {
            logger.error("unable to load tender");
            throw new ResponseException("Unable to load Tender");
        }

        if(Objects.isNull(tender.getIntegrationType()) || (tender.getIntegrationType() != IntegrationTypes.DSG)) {
            logger.debug("dicks sporting goods not enabled for this order");
            return;
        }

        Optional<EquipmentRecord> trailer = Objects.isNull(stop.getTrailerId()) ? Optional.empty() : PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.EQUIPMENT).where(Tables.EQUIPMENT.ID.eq(stop.getTrailerId())).fetchOne());


        RtsEdiSendDicksSportingGoodsTransportationCarrierShipmentMessagePostRequestInner update = new RtsEdiSendDicksSportingGoodsTransportationCarrierShipmentMessagePostRequestInner();
        update.setData(new RtsEdiSendDicksSportingGoodsTransportationCarrierShipmentMessagePostRequestInnerData());
        update.getData().setSection1(new SectionRtsDicksSportingGoodsTransportationCarrierShipmentMessage1());

        update.getData().getSection1().setBeginningSegmentForTransportationCarrierShipmentStatusMessage(new SegmentBeginningSegmentForTransportationCarrierShipmentStatusMessageEf9bdc7b817edec973c3ff74f7fae7206fc460f62a56f6bc4917e18391e3c5f3());
        update.getData().getSection1().getBeginningSegmentForTransportationCarrierShipmentStatusMessage().setReferenceIdentification("TENDER-" + tender.getNumber());
        update.getData().getSection1().getBeginningSegmentForTransportationCarrierShipmentStatusMessage().setShipmentIdentificationNumber(tender.getOriginalCustomerReferenceNumber()); // original customer reference number
        update.getData().getSection1().getBeginningSegmentForTransportationCarrierShipmentStatusMessage().setStandardCarrierAlphaCode(dsg.getIntegration().getDsgScac().toUpperCase());

        update.getData().getSection1().setBusinessInstructionsAndReferenceNumber(List.of(
                new SegmentBusinessInstructionsAndReferenceNumberEcd348263a08ab479c637b88c6e6dce4b4a750bbdfe084123937c8a2b4d2b952(),
                new SegmentBusinessInstructionsAndReferenceNumberEcd348263a08ab479c637b88c6e6dce4b4a750bbdfe084123937c8a2b4d2b952()
        ));
        update.getData().getSection1().getBusinessInstructionsAndReferenceNumber().get(0).setReferenceIdentification("" + order.getOrderNumber());
        update.getData().getSection1().getBusinessInstructionsAndReferenceNumber().get(0).setReferenceIdentificationQualifier(SegmentBusinessInstructionsAndReferenceNumberEcd348263a08ab479c637b88c6e6dce4b4a750bbdfe084123937c8a2b4d2b952.ReferenceIdentificationQualifierEnum.BM);
        update.getData().getSection1().getBusinessInstructionsAndReferenceNumber().get(1).setReferenceIdentification(Objects.nonNull(stop.getLoadTenderStopId()) && !stop.getLoadTenderStopId().isBlank() ? stop.getLoadTenderStopId() : stop.getCustomerOrderNumber());
        update.getData().getSection1().getBusinessInstructionsAndReferenceNumber().get(1).setReferenceIdentificationQualifier(SegmentBusinessInstructionsAndReferenceNumberEcd348263a08ab479c637b88c6e6dce4b4a750bbdfe084123937c8a2b4d2b952.ReferenceIdentificationQualifierEnum.QN);

        update.getData().getSection1().setGroupAssignedNumber1130(List.of(new Group0200D1344a0172fe2ab804acb1ee785f3996527ff51b4943dcb3c6827f6256fc6fbf()));
        update.getData().getSection1().getGroupAssignedNumber1130().get(0).setAssignedNumber(new SegmentAssignedNumber());
        update.getData().getSection1().getGroupAssignedNumber1130().get(0).getAssignedNumber().setAssignedNumber(1);
        update.getData().getSection1().getGroupAssignedNumber1130().get(0).setGroupShipmentStatusDetails1140(List.of(new Group02051a03101ba6619ace50f542d941d3f26bf1d9e42f86f2ff4044856d9a28f10bc3()));
        Group02051a03101ba6619ace50f542d941d3f26bf1d9e42f86f2ff4044856d9a28f10bc3 details = update.getData().getSection1().getGroupAssignedNumber1130().get(0).getGroupShipmentStatusDetails1140().get(0);

        SegmentEquipmentShipmentOrRealPropertyLocationF9bd02ebdaa2734f090ab45abb2414ba90b47cbe5153978b709376877b1eeb4e location = new SegmentEquipmentShipmentOrRealPropertyLocationF9bd02ebdaa2734f090ab45abb2414ba90b47cbe5153978b709376877b1eeb4e();
        location.setCityName(stop.getAddress().getCity());
        location.setStateOrProvinceCode(StateConverter.from(stop.getAddress().getState()).getCode());
        details.setEquipmentShipmentOrRealPropertyLocation(location);

        SegmentEquipmentOrContainerOwnerAndType94aa1adbd6bbecd23749372c0642c25fb06a56c6b306b2015f3756e5211ed95e equipment = new SegmentEquipmentOrContainerOwnerAndType94aa1adbd6bbecd23749372c0642c25fb06a56c6b306b2015f3756e5211ed95e();
        details.setEquipmentOrContainerOwnerAndType(equipment);
        String equipmentNumber = trailer.isPresent() ? trailer.get().getUnitNumber() : order.getCarrierEdiTrailerNumberOrId();
        equipment.setEquipmentNumber(equipmentNumber.isBlank() ? "UNKNOWN" : equipmentNumber.trim());
        equipment.setStandardCarrierAlphaCode(dsg.getIntegration().getDsgScac().toUpperCase());

        ZonedDateTime dt = (isArrival ? stop.getDriverArrivedAt() : stop.getDriverDepartedAt()).atZoneSameInstant(ZoneId.of(stop.getAddress().getIanaTimezoneId()));

        details.setShipmentStatusDetails(new SegmentShipmentStatusDetails9e93a24c60d21fb3059d71dfd13f9724ed0bcee894354fd851588f1c503c4747());
        details.getShipmentStatusDetails().setDate(dt.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        details.getShipmentStatusDetails().setTime(dt.format(DateTimeFormatter.ofPattern("HHmm")));
        details.getShipmentStatusDetails().setShipmentStatusOrAppointmentReasonCode(SegmentShipmentStatusDetails9e93a24c60d21fb3059d71dfd13f9724ed0bcee894354fd851588f1c503c4747.ShipmentStatusOrAppointmentReasonCodeEnum.NS);
        details.getShipmentStatusDetails().setShipmentStatusCode(isArrival ? SegmentShipmentStatusDetails9e93a24c60d21fb3059d71dfd13f9724ed0bcee894354fd851588f1c503c4747.ShipmentStatusCodeEnum.X3 : SegmentShipmentStatusDetails9e93a24c60d21fb3059d71dfd13f9724ed0bcee894354fd851588f1c503c4747.ShipmentStatusCodeEnum.X1);

        try {
            dsg.outbound().rtsEdiSendDicksSportingGoodsTransportationCarrierShipmentMessagePost(
                    List.of(update),
                    dsg.getIntegration().getDsgScac().toUpperCase()
            );
        } catch(ApiException e) {
            logger.error("ApiException: {}", e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) logger.trace(el.toString());
            throw new ResponseException("Unable to Send EDI!", "An error occurred while sending EDI to Dicks Sporting Goods");
        }
    }
}
