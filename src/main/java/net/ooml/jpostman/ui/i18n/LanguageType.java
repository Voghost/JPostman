package net.ooml.jpostman.ui.i18n;

import java.util.Locale;

/**
 * Language type enumeration
 */
public enum LanguageType {
    ENGLISH("en", "English", Locale.ENGLISH),
    CHINESE("zh_CN", "简体中文", Locale.SIMPLIFIED_CHINESE);

    private final String code;
    private final String displayName;
    private final Locale locale;

    LanguageType(String code, String displayName, Locale locale) {
        this.code = code;
        this.displayName = displayName;
        this.locale = locale;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Get LanguageType from code
     */
    public static LanguageType fromCode(String code) {
        for (LanguageType type : LanguageType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return ENGLISH; // Default to English
    }
}
