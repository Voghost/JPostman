package net.ooml.jpostman.model.enums;

/**
 * Request body type enumeration
 */
public enum BodyType {
    NONE("None"),
    JSON("JSON"),
    XML("XML"),
    FORM_DATA("Form Data"),
    X_WWW_FORM_URLENCODED("x-www-form-urlencoded"),
    RAW("Raw Text"),
    BINARY("Binary");

    private final String displayName;

    BodyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static BodyType fromString(String value) {
        for (BodyType type : BodyType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                    type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return NONE; // Default to NONE
    }
}
