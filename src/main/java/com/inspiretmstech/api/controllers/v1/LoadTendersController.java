package com.inspiretmstech.api.controllers.v1;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/load-tenders")
public class LoadTendersController {

    @PostMapping
    public String createLoadTender() {
        System.out.println("Load Tender Created");
        return "FOO";
    }

}
