package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.auth.Requires;
import com.inspiretmstech.api.models.requests.orbcomm.OrbcommDataPushRequest;
import com.inspiretmstech.api.models.responses.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Orbcomm Integration", description = "Orbcomm endpoints for managing integration-specific data")
@RequestMapping("/v1/orbcomm")
public class OrbcommWebhookController {

    private final Logger logger = LoggerFactory.getLogger(OrbcommWebhookController.class);

    @Secured(Authority.Authorities.ORBCOMM)
    @Operation(summary = "POST tracking updates")
    @PostMapping
    public StatusResponse sendTrackingUpdates(@RequestBody OrbcommDataPushRequest request) {
        this.logger.info("Received Request: {}", request.data().size());
        return new StatusResponse("accepted");
    }
}
