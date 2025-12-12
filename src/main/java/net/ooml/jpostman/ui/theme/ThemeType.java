package net.ooml.jpostman.ui.theme;

/**
 * Theme types supported by the application
 */
public enum ThemeType {
    LIGHT("Light", "com.formdev.flatlaf.FlatLightLaf"),
    DARK("Dark", "com.formdev.flatlaf.FlatDarkLaf"),
    INTELLIJ("IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf"),
    DARCULA("Darcula", "com.formdev.flatlaf.FlatDarculaLaf");

    private final String displayName;
    private final String className;

    ThemeType(String displayName, String className) {
        this.displayName = displayName;
        this.className = className;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClassName() {
        return className;
    }

    public static ThemeType fromString(String name) {
        if (name == null) {
            return LIGHT;
        }
        try {
            return ThemeType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LIGHT;
        }
    }
}
