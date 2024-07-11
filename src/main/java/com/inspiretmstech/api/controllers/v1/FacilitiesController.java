package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.auth.Requires;
import com.inspiretmstech.api.auth.Scopes;
import com.inspiretmstech.api.models.ResponseException;
import com.inspiretmstech.api.models.requests.facilities.FacilityRequest;
import com.inspiretmstech.api.models.responses.IDResponse;
import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.FacilitiesRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Optional;

@RestController
@Tag(name = "Facilities", description = "User endpoints for managing facilities")
@RequestMapping("/v1/facilities")
public class FacilitiesController {

    @Secured(Authority.Authorities.USER)
    @Requires({Scopes.FACILITIES})
    @Operation(summary = "Create a facility")
    @PostMapping
    public IDResponse createFacility(
            @RequestBody FacilityRequest request
    ) throws SQLException {
        Optional<FacilitiesRecord> facility = DatabaseConnection.getInstance().with(supabase ->
                supabase
                        .insertInto(Tables.FACILITIES,
                                Tables.FACILITIES.DISPLAY,
                                Tables.FACILITIES.IS_ACTIVE,
                                Tables.FACILITIES.ADDRESS
                        )
                        .values(
                                request.displayName(),
                                request.isActive(),
                                request.address().toAddress()
                        ).returning().fetchOne());
        if (facility.isEmpty())
            throw new ResponseException("Facilities could not be created");

        return IDResponse.from(facility.get().getId());
    }

}
