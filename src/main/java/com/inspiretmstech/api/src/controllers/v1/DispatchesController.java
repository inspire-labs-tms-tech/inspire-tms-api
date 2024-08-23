package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.LoadTenderStatus;
import com.inspiretmstech.db.tables.records.CarrierDispatchesRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Dispatches", description = "Carrier endpoints for responding to loads tendered from an Inspire TMS instance")
@RequestMapping("/v1/dispatches")
public class DispatchesController extends Controller {

    public DispatchesController() {
        super(DispatchesController.class);
    }

    private void handle(String id, LoadTenderStatus status) throws Exception {

        PostgresConnection conn = PostgresConnection.getInstance();

        // locate the existing dispatch
        Optional<CarrierDispatchesRecord> dispatch = conn.with(supabase ->
                supabase.selectFrom(Tables.CARRIER_DISPATCHES)
                        .where(Tables.CARRIER_DISPATCHES.ID.eq(UUID.fromString(id)))
                        .fetchOne());

        if (dispatch.isEmpty())
            throw new ResponseException("Unable to Locate Dispatch!", "A dispatch with id '" + id + "' was not found.");

        Optional<CarrierDispatchesRecord> newDispatch = conn.with(supabase ->
                supabase.update(Tables.CARRIER_DISPATCHES)
                        .set(Tables.CARRIER_DISPATCHES.STATUS, status)
                        .where(Tables.CARRIER_DISPATCHES.ID.eq(dispatch.get().getId()))
                        .returning()
                        .fetchOne());

        if (newDispatch.isEmpty())
            throw new ResponseException("Unable to Update Dispatch!", "A dispatch with id '" + id + "' was not able to be updated.");
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Accept a Dispatched Load")
    @PostMapping("/{id}/accept")
    public StatusResponse acceptDispatch(@PathVariable String id) {
        try {
            this.handle(id, LoadTenderStatus.ACCEPTED);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ResponseException("Unable to Save Status!", "An internal exception occurred while updating the load status. Please try again later.", "If the problem persists, please contact support@inspiretmstech.com");
        }
        return StatusResponse.ACCEPTED();
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Decline a Dispatched Load")
    @PostMapping("/{id}/decline")
    public StatusResponse declineDispatch(@PathVariable String id) {
        try {
            this.handle(id, LoadTenderStatus.DECLINED);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ResponseException("Unable to Save Status!", "An internal exception occurred while updating the load status. Please try again later.", "If the problem persists, please contact support@inspiretmstech.com");
        }
        return StatusResponse.DECLINED();
    }
}
