package net.ooml.jpostman.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class for managing application file paths and directories
 */
public class PathConfig {
    private static final Logger log = LoggerFactory.getLogger(PathConfig.class);

    // Base application directory
    private static final String APP_DIR_NAME = ".jpostman";
    private static final Path USER_HOME = Paths.get(System.getProperty("user.home"));
    private static final Path APP_HOME = USER_HOME.resolve(APP_DIR_NAME);

    // Main directories
    private static final Path CONFIG_DIR = APP_HOME.resolve("config");
    private static final Path PROJECTS_DIR = APP_HOME.resolve("projects");
    private static final Path LOGS_DIR = APP_HOME.resolve("logs");

    // Config files
    private static final Path APP_SETTINGS_FILE = CONFIG_DIR.resolve("app-settings.json");
    private static final Path RECENT_PROJECTS_FILE = CONFIG_DIR.resolve("recent-projects.json");

    // Default project
    private static final String DEFAULT_PROJECT_NAME = "default";

    /**
     * Initialize all required application directories
     * Creates the directory structure if it doesn't exist
     */
    public static void initializeDirectories() throws IOException {
        log.info("Initializing JPostman directories...");

        // Create main directories
        createDirectory(APP_HOME);
        createDirectory(CONFIG_DIR);
        createDirectory(PROJECTS_DIR);
        createDirectory(LOGS_DIR);

        // Create default project structure
        Path defaultProjectDir = PROJECTS_DIR.resolve(DEFAULT_PROJECT_NAME);
        createDirectory(defaultProjectDir);
        createDirectory(defaultProjectDir.resolve("collections"));
        createDirectory(defaultProjectDir.resolve("environments"));

        log.info("JPostman directories initialized successfully");
    }

    /**
     * Create a directory if it doesn't exist
     */
    private static void createDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.debug("Created directory: {}", path);
        }
    }

    /**
     * Get the application home directory
     */
    public static Path getAppHome() {
        return APP_HOME;
    }

    /**
     * Get the config directory
     */
    public static Path getConfigDirectory() {
        return CONFIG_DIR;
    }

    /**
     * Get the projects directory
     */
    public static Path getProjectsDirectory() {
        return PROJECTS_DIR;
    }

    /**
     * Get the logs directory
     */
    public static Path getLogsDirectory() {
        return LOGS_DIR;
    }

    /**
     * Get the app settings file path
     */
    public static Path getAppSettingsFile() {
        return APP_SETTINGS_FILE;
    }

    /**
     * Get the recent projects file path
     */
    public static Path getRecentProjectsFile() {
        return RECENT_PROJECTS_FILE;
    }

    /**
     * Get a specific project directory
     */
    public static Path getProjectDirectory(String projectName) {
        return PROJECTS_DIR.resolve(projectName);
    }

    /**
     * Get the default project directory
     */
    public static Path getDefaultProjectDirectory() {
        return getProjectDirectory(DEFAULT_PROJECT_NAME);
    }

    /**
     * Get the default project name
     */
    public static String getDefaultProjectName() {
        return DEFAULT_PROJECT_NAME;
    }

    /**
     * Get collections directory for a project
     */
    public static Path getCollectionsDirectory(String projectName) {
        return getProjectDirectory(projectName).resolve("collections");
    }

    /**
     * Get environments directory for a project
     */
    public static Path getEnvironmentsDirectory(String projectName) {
        return getProjectDirectory(projectName).resolve("environments");
    }

    /**
     * Get project file path
     */
    public static Path getProjectFile(String projectName) {
        return getProjectDirectory(projectName).resolve("project.json");
    }

    /**
     * Get globals file path for a project
     */
    public static Path getGlobalsFile(String projectName) {
        return getProjectDirectory(projectName).resolve("globals.json");
    }

    /**
     * Get history file path for a project
     */
    public static Path getHistoryFile(String projectName) {
        return getProjectDirectory(projectName).resolve("history.json");
    }

    /**
     * Get log file path
     */
    public static Path getLogFile() {
        return LOGS_DIR.resolve("jpostman.log");
    }
}
