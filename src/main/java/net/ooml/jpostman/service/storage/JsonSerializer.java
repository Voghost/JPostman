package net.ooml.jpostman.service.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON serialization utility
 * Provides configured ObjectMapper instance
 */
public class JsonSerializer {

    private static ObjectMapper objectMapper;

    /**
     * Get configured ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            synchronized (JsonSerializer.class) {
                if (objectMapper == null) {
                    objectMapper = createObjectMapper();
                }
            }
        }
        return objectMapper;
    }

    /**
     * Create and configure ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Don't fail on unknown properties
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
