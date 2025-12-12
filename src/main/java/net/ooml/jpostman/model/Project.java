package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Project model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Settings settings;

    /**
     * Project settings
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Settings {
        private String defaultEnvironment;
    }

    /**
     * Create a new project
     */
    public static Project createNew(String name) {
        LocalDateTime now = LocalDateTime.now();
        return Project.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .createdAt(now)
                .updatedAt(now)
                .settings(Settings.builder().build())
                .build();
    }

    /**
     * Update the modified timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
