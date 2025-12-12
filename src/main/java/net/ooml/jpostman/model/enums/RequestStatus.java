package net.ooml.jpostman.model.enums;

/**
 * Request execution status enumeration
 */
public enum RequestStatus {
    SUCCESS("Success"),
    ERROR("Error"),
    TIMEOUT("Timeout"),
    CANCELLED("Cancelled");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static RequestStatus fromString(String value) {
        for (RequestStatus status : RequestStatus.values()) {
            if (status.name().equalsIgnoreCase(value) ||
                    status.displayName.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ERROR; // Default to ERROR
    }
}
