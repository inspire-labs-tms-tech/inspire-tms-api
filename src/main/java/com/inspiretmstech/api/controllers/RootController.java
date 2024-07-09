package com.inspiretmstech.api.controllers;

import com.inspiretmstech.api.constants.Config;
import com.inspiretmstech.api.models.responses.VersionResponse;
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

}
