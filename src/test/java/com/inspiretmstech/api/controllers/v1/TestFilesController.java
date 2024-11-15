package com.inspiretmstech.api.controllers.v1;

import com.inspiretmstech.api.src.auth.AuthenticatedAuthenticationToken;
import com.inspiretmstech.api.src.auth.methods.apikey.APIKeyAuthenticationHolder;
import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // disable security
public class TestFilesController {

    @Autowired
    private MockMvc server;

    @BeforeEach
    void setup() {
        // disable security
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(new APIKeyAuthenticationHolder("", null), null, List.of(Authority.USER)));
    }


    @Test
    void contextLoads() {
        Assertions.assertNotNull(server);
    }

    @Test
    void emptyBody() throws Exception {
        server.perform(MockMvcRequestBuilders.post("/v1/files/pdfs/merge"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Required request body is missing")));
    }

    @Test
    void lessThanTwoFiles() throws Exception {
        server.perform(MockMvcRequestBuilders
                        .post("/v1/files/pdfs/merge")
                        .contentType("application/json")
                        .content("[]")
                )
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Cannot merge less than 2 PDFs"));
    }

    @Test
    void twoFiles() throws Exception {
        server.perform(MockMvcRequestBuilders
                        .post("/v1/files/pdfs/merge")
                        .queryParam("upload", "false")
                        .contentType("application/json")
                        .content("""
                                [
                                "https://www.princexml.com/howcome/2016/samples/invoice/index.pdf",
                                "https://www.princexml.com/samples/invoice/invoicesample.pdf"
                                ]
                                """)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

}
