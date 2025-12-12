package net.ooml.jpostman;

import net.ooml.jpostman.config.AppConfig;
import net.ooml.jpostman.config.Constants;
import net.ooml.jpostman.config.PathConfig;
import net.ooml.jpostman.service.storage.StorageService;
import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;
import net.ooml.jpostman.ui.theme.ThemeManager;
import net.ooml.jpostman.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * JPostman Application - Main entry point
 */
public class JPostmanApplication {
    private static final Logger log = LoggerFactory.getLogger(JPostmanApplication.class);

    public static void main(String[] args) {
        // macOS specific settings - must be set before GUI creation
        if (OSUtil.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "JPostman");
            System.setProperty("apple.awt.application.appearance", "system");
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                startApplication();
            } catch (Exception e) {
                log.error("Failed to start JPostman", e);
                showErrorAndExit("Failed to start JPostman: " + e.getMessage());
            }
        });
    }

    private static void startApplication() throws Exception {
        log.info("Starting JPostman v{}", Constants.APP_VERSION);

        // Step 1: Initialize application directories
        PathConfig.initializeDirectories();
        log.info("Application directories initialized");

        // Step 2: Load application configuration
        AppConfig config = AppConfig.load();
        log.info("Configuration loaded: theme={}, language={}, project={}",
                config.getTheme(), config.getLanguage(), config.getCurrentProject());

        // Step 3: Initialize internationalization
        // If no language configured, use system language
        if (config.getLanguage() == null || config.getLanguage().isEmpty()) {
            I18nManager.initialize();
            log.info("I18n initialized with system language: {}", I18nManager.getCurrentLanguage().getDisplayName());
        } else {
            I18nManager.initialize(config.getLanguage());
            log.info("I18n initialized with configured language: {}", I18nManager.getCurrentLanguage().getDisplayName());
        }

        // Step 4: Apply theme
        ThemeManager.getInstance().initialize(config);
        log.info("Theme applied: {}", ThemeManager.getInstance().getCurrentTheme());

        // Step 5: Ensure default project exists
        StorageService storage = StorageService.getInstance();
        String projectName = config.getCurrentProject();
        if (!storage.projectExists(projectName)) {
            storage.createProject(projectName);
            log.info("Default project created: {}", projectName);
        }

        // Step 6: Create and show main frame
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);

        log.info("JPostman started successfully");
    }

    /**
     * Show error dialog and exit
     */
    private static void showErrorAndExit(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "JPostman - Error",
                JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }
}
