package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Collection model for organizing requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Collection {

    private String id;
    private String name;
    private String description;

    @Builder.Default
    private List<Folder> folders = new ArrayList<>();

    @Builder.Default
    private List<Request> requests = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Folder model for organizing requests within collection
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Folder {
        private String id;
        private String name;
        private String description;

        public static Folder createNew(String name) {
            return Folder.builder()
                    .id(UUID.randomUUID().toString())
                    .name(name)
                    .build();
        }
    }

    /**
     * Create a new collection
     */
    public static Collection createNew(String name) {
        LocalDateTime now = LocalDateTime.now();
        return Collection.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .folders(new ArrayList<>())
                .requests(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Add a request to the collection
     */
    public void addRequest(Request request) {
        request.setCollectionId(this.id);
        requests.add(request);
        touch();
    }

    /**
     * Remove a request from the collection
     */
    public boolean removeRequest(String requestId) {
        boolean removed = requests.removeIf(r -> requestId.equals(r.getId()));
        if (removed) {
            touch();
        }
        return removed;
    }

    /**
     * Add a folder to the collection
     */
    public void addFolder(Folder folder) {
        folders.add(folder);
        touch();
    }

    /**
     * Remove a folder from the collection
     */
    public boolean removeFolder(String folderId) {
        boolean removed = folders.removeIf(f -> folderId.equals(f.getId()));
        if (removed) {
            // Also remove requests in this folder
            requests.removeIf(r -> folderId.equals(r.getFolderId()));
            touch();
        }
        return removed;
    }

    /**
     * Get folder by ID
     */
    public Folder getFolder(String folderId) {
        return folders.stream()
                .filter(f -> folderId.equals(f.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Update the modified timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
