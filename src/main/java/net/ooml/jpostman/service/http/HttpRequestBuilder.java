package net.ooml.jpostman.service.http;

import net.ooml.jpostman.config.Constants;
import net.ooml.jpostman.model.*;
import net.ooml.jpostman.model.enums.AuthType;
import net.ooml.jpostman.model.enums.BodyType;
import net.ooml.jpostman.service.variable.VariableResolver;
import net.ooml.jpostman.util.StringUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP request builder using OkHttp
 * Builds OkHttp Request from JPostman Request model
 */
public class HttpRequestBuilder {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestBuilder.class);

    private final VariableResolver variableResolver;

    public HttpRequestBuilder(VariableResolver variableResolver) {
        this.variableResolver = variableResolver;
    }

    /**
     * Build OkHttp Request from JPostman Request
     */
    public okhttp3.Request build(net.ooml.jpostman.model.Request request) throws IllegalArgumentException {
        // Resolve URL
        String url = resolveUrl(request.getUrl());
        if (StringUtil.isEmpty(url)) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        // Build request
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(url);

        // Add headers
        addHeaders(builder, request);

        // Add authentication
        addAuthentication(builder, request.getAuth());

        // Build request body and set method
        buildRequestWithBody(builder, request);

        okhttp3.Request builtRequest = builder.build();
        log.debug("Built HTTP request: {} {}", request.getMethod(), url);
        return builtRequest;
    }

    /**
     * Resolve URL with variables
     */
    private String resolveUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return url;
        }
        return variableResolver.resolve(url);
    }

    /**
     * Add headers to request
     */
    private void addHeaders(okhttp3.Request.Builder builder, net.ooml.jpostman.model.Request request) {
        // Add default headers
        builder.addHeader("User-Agent", Constants.DEFAULT_USER_AGENT);

        // Add custom headers
        if (request.getHeaders() != null) {
            for (Header header : request.getHeaders()) {
                if (header.getEnabled() != null && header.getEnabled()) {
                    String key = resolveValue(header.getKey());
                    String value = resolveValue(header.getValue());
                    if (StringUtil.isNotEmpty(key) && value != null) {
                        builder.addHeader(key, value);
                    }
                }
            }
        }
    }

    /**
     * Add authentication to request
     */
    private void addAuthentication(okhttp3.Request.Builder builder, AuthConfig auth) {
        if (auth == null || auth.getType() == AuthType.NONE) {
            return;
        }

        switch (auth.getType()) {
            case BASIC:
                addBasicAuth(builder, auth);
                break;
            case BEARER:
                addBearerToken(builder, auth);
                break;
            case API_KEY:
                addApiKey(builder, auth);
                break;
            default:
                log.warn("Unsupported auth type: {}", auth.getType());
        }
    }

    /**
     * Add Basic Authentication
     */
    private void addBasicAuth(okhttp3.Request.Builder builder, AuthConfig auth) {
        String username = resolveValue(auth.getUsername());
        String password = resolveValue(auth.getPassword());

        if (StringUtil.isNotEmpty(username)) {
            String credentials = username + ":" + (password != null ? password : "");
            String encoded = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8));
            builder.addHeader("Authorization", "Basic " + encoded);
            log.debug("Added Basic Auth for user: {}", username);
        }
    }

    /**
     * Add Bearer Token
     */
    private void addBearerToken(okhttp3.Request.Builder builder, AuthConfig auth) {
        String token = resolveValue(auth.getToken());
        if (StringUtil.isNotEmpty(token)) {
            builder.addHeader("Authorization", "Bearer " + token);
            log.debug("Added Bearer Token");
        }
    }

    /**
     * Add API Key
     */
    private void addApiKey(okhttp3.Request.Builder builder, AuthConfig auth) {
        String apiKey = resolveValue(auth.getApiKey());
        String headerName = resolveValue(auth.getApiKeyHeader());

        if (StringUtil.isNotEmpty(apiKey) && StringUtil.isNotEmpty(headerName)) {
            builder.addHeader(headerName, apiKey);
            log.debug("Added API Key with header: {}", headerName);
        }
    }

    /**
     * Build request with body based on HTTP method
     */
    private void buildRequestWithBody(okhttp3.Request.Builder builder, net.ooml.jpostman.model.Request request) {
        String method = request.getMethod().getValue();
        okhttp3.RequestBody body = null;

        // Methods that typically don't have a body
        if ("GET".equals(method) || "HEAD".equals(method) || "DELETE".equals(method)) {
            // For GET and HEAD, body should be null
            // For DELETE, body is optional but we'll use null by default
            builder.method(method, null);
            return;
        }

        // Build request body for POST, PUT, PATCH, etc.
        body = buildRequestBody(request.getBody());

        // If no body provided for methods that typically require one, use empty body
        if (body == null && ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            body = okhttp3.RequestBody.create("", null);
        }

        builder.method(method, body);
    }

    /**
     * Build OkHttp RequestBody from JPostman RequestBody
     */
    private okhttp3.RequestBody buildRequestBody(net.ooml.jpostman.model.RequestBody body) {
        if (body == null || body.getType() == BodyType.NONE) {
            return null;
        }

        String content = resolveValue(body.getContent());
        if (StringUtil.isEmpty(content)) {
            return null;
        }

        MediaType mediaType = getMediaType(body.getType());
        return okhttp3.RequestBody.create(content, mediaType);
    }

    /**
     * Get MediaType for body type
     */
    private MediaType getMediaType(BodyType bodyType) {
        switch (bodyType) {
            case JSON:
                return MediaType.parse(Constants.CONTENT_TYPE_JSON + "; charset=utf-8");
            case XML:
                return MediaType.parse(Constants.CONTENT_TYPE_XML + "; charset=utf-8");
            case FORM_DATA:
                return MediaType.parse(Constants.CONTENT_TYPE_MULTIPART);
            case X_WWW_FORM_URLENCODED:
                return MediaType.parse(Constants.CONTENT_TYPE_FORM);
            case RAW:
                return MediaType.parse(Constants.CONTENT_TYPE_TEXT + "; charset=utf-8");
            default:
                return MediaType.parse(Constants.CONTENT_TYPE_TEXT + "; charset=utf-8");
        }
    }

    /**
     * Resolve value with variable resolver
     */
    private String resolveValue(String value) {
        if (value == null) {
            return null;
        }
        return variableResolver.resolve(value);
    }
}
