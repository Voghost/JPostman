package net.ooml.jpostman.ui.theme;

import com.formdev.flatlaf.FlatLaf;
import net.ooml.jpostman.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Theme Manager - Singleton for managing application themes
 */
public class ThemeManager {
    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    private static ThemeManager instance;

    private ThemeType currentTheme;

    private ThemeManager() {
        // Private constructor for singleton
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) {
                    instance = new ThemeManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize theme from config
     */
    public void initialize(AppConfig config) {
        String themeName = config.getTheme();
        ThemeType theme = ThemeType.fromString(themeName);
        applyTheme(theme);
        log.info("Theme initialized: {}", theme.getDisplayName());
    }

    /**
     * Apply theme
     */
    public void applyTheme(ThemeType theme) {
        try {
            // Set look and feel
            UIManager.setLookAndFeel(theme.getClassName());

            // Update all existing windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }

            this.currentTheme = theme;
            log.info("Theme applied: {}", theme.getDisplayName());

        } catch (Exception e) {
            log.error("Failed to apply theme: {}", theme.getDisplayName(), e);
            // Fall back to system default
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                log.error("Failed to apply system look and feel", ex);
            }
        }
    }

    /**
     * Change theme and save to config
     */
    public void changeTheme(ThemeType theme) throws IOException {
        applyTheme(theme);

        // Save to config
        AppConfig config = AppConfig.load();
        config.setTheme(theme.name());
        config.save();

        log.info("Theme changed and saved: {}", theme.getDisplayName());
    }

    /**
     * Get current theme
     */
    public ThemeType getCurrentTheme() {
        return currentTheme != null ? currentTheme : ThemeType.LIGHT;
    }

    /**
     * Check if current theme is dark
     */
    public boolean isDarkTheme() {
        return currentTheme == ThemeType.DARK || currentTheme == ThemeType.DARCULA;
    }
}
