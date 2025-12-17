package net.ooml.jpostman.util;

import net.ooml.jpostman.model.Header;
import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.model.enums.AuthType;
import net.ooml.jpostman.model.enums.BodyType;
import net.ooml.jpostman.model.enums.HttpMethod;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for generating cURL commands from Request objects
 */
public class CurlGenerator {

    /**
     * Generate cURL command from a Request object
     * @param request The HTTP request to convert
     * @return cURL command string
     */
    public static String generateCurl(Request request) {
        if (request == null) {
            return "";
        }

        StringBuilder curl = new StringBuilder("curl");

        // Add method
        HttpMethod method = request.getMethod() != null ? request.getMethod() : HttpMethod.GET;
        if (method != HttpMethod.GET) {
            curl.append(" -X ").append(method.name());
        }

        // Add URL with query parameters
        String url = buildUrlWithParams(request);
        curl.append(" '").append(url).append("'");

        // Add headers
        if (request.getHeaders() != null) {
            for (Header header : request.getHeaders()) {
                if (Boolean.TRUE.equals(header.getEnabled()) && header.getKey() != null && !header.getKey().isEmpty()) {
                    curl.append(" \\\n  -H '")
                        .append(escapeString(header.getKey()))
                        .append(": ")
                        .append(escapeString(header.getValue() != null ? header.getValue() : ""))
                        .append("'");
                }
            }
        }

        // Add authentication
        if (request.getAuth() != null && request.getAuth().getType() != AuthType.NONE) {
            addAuthToCurl(curl, request);
        }

        // Add body
        if (request.getBody() != null && request.getBody().getType() != BodyType.NONE) {
            addBodyToCurl(curl, request);
        }

        return curl.toString();
    }

    /**
     * Build URL with query parameters
     */
    private static String buildUrlWithParams(Request request) {
        String url = request.getUrl() != null ? request.getUrl() : "";

        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            List<String> params = request.getQueryParams().stream()
                .filter(param -> Boolean.TRUE.equals(param.getEnabled()) && param.getKey() != null && !param.getKey().isEmpty())
                .map(param -> {
                    String key = param.getKey();
                    String value = param.getValue() != null ? param.getValue() : "";
                    return key + "=" + value;
                })
                .collect(Collectors.toList());

            if (!params.isEmpty()) {
                String separator = url.contains("?") ? "&" : "?";
                url = url + separator + String.join("&", params);
            }
        }

        return url;
    }

    /**
     * Add authentication to cURL command
     */
    private static void addAuthToCurl(StringBuilder curl, Request request) {
        AuthType authType = request.getAuth().getType();

        switch (authType) {
            case BASIC:
                String username = request.getAuth().getUsername() != null ? request.getAuth().getUsername() : "";
                String password = request.getAuth().getPassword() != null ? request.getAuth().getPassword() : "";
                curl.append(" \\\n  -u '").append(escapeString(username))
                    .append(":").append(escapeString(password)).append("'");
                break;

            case BEARER:
                String token = request.getAuth().getToken() != null ? request.getAuth().getToken() : "";
                curl.append(" \\\n  -H 'Authorization: Bearer ")
                    .append(escapeString(token)).append("'");
                break;

            case API_KEY:
                String headerName = request.getAuth().getApiKeyHeader() != null ?
                    request.getAuth().getApiKeyHeader() : "X-API-Key";
                String apiKey = request.getAuth().getApiKey() != null ? request.getAuth().getApiKey() : "";
                curl.append(" \\\n  -H '").append(escapeString(headerName))
                    .append(": ").append(escapeString(apiKey)).append("'");
                break;

            default:
                break;
        }
    }

    /**
     * Add body to cURL command
     */
    private static void addBodyToCurl(StringBuilder curl, Request request) {
        BodyType bodyType = request.getBody().getType();
        String content = request.getBody().getContent();

        if (content == null || content.isEmpty()) {
            return;
        }

        switch (bodyType) {
            case JSON:
                curl.append(" \\\n  -H 'Content-Type: application/json'");
                curl.append(" \\\n  -d '").append(escapeString(content)).append("'");
                break;

            case XML:
                curl.append(" \\\n  -H 'Content-Type: application/xml'");
                curl.append(" \\\n  -d '").append(escapeString(content)).append("'");
                break;

            case RAW:
                curl.append(" \\\n  -d '").append(escapeString(content)).append("'");
                break;

            case X_WWW_FORM_URLENCODED:
                curl.append(" \\\n  -H 'Content-Type: application/x-www-form-urlencoded'");
                curl.append(" \\\n  -d '").append(escapeString(content)).append("'");
                break;

            case FORM_DATA:
                curl.append(" \\\n  -H 'Content-Type: multipart/form-data'");
                // Parse form data
                String[] pairs = content.split("&");
                for (String pair : pairs) {
                    if (pair.trim().isEmpty()) continue;
                    String[] kv = pair.split("=", 2);
                    if (kv.length > 0) {
                        String key = kv[0];
                        String value = kv.length > 1 ? kv[1] : "";
                        curl.append(" \\\n  -F '").append(escapeString(key))
                            .append("=").append(escapeString(value)).append("'");
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * Escape single quotes in strings for shell safety
     */
    private static String escapeString(String str) {
        if (str == null) {
            return "";
        }
        // Replace single quote with '\'' (end quote, escaped quote, start quote)
        return str.replace("'", "'\\''");
    }
}
