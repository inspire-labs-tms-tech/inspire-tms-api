package com.inspiretmstech.api.controllers.v1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inspiretmstech.api.controllers.v1.utils.FileLoader;
import com.inspiretmstech.api.src.models.requests.tenders.gp.GeorgiaPacificLoadTender;
import com.inspiretmstech.api.src.models.requests.truckertools.TruckerToolsCommentsRequest;
import com.inspiretmstech.api.src.models.requests.truckertools.TruckerToolsDocumentsRequest;
import com.inspiretmstech.api.src.models.requests.truckertools.TruckerToolsStatusRequest;
import com.inspiretmstech.db.tables.TruckerToolsLoadComments;
import com.inspiretmstech.db.tables.TruckerToolsLoadStatuses;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class TestTruckerToolsController {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private MockMvc server;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(server);
    }

    @Test
    void testFilesDeserialize() throws Exception {

        List<JsonObject> tests = new ArrayList<>();
        List<String> files = FileLoader.from(resourceLoader).loadAsString("classpath:/truckertools/*.json");
        for (String file : files) {
            JsonObject data = JsonParser.parseString(file).getAsJsonObject();
            tests.add(data);
        }

        // needs to load 16 tests
        Assertions.assertEquals(16, tests.size());

        // ensure all test-cases parse
        Gson gson = new Gson();
        for (JsonObject test : tests)
            switch (test.get("type").getAsString()) {
                case "StatusUpdate" -> gson.fromJson(test, TruckerToolsStatusRequest.class);
                case "CommentUpdate" -> gson.fromJson(test, TruckerToolsCommentsRequest.class);
                case "DocumentUpdate" -> gson.fromJson(test, TruckerToolsDocumentsRequest.class);
                default -> Assertions.fail("Unexpected type: " + test.get("type").getAsString());
            }

    }

}
