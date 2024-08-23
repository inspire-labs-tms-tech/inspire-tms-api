package com.inspiretmstech.api.src.configurations;

import com.google.gson.Gson;
import com.inspiretmstech.api.src.auth.AuthenticationFilter;
import com.inspiretmstech.api.src.constants.EnvironmentVariables;
import com.inspiretmstech.api.src.models.responses.ErrorResponse;
import com.inspiretmstech.common.utils.Environment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration {

    private final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new GlobalAuthenticationFailureHandler();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                try {
                    URI uri = new URI(Environment.get(EnvironmentVariables.SITE_URL));
                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    int port = uri.getPort();

                    String origin;
                    if (port == -1) origin = scheme + "://" + host;
                    else origin = scheme + "://" + host + ":" + port;

                    registry
                            .addMapping("/**")
                            .allowedOrigins(origin)
                            .allowedMethods("*");

                    logger.info("CORS enabled for: {}", origin);

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .addFilterAt(new AuthenticationFilter(), BasicAuthenticationFilter.class)

                .exceptionHandling(customizer -> {
                    customizer.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json");

                        ErrorResponse r = new ErrorResponse(accessDeniedException.getMessage(), "An authorization error occurred: " + accessDeniedException.getMessage(), "");

                        Gson gson = new Gson();
                        String json = gson.toJson(r);

                        response.getWriter().write(json);
                        response.getWriter().flush();
                    });
                    customizer.authenticationEntryPoint((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json");

                        ErrorResponse r = new ErrorResponse(accessDeniedException.getMessage(), "An authorization error occurred: " + accessDeniedException.getMessage(), "");

                        Gson gson = new Gson();
                        String json = gson.toJson(r);

                        response.getWriter().write(json);
                        response.getWriter().flush();
                    });
                })
                .headers(header -> header.addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Origin", "*")))
                .build();
    }

}
