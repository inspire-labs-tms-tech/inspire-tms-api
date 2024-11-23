package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.requires.Requires;
import com.inspiretmstech.api.src.auth.requires.Scopes;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimes;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimesProcessor;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimesRequest;
import com.inspiretmstech.api.src.utils.inouttimes.processors.AfterShipProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "AfterShip", description = "Proxy for AfterShip Load Updates")
@RequestMapping("/v1/aftership")
public class AfterShipController extends Controller {

    public AfterShipController() {
        super(AfterShipController.class);
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.ORDERS)
    @Operation(summary = "Send Arrived Tracking Update")
    @PostMapping("/tracking/arrived")
    public StatusResponse sendAfterShipArrivedTrackingUpdate(@RequestBody InOutTimesRequest update) {
        InOutTimesProcessor processor = new InOutTimesProcessor(List.of(new AfterShipProcessor()));
        try {
            processor.arrived(update);
            return StatusResponse.ACCEPTED();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ResponseException("Unable to Send Tracking Update", "An error occurred while sending arrived tracking update");
        }
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.ORDERS)
    @Operation(summary = "Send Departed Tracking Update")
    @PostMapping("/tracking/departed")
    public StatusResponse sendAfterShipDepartedTrackingUpdate(@RequestBody InOutTimesRequest update) {
        InOutTimesProcessor processor = new InOutTimesProcessor(List.of(new AfterShipProcessor()));
        try {
            processor.departed(update);
            return StatusResponse.ACCEPTED();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ResponseException("Unable to Send Tracking Update", "An error occurred while sending departed tracking update");
        }
    }

}
