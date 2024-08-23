package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.constants.Config;
import com.inspiretmstech.api.src.constants.EnvironmentVariables;
import com.inspiretmstech.api.src.models.responses.VersionResponse;
import com.inspiretmstech.api.src.models.responses.about.AboutCompanyResponse;
import com.inspiretmstech.api.src.models.responses.about.AboutResponse;
import com.inspiretmstech.api.src.models.responses.about.AboutSupabaseKeysResponse;
import com.inspiretmstech.api.src.models.responses.about.AboutSupabaseResponse;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Environment;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.enums.CompanyTypes;
import com.inspiretmstech.db.tables.records.CompaniesRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.client.utils.URIBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
@Tag(name = "About", description = "Retrieve basic diagnostic and public information about an Inspire TMS Instance")
@RequestMapping("/v1")
public class RootController {

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Retrieve the Inspire TMS API version")
    @GetMapping("/version")
    public VersionResponse getVersion() {
        return new VersionResponse(Config.VERSION);
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "An unauthenticated (anonymous) ping endpoint")
    @GetMapping("/ping")
    public String ping() {
        return "\"pong\"";
    }

    @Secured(Authority.Authorities.USER)
    @Operation(summary = "A user-authenticated ping endpoint (to verify user-level authentication)")
    @GetMapping("/ping/user")
    public String pingUser() {
        return "\"pong\"";
    }

    @Secured(Authority.Authorities.CUSTOMER)
    @Operation(summary = "A customer-authenticated ping endpoint (to verify customer-level authentication)")
    @GetMapping("/ping/customer")
    public String pingCustomer() {
        return "\"pong\"";
    }

    @Secured(Authority.Authorities.ANON)
    @Operation(summary = "Retrieve basic information about the company configured in the Inspire TMS instance")
    @GetMapping("/about")
    public AboutResponse getAbout() throws Exception {

        PostgresConnection conn = PostgresConnection.getInstance();

        Optional<CompaniesRecord> companyRecord = conn.with((supabase) ->
                supabase.selectFrom(Tables.COMPANIES).fetchOne()
        );

        if (companyRecord.isEmpty()) throw new RuntimeException("unable to load company");

        // company
        @Nullable String fileURL = null;
        if (Objects.nonNull(companyRecord.get().getLogoFileId())) {
            URIBuilder builder = new URIBuilder(Environment.get(EnvironmentVariables.SITE_URL));
            builder.setPath("/api/v1/files/" + companyRecord.get().getLogoFileId());
            fileURL = builder.build().toString();
        }
        AboutCompanyResponse company = new AboutCompanyResponse(
                companyRecord.get().getDisplayName(),
                companyRecord.get().getIsPublished(),
                fileURL,
                companyRecord.get().getType() == CompanyTypes.BROKER,
                companyRecord.get().getType() == CompanyTypes.CARRIER
        );

        // supabase
        AboutSupabaseKeysResponse keys = new AboutSupabaseKeysResponse(Environment.get(EnvironmentVariables.SUPABASE_ANON_KEY));
        AboutSupabaseResponse supabase = new AboutSupabaseResponse(Environment.get(EnvironmentVariables.SUPABASE_URL), keys);

        return new AboutResponse(Config.VERSION, company, supabase);
    }

}
