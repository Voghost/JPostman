package net.ooml.jpostman.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Validation utility class
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Validate URL
     */
    public static boolean isValidUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return false;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate email
     */
    public static boolean isValidEmail(String email) {
        if (StringUtil.isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate port number
     */
    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }

    /**
     * Validate timeout value
     */
    public static boolean isValidTimeout(int timeout) {
        return timeout > 0 && timeout <= 600000; // Max 10 minutes
    }

    /**
     * Check if string contains variables ({{variable}})
     */
    public static boolean containsVariables(String text) {
        if (StringUtil.isEmpty(text)) {
            return false;
        }
        return VARIABLE_PATTERN.matcher(text).find();
    }

    /**
     * Validate JSON content type
     */
    public static boolean isJsonContentType(String contentType) {
        if (StringUtil.isEmpty(contentType)) {
            return false;
        }
        return contentType.toLowerCase().contains("application/json");
    }

    /**
     * Validate XML content type
     */
    public static boolean isXmlContentType(String contentType) {
        if (StringUtil.isEmpty(contentType)) {
            return false;
        }
        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("application/xml") ||
                lowerContentType.contains("text/xml");
    }

    private ValidationUtil() {
        // Prevent instantiation
    }
}
