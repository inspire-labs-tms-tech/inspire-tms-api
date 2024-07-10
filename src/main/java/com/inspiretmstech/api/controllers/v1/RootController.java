package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.auth.Authority;
import com.inspiretmstech.api.constants.Config;
import com.inspiretmstech.api.constants.Environment;
import com.inspiretmstech.api.models.responses.VersionResponse;
import com.inspiretmstech.api.models.responses.about.AboutCompanyResponse;
import com.inspiretmstech.api.models.responses.about.AboutResponse;
import com.inspiretmstech.api.models.responses.about.AboutSupabaseKeysResponse;
import com.inspiretmstech.api.models.responses.about.AboutSupabaseResponse;
import com.inspiretmstech.api.utils.DatabaseConnection;
import com.inspiretmstech.db.Tables;
import com.inspiretmstech.db.tables.records.CompaniesRecord;
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
    @GetMapping("/version")
    public VersionResponse getVersion() {
        return new VersionResponse(Config.VERSION);
    }

    @Secured(Authority.Authorities.ANON)
    @GetMapping("/ping")
    public String ping() {
        return "\"pong\"";
    }

    @Secured(Authority.Authorities.USER)
    @GetMapping("/ping/user")
    public String pingUser() {
        return "\"pong\"";
    }

    @Secured(Authority.Authorities.CUSTOMER)
    @GetMapping("/ping/customer")
    public String pingCustomer() {
        return "\"pong\"";
    }

    @Secured(Authority.Authorities.ANON)
    @GetMapping("/about")
    public AboutResponse getAbout() throws Exception {

        DatabaseConnection conn = DatabaseConnection.getInstance();

        Optional<CompaniesRecord> companyRecord = conn.with((supabase) ->
                supabase.selectFrom(Tables.COMPANIES).fetchOne()
        );

        if (companyRecord.isEmpty()) throw new RuntimeException("unable to load company");

        // company
        @Nullable String fileURL = null;
        if (Objects.nonNull(companyRecord.get().getLogoFileId())) {
            URIBuilder builder = new URIBuilder(Environment.get(Environment.Variables.SITE_URL));
            builder.setPath("/api/v1/files/" + companyRecord.get().getLogoFileId());
            fileURL = builder.build().toString();
        }
        AboutCompanyResponse company = new AboutCompanyResponse(companyRecord.get().getDisplayName(), companyRecord.get().getIsPublished(), fileURL);

        // supabase
        AboutSupabaseKeysResponse keys = new AboutSupabaseKeysResponse(Environment.get(Environment.Variables.SUPABASE_ANON_KEY));
        AboutSupabaseResponse supabase = new AboutSupabaseResponse(Environment.get(Environment.Variables.SUPABASE_URL), keys);

        return new AboutResponse(Config.VERSION, company, supabase);
    }

}
