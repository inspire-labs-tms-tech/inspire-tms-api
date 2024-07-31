package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.auth.Requires;
import com.inspiretmstech.api.auth.Scopes;
import com.inspiretmstech.api.models.ResponseException;
import com.inspiretmstech.api.models.requests.facilities.FacilityRequest;
import com.inspiretmstech.api.models.responses.IDResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.FacilitiesRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

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
        Optional<FacilitiesRecord> facility = PostgresConnection.getInstance().with(supabase -> supabase
                .insertInto(Tables.FACILITIES,
                        Tables.FACILITIES.DISPLAY,
                        Tables.FACILITIES.IS_ACTIVE,
                        Tables.FACILITIES.ADDRESS
                )
                .values(
                        request.displayName(),
                        request.isActive(),
                        request.address().toAddress()
                )
                .returning()
                .fetchOne());
        if (facility.isEmpty())
            throw new ResponseException("Facility could not be created", "An unexpected error occurred while creating the facility", "Is the displayName unique?");

        return IDResponse.from(facility.get().getId());
    }

    @Secured(Authority.Authorities.USER)
    @Requires({Scopes.FACILITIES})
    @Operation(summary = "Update a facility")
    @PutMapping("{id}")
    public void updateFacility(
            @RequestBody FacilityRequest request,
            @PathVariable String id
    ) throws SQLException {

        Optional<FacilitiesRecord> updated = PostgresConnection.getInstance().with(supabase -> supabase
                .update(Tables.FACILITIES)
                .set(Tables.FACILITIES.DISPLAY, request.displayName())
                .set(Tables.FACILITIES.IS_ACTIVE, request.isActive())
                .set(Tables.FACILITIES.ADDRESS, request.address().toAddress())
                .where(Tables.FACILITIES.ID.eq(UUID.fromString(id)))
                .returning()
                .fetchOne());
        if (updated.isEmpty())
            throw new ResponseException("Facility could not be updated", "An unknown exception occurred while updating the facility", "Does the facility exist?");
    }


}
