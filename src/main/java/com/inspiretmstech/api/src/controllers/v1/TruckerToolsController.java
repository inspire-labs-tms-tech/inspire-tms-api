package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.truckertools.TruckerToolsCommentsRequest;
import com.inspiretmstech.api.src.models.requests.truckertools.TruckerToolsDocumentsRequest;
import com.inspiretmstech.api.src.models.requests.truckertools.TruckerToolsStatusRequest;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@RestController
@Tag(name = "Trucker Tools", description = "Webhooks for Trucker Tools")
@RequestMapping("/v1/trucker-tools")
public class TruckerToolsController extends Controller {

    public TruckerToolsController() {
        super(TruckerToolsController.class);
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Load Track Status Webhook")
    @PostMapping("/status")
    public StatusResponse updateStatus(@RequestBody TruckerToolsStatusRequest request) throws SQLException {

        logger.trace("inbound status request: {}", request);

        if (Objects.isNull(request)) throw new ResponseException("Request Body Cannot be Null");

        if (Objects.isNull(request.status())) throw new ResponseException("Status Cannot be Null");

        TruckerToolsLoadStatusesRecord status = new TruckerToolsLoadStatusesRecord();
        status.setLoadId((long) request.loadTrackId());
        status.setStopNumber((long) request.status().stopOrderNumber());
        status.setName(request.status().name());
        status.setLat(Objects.isNull(request.status().location()) ? null : new BigDecimal(request.status().location().lat()));
        status.setLong(Objects.isNull(request.status().location()) ? null : new BigDecimal(request.status().location().lon()));

        Optional<TruckerToolsLoadStatusesRecord> created = PostgresConnection.getInstance().with(supabase ->
                supabase
                        .insertInto(Tables.TRUCKER_TOOLS_LOAD_STATUSES)
                        .set(status)
                        .returning()
                        .fetchOne());

        // process stop-level information
        if (request.status().stopOrderNumber() > 0) try {

            // get the load
            Optional<TruckerToolsLoadsRecord> load = PostgresConnection.getInstance().with(supabase -> supabase.selectFrom(Tables.TRUCKER_TOOLS_LOADS).where(Tables.TRUCKER_TOOLS_LOADS.ID.eq((long) request.loadTrackId())).fetchOne());
            if (load.isEmpty()) throw new NullPointerException("Unable to Load LoadTrack");

            // update arrived/departed times
            if (request.status().name().startsWith("Checked Completed")) {
                Optional<StopsRecord> stop = PostgresConnection.getInstance().with(supabase ->
                        supabase.update(Tables.STOPS)
                                .set(Tables.STOPS.DRIVER_ARRIVED_AT, OffsetDateTime.now())
                                .set(Tables.STOPS.DRIVER_DEPARTED_AT, OffsetDateTime.now())
                                .where(Tables.STOPS.ORDER_ID.eq(load.get().getOrderId()).and(Tables.STOPS.STOP_NUMBER.eq((long) request.loadTrackId()))).returning().fetchOne()
                );
                if(stop.isEmpty() || Objects.isNull(stop.get().getDriverArrivedAt())) throw new NullPointerException("Unable to Update Arrival/Departure Times");
            }

        } catch (Exception e) {
            this.logger.error("Unable to Update Stop {}: {}", request.status().stopOrderNumber(), e.getMessage());
        }


        if (created.isEmpty()) throw new ResponseException("Unable to Create Load Track Status!");
        return StatusResponse.ACCEPTED();
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Load Track Documents Webhook")
    @PostMapping("/documents")
    public StatusResponse updateDocuments(@RequestBody TruckerToolsDocumentsRequest request) throws SQLException {

        logger.trace("inbound document request: {}", request);

        if (Objects.isNull(request)) throw new ResponseException("Request Body Cannot be Null");

        if (Objects.isNull(request.document())) throw new ResponseException("Document Cannot be Null");

        TruckerToolsLoadDocumentsRecord document = new TruckerToolsLoadDocumentsRecord();
        document.setLoadId((long) request.loadTrackId());
        document.setType(request.document().type());
        document.setUrl(request.document().url());
        document.setLat(Objects.isNull(request.document().location()) ? null : new BigDecimal(request.document().location().lat()));
        document.setLong(Objects.isNull(request.document().location()) ? null : new BigDecimal(request.document().location().lon()));
        document.setCity(Objects.isNull(request.document().location()) ? null : request.document().location().city());
        document.setState(Objects.isNull(request.document().location()) ? null : request.document().location().state());

        Optional<TruckerToolsLoadDocumentsRecord> created = PostgresConnection.getInstance().with(supabase ->
                supabase
                        .insertInto(Tables.TRUCKER_TOOLS_LOAD_DOCUMENTS)
                        .set(document)
                        .returning()
                        .fetchOne());

        if (created.isEmpty()) throw new ResponseException("Unable to Create Load Track Document!");
        return StatusResponse.ACCEPTED();
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Load Track Comments Webhook")
    @PostMapping("/comments")
    public StatusResponse updateComments(@RequestBody TruckerToolsCommentsRequest request) throws SQLException {

        logger.trace("inbound comment request: {}", request);

        if (Objects.isNull(request)) throw new ResponseException("Request Body Cannot be Null");

        if (Objects.isNull(request.comments())) throw new ResponseException("Comment Cannot be Null");

        TruckerToolsLoadCommentsRecord comment = new TruckerToolsLoadCommentsRecord();
        comment.setLoadId((long) request.loadTrackId());
        comment.setComment(request.comments().comment());
        comment.setCommentBy(request.comments().commentBy());
        comment.setLat(Objects.isNull(request.comments().location()) ? null : new BigDecimal(request.comments().location().lat()));
        comment.setLong(Objects.isNull(request.comments().location()) ? null : new BigDecimal(request.comments().location().lon()));
        comment.setCity(Objects.isNull(request.comments().location()) ? null : request.comments().location().city());
        comment.setState(Objects.isNull(request.comments().location()) ? null : request.comments().location().state());

        Optional<TruckerToolsLoadCommentsRecord> created = PostgresConnection.getInstance().with(supabase ->
                supabase
                        .insertInto(Tables.TRUCKER_TOOLS_LOAD_COMMENTS)
                        .set(comment)
                        .returning()
                        .fetchOne());

        if (created.isEmpty()) {
            logger.error("unable to create load track comment");
            throw new ResponseException("Unable to Create Load Track Comment!");
        }
        return StatusResponse.ACCEPTED();
    }
}
