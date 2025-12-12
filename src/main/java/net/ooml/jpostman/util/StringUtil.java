package net.ooml.jpostman.util;

/**
 * String utility class
 */
public class StringUtil {

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Get string or default value if null/empty
     */
    public static String getOrDefault(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Truncate string to max length
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Format file size to human readable format
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Format duration to human readable format
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + " ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2f s", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%d min %d s", minutes, seconds);
        }
    }

    private StringUtil() {
        // Prevent instantiation
    }
}
