package net.ooml.jpostman.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON utility class
 */
public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * Create configured ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /**
     * Get the shared ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON", e);
            return null;
        }
    }

    /**
     * Convert object to pretty JSON string
     */
    public static String toPrettyJson(Object obj) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to pretty JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON", e);
            return null;
        }
    }

    /**
     * Format JSON string (prettify)
     */
    public static String formatJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return json;
        }
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to format JSON", e);
            return json;
        }
    }

    /**
     * Validate JSON string
     */
    public static boolean isValidJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private JsonUtil() {
        // Prevent instantiation
    }
}
