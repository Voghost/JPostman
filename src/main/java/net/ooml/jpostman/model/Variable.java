package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Variable model for environment and global variables
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable {
    private String key;
    private String value;

    @Builder.Default
    private Boolean enabled = true;

    private String description;

    @Builder.Default
    private Boolean secret = false; // Mark as secret (e.g., API keys, passwords)
}
