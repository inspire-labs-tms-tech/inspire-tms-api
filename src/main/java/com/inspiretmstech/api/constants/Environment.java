package com.inspiretmstech.api.constants;

import java.util.Optional;

public class Environment {

    public enum Variables {
        SUPABASE_URL,
        SUPABASE_ANON_KEY,
        DB_USER,
        DB_PASS,
        DB_HOST,
        DB_PORT,
        DB_NAME,
        SITE_URL,
        GOOGLE_GEOCODING_API_KEY
    }

    public static String get(Variables variable) {
        Optional<String> value = Optional.ofNullable(System.getenv(variable.name()));
        if(value.isPresent()) return value.get();
        throw new RuntimeException("environment variable " + variable.name() + " is not defined at runtime");
    }

}
