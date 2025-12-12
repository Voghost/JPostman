package net.ooml.jpostman.config;

/**
 * Application constants
 */
public class Constants {

    // Application Info
    public static final String APP_NAME = "JPostman";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_DESCRIPTION = "A Postman-like HTTP Client";

    // Window Settings
    public static final int DEFAULT_WINDOW_WIDTH = 1200;
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    public static final int DEFAULT_LEFT_PANEL_WIDTH = 300;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;

    // Request Settings
    public static final int DEFAULT_TIMEOUT_MS = 30000; // 30 seconds
    public static final int MAX_HISTORY_ENTRIES = 100;
    public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;
    public static final boolean DEFAULT_VALIDATE_SSL = true;

    // UI Constants
    public static final int DIVIDER_SIZE = 6;
    public static final int COMPONENT_PADDING = 10;
    public static final int BUTTON_HEIGHT = 30;
    public static final int TEXT_FIELD_HEIGHT = 28;

    // Variable Pattern
    public static final String VARIABLE_PATTERN = "\\{\\{([^}]+)\\}\\}";

    // File Extensions
    public static final String JSON_EXTENSION = ".json";

    // Date Format
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // HTTP
    public static final String DEFAULT_USER_AGENT = "JPostman/" + APP_VERSION;
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_TEXT = "text/plain";

    // Character Encoding
    public static final String DEFAULT_CHARSET = "UTF-8";

    // Logging
    public static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";

    private Constants() {
        // Prevent instantiation
    }
}
