package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ooml.jpostman.model.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP response model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private Integer statusCode;
    private String statusText;

    @Builder.Default
    private RequestStatus status = RequestStatus.SUCCESS;

    @Builder.Default
    private List<Header> headers = new ArrayList<>();

    private String body;
    private Long size; // Response size in bytes
    private Long duration; // Response time in milliseconds
    private LocalDateTime timestamp;
    private String errorMessage;

    /**
     * Create success response
     */
    public static Response createSuccess(int statusCode, String statusText,
                                         List<Header> headers, String body,
                                         long duration) {
        return Response.builder()
                .statusCode(statusCode)
                .statusText(statusText)
                .status(RequestStatus.SUCCESS)
                .headers(headers != null ? headers : new ArrayList<>())
                .body(body)
                .size(body != null ? (long) body.length() : 0L)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response
     */
    public static Response createError(String errorMessage) {
        return Response.builder()
                .status(RequestStatus.ERROR)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create timeout response
     */
    public static Response createTimeout() {
        return Response.builder()
                .status(RequestStatus.TIMEOUT)
                .errorMessage("Request timeout")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Check if response is successful (2xx status code)
     */
    public boolean isSuccessful() {
        return statusCode != null && statusCode >= 200 && statusCode < 300;
    }
}
