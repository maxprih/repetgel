package com.maxpri.repetgel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private KeycloakProperties keycloak = new KeycloakProperties();
    private FilesProperties files = new FilesProperties();

    @Getter
    @Setter
    public static class KeycloakProperties {
        private String realm;
        private Map<String, String> roles;
    }

    @Getter
    @Setter
    public static class FilesProperties {
        private int presignedUrlDurationMinutes;
    }
}