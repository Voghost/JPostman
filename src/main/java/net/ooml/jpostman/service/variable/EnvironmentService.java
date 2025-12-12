package net.ooml.jpostman.service.variable;

import net.ooml.jpostman.model.Environment;
import net.ooml.jpostman.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Environment service for managing environments
 */
public class EnvironmentService {
    private static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);

    private final StorageService storageService;
    private String currentProjectName;
    private Environment currentEnvironment;
    private Environment globalVariables;

    public EnvironmentService(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Initialize for a project
     */
    public void initialize(String projectName) throws IOException {
        this.currentProjectName = projectName;
        // Load global variables
        this.globalVariables = storageService.loadGlobals(projectName);
        log.info("EnvironmentService initialized for project: {}", projectName);
    }

    /**
     * Set current environment
     */
    public void setCurrentEnvironment(String environmentName) throws IOException {
        if (environmentName == null) {
            this.currentEnvironment = null;
            log.info("Current environment cleared");
            return;
        }

        this.currentEnvironment = storageService.loadEnvironment(currentProjectName, environmentName);
        log.info("Current environment set to: {}", environmentName);
    }

    /**
     * Get current environment
     */
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }

    /**
     * Get global variables
     */
    public Environment getGlobalVariables() {
        return globalVariables;
    }

    /**
     * Save current environment
     */
    public void saveCurrentEnvironment() throws IOException {
        if (currentEnvironment != null) {
            storageService.saveEnvironment(currentProjectName, currentEnvironment);
            log.debug("Current environment saved");
        }
    }

    /**
     * Save global variables
     */
    public void saveGlobalVariables() throws IOException {
        if (globalVariables != null) {
            storageService.saveGlobals(currentProjectName, globalVariables);
            log.debug("Global variables saved");
        }
    }

    /**
     * Load all environments for current project
     */
    public List<Environment> loadAllEnvironments() throws IOException {
        return storageService.loadAllEnvironments(currentProjectName);
    }

    /**
     * Create new environment
     */
    public Environment createEnvironment(String name) throws IOException {
        Environment env = Environment.createNew(name);
        storageService.saveEnvironment(currentProjectName, env);
        log.info("New environment created: {}", name);
        return env;
    }

    /**
     * Delete environment
     */
    public boolean deleteEnvironment(String environmentName) throws IOException {
        boolean deleted = storageService.deleteEnvironment(currentProjectName, environmentName);
        if (deleted && currentEnvironment != null &&
                environmentName.equals(currentEnvironment.getName())) {
            currentEnvironment = null;
        }
        log.info("Environment deleted: {}", environmentName);
        return deleted;
    }

    /**
     * Create a variable resolver with current environment and global variables
     */
    public VariableResolver createVariableResolver() {
        VariableResolver resolver = new VariableResolver();
        resolver.setGlobalVariables(globalVariables);
        if (currentEnvironment != null) {
            resolver.setEnvironmentVariables(currentEnvironment);
        }
        return resolver;
    }
}
