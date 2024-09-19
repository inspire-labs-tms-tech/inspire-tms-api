package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.timezone.TimeZoneLookupRequest;
import com.inspiretmstech.api.src.models.responses.IDResponse;
import com.inspiretmstech.common.utils.TimeZones;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Objects;

@RestController
@Tag(name = "TimeZone", description = "Timezone Tools")
@RequestMapping("/v1/timezone")
public class TimeZoneController extends Controller {

    public TimeZoneController() {super(TimeZoneController.class);}

    @Secured(Authority.Authorities.USER)
    @Operation(summary = "Lookup a timezone via coordinates")
    @PostMapping("/lookup")
    public IDResponse lookupTimezone(@RequestBody TimeZoneLookupRequest request) {

        if (Objects.isNull(request)) throw new ResponseException("Request cannot be null");
        if (Objects.isNull(request.lat())) throw new ResponseException("Lat cannot be null");
        if (Objects.isNull(request.lng())) throw new ResponseException("Lng cannot be null");

        ZoneId zoneId = TimeZones.lookup(request.toLatLng(), request.toString());
        if (Objects.isNull(zoneId)) throw new ResponseException("Unable to Lookup Zone!");
        return new IDResponse(zoneId.getId());
    }

}
