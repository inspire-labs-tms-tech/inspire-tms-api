package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.requires.Requires;
import com.inspiretmstech.api.src.auth.requires.Scopes;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.address.AddressObjectModel;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.models.requests.facilities.FacilityRequest;
import com.inspiretmstech.api.src.models.responses.IDResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.FacilitiesRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "Facilities", description = "User endpoints for managing facilities")
@RequestMapping("/v1/facilities")
public class FacilitiesController extends Controller {

    public FacilitiesController() {
        super(FacilitiesController.class);
    }

    @Secured(Authority.Authorities.USER)
    @Requires({Scopes.FACILITIES})
    @Operation(summary = "Create a facility")
    @PostMapping
    public IDResponse createFacility(
            @RequestBody FacilityRequest request
    ) throws SQLException {

        if (Objects.isNull(request.address()) && Objects.isNull(request.fullyQualifiedAddress()))
            throw new ResponseException("Invalid Request", "'request' or 'fullyQualifiedAddress' must be specified");
        if (!Objects.isNull(request.address()) && !Objects.isNull(request.fullyQualifiedAddress()))
            throw new ResponseException("Invalid Request", "only one of 'request' or 'fullyQualifiedAddress' must be specified");

        AddressObjectModel address = Objects.isNull(request.address()) ? request.fullyQualifiedAddress() : request.address();

        Optional<FacilitiesRecord> facility = Optional.empty();
        try {
            facility = PostgresConnection.getInstance().unsafely(supabase -> supabase
                    .insertInto(Tables.FACILITIES,
                            Tables.FACILITIES.DISPLAY,
                            Tables.FACILITIES.IS_ACTIVE,
                            Tables.FACILITIES.ADDRESS,
                            Tables.FACILITIES.MIGRATED_FACILITY_ID
                    )
                    .values(
                            request.displayName(),
                            request.isActive(),
                            address.build(),
                            request.externalID()
                    )
                    .returning()
                    .fetchOne());
        } catch (Exception e) {
            if (e.getMessage().contains("Key (migrated_facility_id)=(" + request.externalID() + ") already exists"))
                throw new ResponseException("External ID Already Exists", "A facility with external id '" + request.externalID() + "' already exists");
        }
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

        if (Objects.isNull(request.address()) && Objects.isNull(request.fullyQualifiedAddress()))
            throw new ResponseException("Invalid Request", "'request' or 'fullyQualifiedAddress' must be specified");
        if (!Objects.isNull(request.address()) && !Objects.isNull(request.fullyQualifiedAddress()))
            throw new ResponseException("Invalid Request", "only one of 'request' or 'fullyQualifiedAddress' must be specified");

        AddressObjectModel address = Objects.isNull(request.address()) ? request.fullyQualifiedAddress() : request.address();

        Optional<FacilitiesRecord> updated = PostgresConnection.getInstance().with(supabase -> supabase
                .update(Tables.FACILITIES)
                .set(Tables.FACILITIES.DISPLAY, request.displayName())
                .set(Tables.FACILITIES.IS_ACTIVE, request.isActive())
                .set(Tables.FACILITIES.ADDRESS, address.build())
                .set(Tables.FACILITIES.MIGRATED_FACILITY_ID, request.externalID())
                .where(Tables.FACILITIES.ID.eq(UUID.fromString(id)))
                .returning()
                .fetchOne());
        if (updated.isEmpty())
            throw new ResponseException("Facility could not be updated", "An unknown exception occurred while updating the facility", "Does the facility exist?");
    }


}
