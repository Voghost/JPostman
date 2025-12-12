package net.ooml.jpostman.model.enums;

/**
 * HTTP method enumeration
 */
public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    private final String value;

    HttpMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static HttpMethod fromString(String value) {
        for (HttpMethod method : HttpMethod.values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        return GET; // Default to GET
    }
}
