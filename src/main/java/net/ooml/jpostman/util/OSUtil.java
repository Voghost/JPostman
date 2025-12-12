package net.ooml.jpostman.util;

/**
 * Operating system utility class
 */
public class OSUtil {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    /**
     * Check if running on macOS
     */
    public static boolean isMac() {
        return OS_NAME.contains("mac");
    }

    /**
     * Check if running on macOS (alias for isMac)
     */
    public static boolean isMacOS() {
        return isMac();
    }

    /**
     * Check if running on Windows
     */
    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }

    /**
     * Check if running on Linux
     */
    public static boolean isLinux() {
        return OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix");
    }

    /**
     * Get OS name
     */
    public static String getOSName() {
        return OS_NAME;
    }

    /**
     * Get user home directory
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    private OSUtil() {
        // Prevent instantiation
    }
}
