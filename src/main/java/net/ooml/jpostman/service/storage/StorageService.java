package net.ooml.jpostman.service.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ooml.jpostman.config.PathConfig;
import net.ooml.jpostman.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage service for persisting and loading data
 * Singleton pattern for managing file I/O operations
 */
public class StorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private static StorageService instance;
    private final ObjectMapper objectMapper;

    private StorageService() {
        this.objectMapper = JsonSerializer.getObjectMapper();
    }

    /**
     * Get singleton instance
     */
    public static StorageService getInstance() {
        if (instance == null) {
            synchronized (StorageService.class) {
                if (instance == null) {
                    instance = new StorageService();
                }
            }
        }
        return instance;
    }

    // ===== Project Operations =====

    /**
     * Load project
     */
    public Project loadProject(String projectName) throws IOException {
        Path projectFile = PathConfig.getProjectFile(projectName);
        if (!Files.exists(projectFile)) {
            throw new IOException("Project file not found: " + projectFile);
        }
        return objectMapper.readValue(projectFile.toFile(), Project.class);
    }

    /**
     * Save project
     */
    public void saveProject(String projectName, Project project) throws IOException {
        Path projectFile = PathConfig.getProjectFile(projectName);
        Files.createDirectories(projectFile.getParent());
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(projectFile.toFile(), project);
        log.debug("Project saved: {}", projectName);
    }

    /**
     * Create new project
     */
    public void createProject(String projectName) throws IOException {
        Project project = Project.createNew(projectName);
        saveProject(projectName, project);

        // Create project directories
        Files.createDirectories(PathConfig.getCollectionsDirectory(projectName));
        Files.createDirectories(PathConfig.getEnvironmentsDirectory(projectName));

        log.info("New project created: {}", projectName);
    }

    /**
     * Check if project exists
     */
    public boolean projectExists(String projectName) {
        return Files.exists(PathConfig.getProjectFile(projectName));
    }

    // ===== Collection Operations =====

    /**
     * Load collection
     */
    public Collection loadCollection(String projectName, String collectionId) throws IOException {
        Path collectionFile = PathConfig.getCollectionsDirectory(projectName)
                .resolve(collectionId + ".json");
        if (!Files.exists(collectionFile)) {
            throw new IOException("Collection file not found: " + collectionFile);
        }
        return objectMapper.readValue(collectionFile.toFile(), Collection.class);
    }

    /**
     * Save collection
     */
    public void saveCollection(String projectName, Collection collection) throws IOException {
        Path collectionsDir = PathConfig.getCollectionsDirectory(projectName);
        Files.createDirectories(collectionsDir);

        Path collectionFile = collectionsDir.resolve(collection.getId() + ".json");
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(collectionFile.toFile(), collection);
        log.debug("Collection saved: {}", collection.getName());
    }

    /**
     * Delete collection
     */
    public boolean deleteCollection(String projectName, String collectionId) throws IOException {
        Path collectionFile = PathConfig.getCollectionsDirectory(projectName)
                .resolve(collectionId + ".json");
        return Files.deleteIfExists(collectionFile);
    }

    /**
     * Load all collections for a project
     */
    public List<Collection> loadAllCollections(String projectName) throws IOException {
        List<Collection> collections = new ArrayList<>();
        Path collectionsDir = PathConfig.getCollectionsDirectory(projectName);

        if (!Files.exists(collectionsDir)) {
            return collections;
        }

        Files.list(collectionsDir)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Collection collection = objectMapper.readValue(path.toFile(), Collection.class);
                        collections.add(collection);
                    } catch (IOException e) {
                        log.error("Failed to load collection: {}", path, e);
                    }
                });

        return collections;
    }

    // ===== Environment Operations =====

    /**
     * Load environment
     */
    public Environment loadEnvironment(String projectName, String environmentName) throws IOException {
        Path envFile = PathConfig.getEnvironmentsDirectory(projectName)
                .resolve(environmentName + ".json");
        if (!Files.exists(envFile)) {
            throw new IOException("Environment file not found: " + envFile);
        }
        return objectMapper.readValue(envFile.toFile(), Environment.class);
    }

    /**
     * Save environment
     */
    public void saveEnvironment(String projectName, Environment environment) throws IOException {
        Path envDir = PathConfig.getEnvironmentsDirectory(projectName);
        Files.createDirectories(envDir);

        Path envFile = envDir.resolve(environment.getName() + ".json");
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(envFile.toFile(), environment);
        log.debug("Environment saved: {}", environment.getName());
    }

    /**
     * Delete environment
     */
    public boolean deleteEnvironment(String projectName, String environmentName) throws IOException {
        Path envFile = PathConfig.getEnvironmentsDirectory(projectName)
                .resolve(environmentName + ".json");
        return Files.deleteIfExists(envFile);
    }

    /**
     * Load all environments for a project
     */
    public List<Environment> loadAllEnvironments(String projectName) throws IOException {
        List<Environment> environments = new ArrayList<>();
        Path envDir = PathConfig.getEnvironmentsDirectory(projectName);

        if (!Files.exists(envDir)) {
            return environments;
        }

        Files.list(envDir)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Environment env = objectMapper.readValue(path.toFile(), Environment.class);
                        environments.add(env);
                    } catch (IOException e) {
                        log.error("Failed to load environment: {}", path, e);
                    }
                });

        return environments;
    }

    // ===== Global Variables Operations =====

    /**
     * Load global variables
     */
    public Environment loadGlobals(String projectName) throws IOException {
        Path globalsFile = PathConfig.getGlobalsFile(projectName);
        if (!Files.exists(globalsFile)) {
            // Create empty globals
            Environment globals = Environment.builder()
                    .name("globals")
                    .variables(new ArrayList<>())
                    .build();
            saveGlobals(projectName, globals);
            return globals;
        }
        return objectMapper.readValue(globalsFile.toFile(), Environment.class);
    }

    /**
     * Save global variables
     */
    public void saveGlobals(String projectName, Environment globals) throws IOException {
        Path globalsFile = PathConfig.getGlobalsFile(projectName);
        Files.createDirectories(globalsFile.getParent());
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(globalsFile.toFile(), globals);
        log.debug("Global variables saved for project: {}", projectName);
    }

    // ===== History Operations =====

    /**
     * Load request history
     */
    public List<HistoryEntry> loadHistory(String projectName) throws IOException {
        Path historyFile = PathConfig.getHistoryFile(projectName);
        if (!Files.exists(historyFile)) {
            return new ArrayList<>();
        }

        HistoryData historyData = objectMapper.readValue(historyFile.toFile(), HistoryData.class);
        return historyData.getEntries();
    }

    /**
     * Save request history
     */
    public void saveHistory(String projectName, List<HistoryEntry> entries) throws IOException {
        Path historyFile = PathConfig.getHistoryFile(projectName);
        Files.createDirectories(historyFile.getParent());

        HistoryData historyData = new HistoryData();
        historyData.setMaxEntries(100);
        historyData.setEntries(entries);

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(historyFile.toFile(), historyData);
        log.debug("History saved for project: {}", projectName);
    }

    /**
     * History data wrapper class
     */
    private static class HistoryData {
        private int maxEntries;
        private List<HistoryEntry> entries;

        public int getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }

        public List<HistoryEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<HistoryEntry> entries) {
            this.entries = entries;
        }
    }
}
