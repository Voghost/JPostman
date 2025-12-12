package net.ooml.jpostman.service.variable;

/**
 * Variable scope enumeration
 */
public enum VariableScope {
    GLOBAL("Global"),
    ENVIRONMENT("Environment"),
    LOCAL("Local");

    private final String displayName;

    VariableScope(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
