package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ooml.jpostman.model.enums.HttpMethod;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request history entry model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryEntry {

    private String id;
    private String requestId; // Reference to the original request (optional)
    private String collectionId; // Reference to the collection (optional)
    private HttpMethod method;
    private String url;
    private Integer statusCode;
    private String statusText;
    private Long duration;
    private LocalDateTime timestamp;

    // Simplified request/response data
    private Request request;
    private Response response;

    /**
     * Create a new history entry
     */
    public static HistoryEntry createNew(Request request, Response response) {
        return HistoryEntry.builder()
                .id(UUID.randomUUID().toString())
                .requestId(request.getId())
                .collectionId(request.getCollectionId())
                .method(request.getMethod())
                .url(request.getUrl())
                .statusCode(response.getStatusCode())
                .statusText(response.getStatusText())
                .duration(response.getDuration())
                .timestamp(LocalDateTime.now())
                .request(request)
                .response(response)
                .build();
    }
}
