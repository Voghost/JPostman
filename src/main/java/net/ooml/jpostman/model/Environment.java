package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Environment model for managing environment variables
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Environment {

    private String id;
    private String name;

    @Builder.Default
    private List<Variable> variables = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Create a new environment
     */
    public static Environment createNew(String name) {
        LocalDateTime now = LocalDateTime.now();
        return Environment.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .variables(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Get variable value by key
     */
    public String getVariableValue(String key) {
        return variables.stream()
                .filter(v -> v.getEnabled() && key.equals(v.getKey()))
                .findFirst()
                .map(Variable::getValue)
                .orElse(null);
    }

    /**
     * Add or update a variable
     */
    public void setVariable(String key, String value) {
        Variable existing = variables.stream()
                .filter(v -> key.equals(v.getKey()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setValue(value);
        } else {
            variables.add(Variable.builder()
                    .key(key)
                    .value(value)
                    .enabled(true)
                    .build());
        }
        touch();
    }

    /**
     * Update the modified timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
