package net.ooml.jpostman.ui.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Internationalization manager
 * Singleton pattern for managing application messages
 */
public class I18nManager {
    private static final Logger log = LoggerFactory.getLogger(I18nManager.class);

    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    private static I18nManager instance;
    private ResourceBundle bundle;
    private LanguageType currentLanguage;

    private I18nManager() {
        // Private constructor for singleton
    }

    /**
     * Get singleton instance
     */
    public static I18nManager getInstance() {
        if (instance == null) {
            synchronized (I18nManager.class) {
                if (instance == null) {
                    instance = new I18nManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize with default language (from system)
     */
    public static void initialize() {
        initialize(detectSystemLanguage());
    }

    /**
     * Detect system default language
     */
    public static LanguageType detectSystemLanguage() {
        Locale systemLocale = Locale.getDefault();
        String language = systemLocale.getLanguage();

        log.debug("System locale: {}, language: {}", systemLocale, language);

        // Check if it's Chinese
        if ("zh".equals(language)) {
            return LanguageType.CHINESE;
        }

        // Default to English
        return LanguageType.ENGLISH;
    }

    /**
     * Initialize with specific language
     */
    public static void initialize(String languageCode) {
        LanguageType language = LanguageType.fromCode(languageCode);
        initialize(language);
    }

    /**
     * Initialize with specific language type
     */
    public static void initialize(LanguageType language) {
        I18nManager manager = getInstance();
        manager.currentLanguage = language;
        manager.loadBundle(language.getLocale());
        log.info("I18nManager initialized with language: {}", language.getDisplayName());
    }

    /**
     * Load resource bundle for specified locale
     */
    private void loadBundle(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
            log.debug("Resource bundle loaded for locale: {}", locale);
        } catch (Exception e) {
            log.error("Failed to load resource bundle for locale: {}", locale, e);
            // Fallback to English
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.ENGLISH);
        }
    }

    /**
     * Get message by key
     */
    public static String get(String key) {
        I18nManager manager = getInstance();
        if (manager.bundle == null) {
            initialize();
        }

        try {
            return manager.bundle.getString(key);
        } catch (Exception e) {
            log.warn("Message key not found: {}", key);
            return key; // Return the key itself if not found
        }
    }

    /**
     * Get formatted message with parameters
     * Example: get("error.networkError", "Connection timeout")
     */
    public static String get(String key, Object... params) {
        String message = get(key);
        try {
            return MessageFormat.format(message, params);
        } catch (Exception e) {
            log.warn("Failed to format message: {}", key, e);
            return message;
        }
    }

    /**
     * Change language at runtime
     */
    public static void changeLanguage(LanguageType language) {
        I18nManager manager = getInstance();
        manager.currentLanguage = language;
        manager.loadBundle(language.getLocale());
        log.info("Language changed to: {}", language.getDisplayName());
    }

    /**
     * Get current language
     */
    public static LanguageType getCurrentLanguage() {
        I18nManager manager = getInstance();
        return manager.currentLanguage != null ? manager.currentLanguage : LanguageType.ENGLISH;
    }

    /**
     * Get current locale
     */
    public static Locale getCurrentLocale() {
        return getCurrentLanguage().getLocale();
    }

    /**
     * Check if key exists
     */
    public static boolean hasKey(String key) {
        I18nManager manager = getInstance();
        if (manager.bundle == null) {
            initialize();
        }

        try {
            manager.bundle.getString(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
