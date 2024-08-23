package com.inspiretmstech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {OAuth2ClientAutoConfiguration.class, SecurityAutoConfiguration.class})
public class API {

    public static void main(String[] args) {
        SpringApplication.run(API.class, args);
    }

}
