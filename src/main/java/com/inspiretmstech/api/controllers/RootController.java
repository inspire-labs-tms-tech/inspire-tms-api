package com.inspiretmstech.api.controllers;

import com.inspiretmstech.api.constants.Config;
import com.inspiretmstech.api.constants.Environment;
import com.inspiretmstech.api.models.responses.ErrorResponse;
import com.inspiretmstech.api.models.responses.VersionResponse;
import com.inspiretmstech.api.models.responses.about.AboutCompanyResponse;
import com.inspiretmstech.api.models.responses.about.AboutResponse;
import com.inspiretmstech.api.models.responses.about.AboutSupabaseKeysResponse;
import com.inspiretmstech.api.models.responses.about.AboutSupabaseResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class RootController {

    @GetMapping("/version")
    public VersionResponse getVersion() {
        return new VersionResponse(Config.VERSION);
    }

    @GetMapping("/ping")
    public String ping() {
        return "\"pong\"";
    }

    @GetMapping("/about")
    public AboutResponse getAbout() {

        // company
        AboutCompanyResponse company = new AboutCompanyResponse("", false, "");

        // supabase
        AboutSupabaseKeysResponse keys = new AboutSupabaseKeysResponse(Environment.get(Environment.Variables.SUPABASE_ANON_KEY));
        AboutSupabaseResponse supabase = new AboutSupabaseResponse(Environment.get(Environment.Variables.SUPABASE_URL), keys);

        return new AboutResponse(Config.VERSION, company, supabase);
    }

}
