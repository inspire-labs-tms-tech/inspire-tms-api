package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.requires.Requires;
import com.inspiretmstech.api.src.auth.requires.Scopes;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.orders.SubmitOrderTimeRequest;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.api.src.utils.inouttimes.InOutTimesProcessor;
import com.inspiretmstech.api.src.models.requests.InOutTimesRequest;
import com.inspiretmstech.api.src.utils.inouttimes.processors.SaveToDatabaseProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Orders", description = "Act on an Order")
@RequestMapping("/v1/orders")
public class OrdersController extends Controller {

    private final InOutTimesProcessor processor;

    public OrdersController() {
        super(OrdersController.class);
        this.processor = new InOutTimesProcessor();
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.ORDERS)
    @Operation(summary = "Submit arrival time on an order")
    @PostMapping("/{orderID}/{stopNumber}/arrived")
    public StatusResponse submitArrivalTime(
            @PathVariable("orderID") String order,
            @PathVariable("stopNumber") Long stop,
            @RequestBody SubmitOrderTimeRequest request
    ) {
        if(!request.saveToDatabase()) this.processor.remove(SaveToDatabaseProcessor.class);
        this.processor.arrived(new InOutTimesRequest(UUID.fromString(order), stop, request.timestamp()));
        return StatusResponse.ACCEPTED();
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.ORDERS)
    @Operation(summary = "Submit arrival time on an order")
    @PostMapping("/{orderID}/{stopNumber}/departed")
    public StatusResponse submitDepartureTime(
            @PathVariable("orderID") String order,
            @PathVariable("stopNumber") Long stop,
            @RequestBody SubmitOrderTimeRequest request
    ) {
        if(!request.saveToDatabase()) this.processor.remove(SaveToDatabaseProcessor.class);
        this.processor.departed(new InOutTimesRequest(UUID.fromString(order), stop, request.timestamp()));
        return StatusResponse.ACCEPTED();
    }
}
