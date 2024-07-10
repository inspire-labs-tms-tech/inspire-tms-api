package com.inspiretmstech.api.configurations;

import com.google.gson.Gson;
import com.inspiretmstech.api.auth.AuthenticationFilter;
import com.inspiretmstech.api.auth.bearer.APIKeyDetailsService;
import com.inspiretmstech.api.models.responses.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Bean
    UserDetailsService userDetailsService() {
        return new APIKeyDetailsService();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new GlobalAuthenticationFailureHandler();
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(AbstractHttpConfigurer::disable)
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
                .build();
    }

}
