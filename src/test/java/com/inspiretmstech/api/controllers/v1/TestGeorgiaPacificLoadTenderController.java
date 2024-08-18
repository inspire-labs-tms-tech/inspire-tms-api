package com.inspiretmstech.api.controllers.v1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.inspiretmstech.api.controllers.v1.utils.FileLoader;
import com.inspiretmstech.api.src.models.requests.tenders.gp.GeorgiaPacificLoadTender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
public class TestGeorgiaPacificLoadTenderController {

    @Autowired
    private MockMvc server;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(server);
    }

    @Test
    void testFilesDeserialize() throws Exception {

        List<JsonArray> tests = this.getTestCases();

        // needs to load 149 tests
        Assertions.assertEquals(149, tests.size());

        // ensure all test-cases parse
        Gson gson = new Gson();
        for (JsonArray test : tests) gson.fromJson(test, GeorgiaPacificLoadTender.Shipment[].class);

    }

    private List<JsonArray> getTestCases() throws IOException {
        List<JsonArray> tests = new ArrayList<>();
        List<String> files = FileLoader.from(resourceLoader).loadAsString("classpath:/gp/*.json");
        for (String file : files) {
            JsonArray data = JsonParser.parseString(file).getAsJsonArray();
            tests.add(data);
        }
        return tests;
    }

}
