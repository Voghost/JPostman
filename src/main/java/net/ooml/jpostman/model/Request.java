package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ooml.jpostman.model.enums.HttpMethod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HTTP request model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {

    private String id;
    private String name;
    private String collectionId;
    private String folderId; // Parent folder ID (optional)

    @Builder.Default
    private HttpMethod method = HttpMethod.GET;

    private String url;

    @Builder.Default
    private List<Header> headers = new ArrayList<>();

    @Builder.Default
    private List<Header> queryParams = new ArrayList<>();

    @Builder.Default
    private AuthConfig auth = AuthConfig.createNone();

    @Builder.Default
    private RequestBody body = RequestBody.createEmpty();

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Create a new request with generated ID
     */
    public static Request createNew(String name, HttpMethod method) {
        LocalDateTime now = LocalDateTime.now();
        return Request.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .method(method)
                .url("")
                .headers(new ArrayList<>())
                .queryParams(new ArrayList<>())
                .auth(AuthConfig.createNone())
                .body(RequestBody.createEmpty())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Create a copy of this request
     */
    public Request copy() {
        LocalDateTime now = LocalDateTime.now();
        return Request.builder()
                .id(UUID.randomUUID().toString())
                .name(this.name + " (Copy)")
                .collectionId(this.collectionId)
                .folderId(this.folderId)
                .method(this.method)
                .url(this.url)
                .headers(new ArrayList<>(this.headers))
                .queryParams(new ArrayList<>(this.queryParams))
                .auth(this.auth)
                .body(this.body)
                .description(this.description)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Update the modified timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
