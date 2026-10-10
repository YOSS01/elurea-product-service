package com.elurea.product_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Typed view of the "app.*" settings in application.properties.
 */
@ConfigurationProperties("app")
public record AppProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue Cors cors,
        @DefaultValue Storage storage,
        String internalApiKey
) {

    /** Must match the user service, which signs the tokens. */
    public record Jwt(String secret, @DefaultValue("elurea-user-service") String issuer) {
    }

    public record Cors(@DefaultValue("http://localhost:3000") List<String> allowedOrigins) {
    }

    public record Storage(@DefaultValue("uploads") String localDir, String publicBaseUrl) {
    }
}
