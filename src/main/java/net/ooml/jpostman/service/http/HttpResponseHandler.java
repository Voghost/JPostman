package net.ooml.jpostman.service.http;

import net.ooml.jpostman.model.Header;
import net.ooml.jpostman.model.Response;
import net.ooml.jpostman.model.enums.RequestStatus;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP response handler
 * Converts OkHttp Response to JPostman Response
 */
public class HttpResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);

    private static final long MAX_BODY_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Handle successful OkHttp response
     */
    public Response handleResponse(okhttp3.Response okHttpResponse, long duration) {
        try {
            int statusCode = okHttpResponse.code();
            String statusMessage = okHttpResponse.message();

            // Parse headers
            List<Header> headers = parseHeaders(okHttpResponse.headers());

            // Read body
            String body = readBody(okHttpResponse.body());

            // Calculate size
            long size = body != null ? body.length() : 0;

            Response response = Response.builder()
                    .statusCode(statusCode)
                    .statusText(statusMessage)
                    .status(RequestStatus.SUCCESS)
                    .headers(headers)
                    .body(body)
                    .size(size)
                    .duration(duration)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.debug("Response handled: {} {} ({}ms)", statusCode, statusMessage, duration);
            return response;

        } catch (Exception e) {
            log.error("Failed to handle response", e);
            return Response.createError("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * Handle error response
     */
    public Response handleError(Exception exception) {
        String errorMessage = exception.getMessage();
        log.error("Request failed: {}", errorMessage);

        if (exception instanceof java.net.SocketTimeoutException) {
            return Response.createTimeout();
        }

        return Response.createError(errorMessage != null ? errorMessage : "Unknown error occurred");
    }

    /**
     * Parse OkHttp headers to JPostman headers
     */
    private List<Header> parseHeaders(Headers okHttpHeaders) {
        List<Header> headers = new ArrayList<>();

        for (String name : okHttpHeaders.names()) {
            for (String value : okHttpHeaders.values(name)) {
                Header header = Header.builder()
                        .key(name)
                        .value(value)
                        .enabled(true)
                        .build();
                headers.add(header);
            }
        }

        return headers;
    }

    /**
     * Read response body
     */
    private String readBody(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        }

        // Check body size
        long contentLength = responseBody.contentLength();
        if (contentLength > MAX_BODY_SIZE) {
            log.warn("Response body too large ({} bytes), truncating", contentLength);
            return "[Response body too large to display]";
        }

        try {
            String body = responseBody.string();
            log.debug("Response body read: {} bytes", body.length());
            return body;
        } finally {
            responseBody.close();
        }
    }

    /**
     * Create a minimal response for display purposes
     */
    public Response createMinimalResponse(int statusCode, String statusText, String body) {
        return Response.builder()
                .statusCode(statusCode)
                .statusText(statusText)
                .status(RequestStatus.SUCCESS)
                .headers(new ArrayList<>())
                .body(body)
                .size(body != null ? (long) body.length() : 0L)
                .duration(0L)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
