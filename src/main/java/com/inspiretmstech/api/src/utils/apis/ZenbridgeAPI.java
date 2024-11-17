package com.inspiretmstech.api.src.utils.apis;

import com.inspiretmstech.common.utils.Environment;

public class ZenbridgeAPI {

    public static String getBaseURL() {
        String VERSION = Environment.get("VERSION");
        return switch (VERSION) {
            case "main" -> "https://api.zenbridge.io";
            case "development" -> "https://api.sandbox.zenbridge.io";
            default -> throw new RuntimeException("VERSION '" + VERSION + "' is unhandled");
        };
    }

}
