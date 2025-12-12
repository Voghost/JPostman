package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ooml.jpostman.model.enums.AuthType;

/**
 * Authentication configuration model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthConfig {

    @Builder.Default
    private AuthType type = AuthType.NONE;

    // Basic Auth
    private String username;
    private String password;

    // Bearer Token
    private String token;

    // API Key
    private String apiKey;
    private String apiKeyHeader; // Header name for API key

    public static AuthConfig createNone() {
        return AuthConfig.builder()
                .type(AuthType.NONE)
                .build();
    }

    public static AuthConfig createBasicAuth(String username, String password) {
        return AuthConfig.builder()
                .type(AuthType.BASIC)
                .username(username)
                .password(password)
                .build();
    }

    public static AuthConfig createBearerToken(String token) {
        return AuthConfig.builder()
                .type(AuthType.BEARER)
                .token(token)
                .build();
    }

    public static AuthConfig createApiKey(String apiKey, String headerName) {
        return AuthConfig.builder()
                .type(AuthType.API_KEY)
                .apiKey(apiKey)
                .apiKeyHeader(headerName)
                .build();
    }
}
