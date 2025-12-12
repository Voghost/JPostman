package net.ooml.jpostman.model.enums;

/**
 * Authentication type enumeration
 */
public enum AuthType {
    NONE("None"),
    BASIC("Basic Auth"),
    BEARER("Bearer Token"),
    API_KEY("API Key"),
    OAUTH2("OAuth 2.0");

    private final String displayName;

    AuthType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static AuthType fromString(String value) {
        for (AuthType type : AuthType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                    type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return NONE; // Default to NONE
    }
}
