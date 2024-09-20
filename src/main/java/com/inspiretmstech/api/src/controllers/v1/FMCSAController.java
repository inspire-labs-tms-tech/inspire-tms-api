package com.inspiretmstech.api.src.controllers.v1;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.auth.requires.Requires;
import com.inspiretmstech.api.src.auth.requires.Scopes;
import com.inspiretmstech.api.src.constants.EnvironmentVariables;
import com.inspiretmstech.api.src.models.responses.fmcsa.FMCSAResult;
import com.inspiretmstech.common.utils.Environment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "FMCSA", description = "Query the FMCSA Database for Carriers")
@RequestMapping("/v1/fmcsa")
public class FMCSAController {

    private final Logger logger = LoggerFactory.getLogger(FMCSAController.class);

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.CARRIERS)
    @Operation(summary = "Query by DOT Number")
    @PostMapping("/mc-number")
    public List<FMCSAResult> fmcsaLookupMCNumber(@RequestBody int mcNumber) {
        return this.queryFMCSA("/qc/services/carriers/docket-number/" + mcNumber);
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.CARRIERS)
    @Operation(summary = "Query by DOT Number")
    @PostMapping("/dot-number")
    public List<FMCSAResult> fmcsaLookupDOTNumber(@RequestBody int dotNumber) {
        return this.queryFMCSA("/qc/services/carriers/" + dotNumber);
    }

    @Secured(Authority.Authorities.USER)
    @Requires(Scopes.CARRIERS)
    @Operation(summary = "Query by Name")
    @PostMapping("/name")
    public List<FMCSAResult> fmcsaLookupName(@RequestBody String name) {
        String safeName = JsonParser.parseString(name).getAsString().replace(' ', '_');
        return this.queryFMCSA("/qc/services/carriers/name/" + safeName);
    }

    private List<FMCSAResult> queryFMCSA(String endpoint) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {


            URIBuilder builder = new URIBuilder("https://mobile.fmcsa.dot.gov/");
            builder.setPath(endpoint);
            builder.addParameter("webKey", Environment.get(EnvironmentVariables.FMCSA_WEB_KEY));

            String url = builder.build().toString();
            logger.trace(url);
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(request)) {

                String responseBody = EntityUtils.toString(response.getEntity());

                if (response.getStatusLine().getStatusCode() == 200) {

                    JsonObject body = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonElement content = body.get("content");

                    Gson gson = new Gson();
                    List<FMCSAResult> carriers = new ArrayList<>();

                    if (content.isJsonArray())
                        for (JsonElement object : content.getAsJsonArray())
                            carriers.add(gson.fromJson(object, FMCSAResult.class));
                    else if (content.isJsonObject())
                        carriers.add(gson.fromJson(content.getAsJsonObject(), FMCSAResult.class));

                    return carriers;
                } else {
                    logger.error("invalid response ({}): {}", response.getStatusLine().getStatusCode(), responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while parsing FMCSA response: {}", e.getMessage());
        }
        return List.of();
    }

}
