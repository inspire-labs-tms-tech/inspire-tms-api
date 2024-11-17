package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.requires.Requires;
import com.inspiretmstech.api.src.auth.requires.Scopes;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimes;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimesProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Orders", description = "Act on an Order")
@RequestMapping("/v1/orders")
public class OrdersController extends Controller {

    public OrdersController() {
        super(OrdersController.class);
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.ORDERS)
    @Operation(summary = "Submit arrival time on an order")
    @PostMapping("/{orderID}/{stopNumber}/arrived")
    public StatusResponse submitArrivalTime(
            @PathVariable("orderID") String order,
            @PathVariable("stopNumber") Long stop,
            @RequestBody String timestamp
    ) {
        InOutTimesProcessor processor = new InOutTimesProcessor();
        processor.arrived(new InOutTimes(UUID.fromString(order), stop, timestamp));
        return StatusResponse.ACCEPTED();
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.ORDERS)
    @Operation(summary = "Submit arrival time on an order")
    @PostMapping("/{orderID}/{stopNumber}/departed")
    public StatusResponse submitDepartureTime(
            @PathVariable("orderID") String order,
            @PathVariable("stopNumber") Long stop,
            @RequestBody String timestamp
    ) {
        InOutTimesProcessor processor = new InOutTimesProcessor();
        processor.departed(new InOutTimes(UUID.fromString(order), stop, timestamp));
        return StatusResponse.ACCEPTED();
    }
}
