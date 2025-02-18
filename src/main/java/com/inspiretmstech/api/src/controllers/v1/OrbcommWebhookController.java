package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.models.requests.orbcomm.OrbcommDataPushRequest;
import com.inspiretmstech.api.src.models.responses.StatusResponse;
import com.inspiretmstech.api.src.utils.OrbcommAssetLocationService;
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

    private final OrbcommAssetLocationService assetLocationService;

    public OrbcommWebhookController(OrbcommAssetLocationService assetLocationService) {
        this.assetLocationService = assetLocationService;
    }

    @Secured(Authority.Authorities.ORBCOMM)
    @Operation(summary = "POST tracking updates")
    @PostMapping
    public StatusResponse sendTrackingUpdates(@RequestBody OrbcommDataPushRequest request) {
        this.logger.trace("Received Request: {}", request.data().size());
        assetLocationService.enqueue(request);
        return new StatusResponse("accepted");
    }
}
