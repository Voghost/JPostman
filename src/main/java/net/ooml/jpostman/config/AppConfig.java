package net.ooml.jpostman.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private String version;
    private String theme; // LIGHT or DARK
    private String language; // en, zh_CN, etc.
    private String currentProject;
    private Integer historyLimit;
    private Integer timeout;
    private Boolean followRedirects;
    private Boolean validateSSL;
    private WindowState windowState;

    /**
     * Helper methods for window dimensions
     */
    public int getWindowWidth() {
        return windowState != null && windowState.width != null
                ? windowState.width
                : Constants.DEFAULT_WINDOW_WIDTH;
    }

    public void setWindowWidth(int width) {
        if (windowState == null) {
            windowState = WindowState.getDefault();
        }
        windowState.width = width;
    }

    public int getWindowHeight() {
        return windowState != null && windowState.height != null
                ? windowState.height
                : Constants.DEFAULT_WINDOW_HEIGHT;
    }

    public void setWindowHeight(int height) {
        if (windowState == null) {
            windowState = WindowState.getDefault();
        }
        windowState.height = height;
    }

    /**
     * Window state configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WindowState {
        private Integer width;
        private Integer height;
        private Integer x;
        private Integer y;
        private Boolean maximized;
        private Integer leftPanelWidth;

        public static WindowState getDefault() {
            return WindowState.builder()
                    .width(Constants.DEFAULT_WINDOW_WIDTH)
                    .height(Constants.DEFAULT_WINDOW_HEIGHT)
                    .x(100)
                    .y(100)
                    .maximized(false)
                    .leftPanelWidth(Constants.DEFAULT_LEFT_PANEL_WIDTH)
                    .build();
        }
    }

    /**
     * Create default configuration
     */
    public static AppConfig getDefault() {
        return AppConfig.builder()
                .version(Constants.APP_VERSION)
                .theme("DARCULA")
                .language("en")
                .currentProject(PathConfig.getDefaultProjectName())
                .historyLimit(Constants.MAX_HISTORY_ENTRIES)
                .timeout(Constants.DEFAULT_TIMEOUT_MS)
                .followRedirects(Constants.DEFAULT_FOLLOW_REDIRECTS)
                .validateSSL(Constants.DEFAULT_VALIDATE_SSL)
                .windowState(WindowState.getDefault())
                .build();
    }

    /**
     * Load configuration from file
     * If file doesn't exist, return default configuration
     */
    public static AppConfig load() {
        Path configFile = PathConfig.getAppSettingsFile();

        if (!Files.exists(configFile)) {
            log.info("Config file not found, using default configuration");
            AppConfig config = getDefault();
            try {
                config.save();
            } catch (IOException e) {
                log.error("Failed to save default configuration", e);
            }
            return config;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            AppConfig config = mapper.readValue(configFile.toFile(), AppConfig.class);
            log.info("Configuration loaded successfully");
            return config;
        } catch (IOException e) {
            log.error("Failed to load configuration, using defaults", e);
            return getDefault();
        }
    }

    /**
     * Save configuration to file
     */
    public void save() throws IOException {
        Path configFile = PathConfig.getAppSettingsFile();
        ObjectMapper mapper = new ObjectMapper();

        // Ensure config directory exists
        Files.createDirectories(configFile.getParent());

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(configFile.toFile(), this);

        log.info("Configuration saved successfully");
    }
}
