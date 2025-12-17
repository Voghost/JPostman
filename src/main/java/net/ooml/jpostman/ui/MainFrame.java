package net.ooml.jpostman.ui;

import net.ooml.jpostman.config.AppConfig;
import net.ooml.jpostman.config.Constants;
import net.ooml.jpostman.service.http.HttpClientService;
import net.ooml.jpostman.service.storage.StorageService;
import net.ooml.jpostman.service.variable.EnvironmentService;
import net.ooml.jpostman.ui.components.left.RequestListPanel;
import net.ooml.jpostman.ui.components.right.RequestEditorPanel;
import net.ooml.jpostman.ui.components.right.TabbedRequestPanel;
import net.ooml.jpostman.ui.i18n.I18nManager;
import net.ooml.jpostman.ui.theme.ThemeManager;
import net.ooml.jpostman.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

/**
 * Main Frame - Main application window
 */
public class MainFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

    // Services
    private final StorageService storageService;
    private final EnvironmentService environmentService;
    private HttpClientService httpClientService;
    private final AppConfig appConfig;

    // UI Components
    private JSplitPane mainSplitPane;
    private RequestListPanel leftPanel;
    private TabbedRequestPanel rightPanel;
    private JLabel statusLabel;

    public MainFrame() throws IOException {
        // Initialize services
        this.storageService = StorageService.getInstance();
        this.environmentService = new EnvironmentService(storageService);
        this.appConfig = AppConfig.load();

        // Initialize environment service with current project
        String currentProject = appConfig.getCurrentProject();
        environmentService.initialize(currentProject);

        // Create HTTP client with variable resolver
        this.httpClientService = new HttpClientService(
                environmentService.createVariableResolver(),
                Constants.DEFAULT_TIMEOUT_MS
        );

        // Setup UI
        initializeUI();

        log.info("MainFrame initialized");
    }

    /**
     * Initialize UI
     */
    private void initializeUI() {
        // Set frame properties
        setTitle(I18nManager.get("app.name") + " - " + appConfig.getCurrentProject());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        // Set frame size
        setSize(appConfig.getWindowWidth(), appConfig.getWindowHeight());
        setLocationRelativeTo(null);

        // Set application icon
        setIconImages(loadApplicationIcons());

        // Create menu bar
        MenuBarFactory menuBarFactory = new MenuBarFactory(this);
        setJMenuBar(menuBarFactory.createMenuBar());

        // Create main panel
        createMainPanel();

        // Create status bar
        createStatusBar();

        // macOS specific settings
        if (OSUtil.isMacOS()) {
            setupMacOS();
        }

        log.debug("UI initialized");
    }

    /**
     * Create main panel with split pane
     */
    private void createMainPanel() {
        // Create left panel (request list)
        leftPanel = new RequestListPanel(this);

        // Create right panel (tabbed request panel)
        rightPanel = new TabbedRequestPanel(this);

        // Create split pane
        mainSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setResizeWeight(0.2);

        // Add to frame
        add(mainSplitPane, BorderLayout.CENTER);
    }

    /**
     * Create status bar
     */
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        statusLabel = new JLabel(I18nManager.get("status.ready"));
        statusBar.add(statusLabel, BorderLayout.WEST);

        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Load application icons in multiple sizes
     */
    private java.util.List<Image> loadApplicationIcons() {
        java.util.List<Image> icons = new java.util.ArrayList<>();
        int[] sizes = {16, 32, 48, 64, 128, 256};

        for (int size : sizes) {
            try {
                String iconPath = "/icons/jpostman_" + size + ".png";
                java.net.URL iconURL = getClass().getResource(iconPath);
                if (iconURL != null) {
                    ImageIcon icon = new ImageIcon(iconURL);
                    icons.add(icon.getImage());
                    log.debug("Loaded icon: {}", iconPath);
                } else {
                    log.warn("Icon not found: {}", iconPath);
                }
            } catch (Exception e) {
                log.error("Failed to load icon of size {}", size, e);
            }
        }

        if (icons.isEmpty()) {
            log.warn("No application icons loaded");
        } else {
            log.info("Loaded {} application icons", icons.size());
        }

        return icons;
    }

    /**
     * Setup macOS specific features
     */
    private void setupMacOS() {
        // Enable screen menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", I18nManager.get("app.name"));

        // Set dock icon
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            // Load the largest icon (256x256) for dock
            String iconPath = "/icons/jpostman_256.png";
            java.net.URL iconURL = getClass().getResource(iconPath);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                taskbar.setIconImage(icon.getImage());
                log.info("Dock icon set successfully");
            } else {
                log.warn("Dock icon not found: {}", iconPath);
            }
        } catch (Exception e) {
            log.debug("Failed to set dock icon: {}", e.getMessage());
        }
    }

    /**
     * Update status message
     */
    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            // Update window title
            setTitle(I18nManager.get("app.name") + " - " + appConfig.getCurrentProject());

            // Recreate menu bar
            MenuBarFactory menuBarFactory = new MenuBarFactory(this);
            setJMenuBar(menuBarFactory.createMenuBar());

            // Update status bar
            statusLabel.setText(I18nManager.get("status.ready"));

            // Refresh left panel
            if (leftPanel != null) {
                leftPanel.refreshUI();
            }

            // Refresh right panel (tabs)
            if (rightPanel != null) {
                rightPanel.refreshUI();
            }

            // Repaint and revalidate
            revalidate();
            repaint();

            log.info("UI refreshed after language change");
        });
    }

    /**
     * Toggle sidebar visibility
     */
    public void toggleSidebar(boolean visible) {
        leftPanel.setVisible(visible);
        mainSplitPane.setDividerLocation(visible ? 300 : 0);
    }

    // ==================== Menu Action Handlers ====================

    public void onNewRequest() {
        log.info("New request action");
        rightPanel.openNewRequestTab();
    }

    public void onNewCollection() {
        log.info("New collection action");
        leftPanel.createNewCollection();
    }

    public void onSave() {
        log.info("Save action");
        try {
            RequestEditorPanel currentEditor = rightPanel.getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.saveCurrentRequest();
                setStatus(I18nManager.get("status.saved"));
            }
        } catch (Exception e) {
            log.error("Failed to save", e);
            showError("Failed to save: " + e.getMessage());
        }
    }

    public void onImport() {
        log.info("Import action");
        JOptionPane.showMessageDialog(this,
                "Import feature coming soon",
                I18nManager.get("menu.file.import"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onExport() {
        log.info("Export action");
        JOptionPane.showMessageDialog(this,
                "Export feature coming soon",
                I18nManager.get("menu.file.export"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onExit() {
        log.info("Exit action");
        try {
            // Save window state
            appConfig.setWindowWidth(getWidth());
            appConfig.setWindowHeight(getHeight());
            appConfig.save();

            // Shutdown services
            httpClientService.shutdown();

            log.info("Application exiting");
            System.exit(0);
        } catch (Exception e) {
            log.error("Error during exit", e);
            System.exit(1);
        }
    }

    public void onDelete() {
        log.info("Delete action");
        leftPanel.deleteSelectedItem();
    }

    public void onSendRequest() {
        log.info("Send request action");
        RequestEditorPanel currentEditor = rightPanel.getCurrentEditor();
        if (currentEditor != null) {
            currentEditor.sendRequest();
        }
    }

    public void onDuplicateRequest() {
        log.info("Duplicate request action");
        RequestEditorPanel currentEditor = rightPanel.getCurrentEditor();
        if (currentEditor != null) {
            currentEditor.duplicateCurrentRequest();
        }
    }

    public void onManageEnvironment() {
        log.info("Manage environment action");
        JOptionPane.showMessageDialog(this,
                "Environment management coming soon",
                I18nManager.get("menu.tools.environment"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onManageVariables() {
        log.info("Manage variables action");
        JOptionPane.showMessageDialog(this,
                "Variable management coming soon",
                I18nManager.get("menu.tools.variables"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onSettings() {
        log.info("Settings action");
        JOptionPane.showMessageDialog(this,
                "Settings dialog coming soon",
                I18nManager.get("menu.tools.settings"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onDocumentation() {
        log.info("Documentation action");
        JOptionPane.showMessageDialog(this,
                "Documentation coming soon",
                I18nManager.get("menu.help.documentation"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onAbout() {
        log.info("About action");

        // Create HTML content with clickable links
        String htmlMessage = "<html><body style='width: 300px; padding: 10px;'>" +
                "<h2 style='margin: 0;'>" + I18nManager.get("app.name") + "</h2>" +
                "<p style='margin: 5px 0;'>" + I18nManager.get("app.version") + ": " + Constants.APP_VERSION + "</p>" +
                "<p style='margin: 5px 0;'>" + I18nManager.get("app.description") + "</p>" +
                "<br>" +
                "<p style='margin: 5px 0;'><b>" + I18nManager.get("about.author") + ":</b> LeifLiu</p>" +
                "<p style='margin: 5px 0;'><b>" + I18nManager.get("about.email") + ":</b> " +
                "<a href='mailto:voghost2@gmail.com'>voghost2@gmail.com</a></p>" +
                "<p style='margin: 5px 0;'><b>" + I18nManager.get("about.github") + ":</b> " +
                "<a href='http://github.com/voghost'>http://github.com/voghost</a></p>" +
                "</body></html>";

        // Create JEditorPane with HTML content
        JEditorPane editorPane = new JEditorPane("text/html", htmlMessage);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        // Add hyperlink listener to open links in browser
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                } catch (Exception ex) {
                    log.error("Failed to open link: " + e.getURL(), ex);
                }
            }
        });

        JOptionPane.showMessageDialog(this,
                editorPane,
                I18nManager.get("menu.help.about"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show error dialog
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                I18nManager.get("error.title"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show info dialog
     */
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                I18nManager.get("info.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== Getters ====================

    public StorageService getStorageService() {
        return storageService;
    }

    public EnvironmentService getEnvironmentService() {
        return environmentService;
    }

    public HttpClientService getHttpClientService() {
        return httpClientService;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public String getCurrentProject() {
        return appConfig.getCurrentProject();
    }

    public TabbedRequestPanel getRightPanel() {
        return rightPanel;
    }

    public RequestListPanel getLeftPanel() {
        return leftPanel;
    }

    // ==================== Project Management ====================

    /**
     * Switch to a different project
     * Orchestrates reloading of all project-specific components
     * @param newProjectName Name of project to switch to
     */
    public void switchProject(String newProjectName) {
        String currentProject = appConfig.getCurrentProject();

        // No-op if switching to same project
        if (currentProject.equals(newProjectName)) {
            log.debug("Already on project: {}", newProjectName);
            return;
        }

        try {
            // Verify project exists
            if (!storageService.projectExists(newProjectName)) {
                showError(I18nManager.get("error.project_not_found") + ": " + newProjectName);
                return;
            }

            log.info("Switching from project '{}' to '{}'", currentProject, newProjectName);
            setStatus(I18nManager.get("status.switching_project"));

            // Close all open tabs
            rightPanel.closeAllTabs();

            // Update app config and save
            appConfig.setCurrentProject(newProjectName);
            appConfig.save();

            // Reinitialize environment service
            environmentService.initialize(newProjectName);

            // Recreate HTTP client with new variable resolver
            httpClientService.shutdown();
            httpClientService = new HttpClientService(
                    environmentService.createVariableResolver(),
                    Constants.DEFAULT_TIMEOUT_MS
            );

            // Reload collections in left panel
            leftPanel.loadCollections();

            // Update project switcher dropdown
            leftPanel.getProjectSwitcher().refresh();

            // Update window title
            setTitle(I18nManager.get("app.name") + " - " + newProjectName);

            // Update status
            setStatus(I18nManager.get("status.project_switched") + ": " + newProjectName);

            log.info("Successfully switched to project: {}", newProjectName);

        } catch (IOException e) {
            log.error("Failed to switch project", e);
            showError(I18nManager.get("error.switch_project_failed") + ": " + e.getMessage());

            // Attempt to revert to original project
            try {
                appConfig.setCurrentProject(currentProject);
                appConfig.save();
            } catch (IOException ex) {
                log.error("Failed to revert project", ex);
            }
        }
    }

    /**
     * Handler for create new project action
     */
    public void onNewProject() {
        log.info("New project action");

        String projectName = JOptionPane.showInputDialog(
                this,
                I18nManager.get("dialog.new_project.message"),
                I18nManager.get("dialog.new_project.title"),
                JOptionPane.QUESTION_MESSAGE
        );

        if (projectName == null || projectName.trim().isEmpty()) {
            return; // User cancelled or entered empty name
        }

        projectName = projectName.trim();

        // Validate project name
        if (!storageService.isValidProjectName(projectName)) {
            showError(I18nManager.get("error.invalid_project_name"));
            return;
        }

        // Check if project already exists
        if (storageService.projectExists(projectName)) {
            showError(I18nManager.get("error.project_already_exists") + ": " + projectName);
            return;
        }

        try {
            // Create project
            storageService.createProject(projectName);

            // Confirm switch
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    I18nManager.get("dialog.new_project.switch_confirm") + " '" + projectName + "'?",
                    I18nManager.get("dialog.new_project.title"),
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                switchProject(projectName);
            } else {
                // Just refresh the project list
                leftPanel.getProjectSwitcher().refresh();
            }

        } catch (IOException e) {
            log.error("Failed to create project", e);
            showError(I18nManager.get("error.create_project_failed") + ": " + e.getMessage());
        }
    }

    /**
     * Handler for switch project action
     */
    public void onSwitchProject() {
        log.info("Switch project action");

        try {
            List<String> projects = storageService.listAllProjects();

            if (projects.isEmpty()) {
                showInfo(I18nManager.get("info.no_projects"));
                return;
            }

            // Create combo box with projects
            String currentProject = appConfig.getCurrentProject();
            String[] projectArray = projects.toArray(new String[0]);

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    I18nManager.get("dialog.switch_project.message"),
                    I18nManager.get("dialog.switch_project.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    projectArray,
                    currentProject
            );

            if (selected != null && !selected.equals(currentProject)) {
                switchProject(selected);
            }

        } catch (IOException e) {
            log.error("Failed to list projects", e);
            showError(I18nManager.get("error.list_projects_failed") + ": " + e.getMessage());
        }
    }

    /**
     * Handler for delete project action
     */
    public void onDeleteProject() {
        log.info("Delete project action");

        try {
            List<String> projects = storageService.listAllProjects();

            if (projects.isEmpty()) {
                showInfo(I18nManager.get("info.no_projects"));
                return;
            }

            // Cannot delete if only one project exists
            if (projects.size() == 1) {
                showError(I18nManager.get("error.cannot_delete_only_project"));
                return;
            }

            String currentProject = appConfig.getCurrentProject();

            // Create combo box with projects
            String[] projectArray = projects.toArray(new String[0]);

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    I18nManager.get("dialog.delete_project.message"),
                    I18nManager.get("dialog.delete_project.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    projectArray,
                    null
            );

            if (selected == null) {
                return; // User cancelled
            }

            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    I18nManager.get("dialog.delete_project.confirm") + " '" + selected + "'?\n" +
                            I18nManager.get("dialog.delete_project.warning"),
                    I18nManager.get("dialog.delete_project.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Delete project
            storageService.deleteProject(selected);

            // If deleting current project, switch to another one
            if (selected.equals(currentProject)) {
                projects.remove(selected);
                if (!projects.isEmpty()) {
                    String newProject = projects.get(0);
                    switchProject(newProject);
                }
            } else {
                // Just refresh project list
                leftPanel.getProjectSwitcher().refresh();
            }

            showInfo(I18nManager.get("info.project_deleted") + ": " + selected);

        } catch (IOException e) {
            log.error("Failed to delete project", e);
            showError(I18nManager.get("error.delete_project_failed") + ": " + e.getMessage());
        }
    }

    /**
     * Handler for rename project action
     */
    public void onRenameProject() {
        log.info("Rename project action");

        try {
            List<String> projects = storageService.listAllProjects();

            if (projects.isEmpty()) {
                showInfo(I18nManager.get("info.no_projects"));
                return;
            }

            String currentProject = appConfig.getCurrentProject();
            String[] projectArray = projects.toArray(new String[0]);

            // Select project to rename
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    I18nManager.get("dialog.rename_project.select_message"),
                    I18nManager.get("dialog.rename_project.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    projectArray,
                    currentProject
            );

            if (selected == null) {
                return; // User cancelled
            }

            // Ask for new name
            String newName = JOptionPane.showInputDialog(
                    this,
                    I18nManager.get("dialog.rename_project.new_name_message"),
                    I18nManager.get("dialog.rename_project.title"),
                    JOptionPane.QUESTION_MESSAGE
            );

            if (newName == null || newName.trim().isEmpty()) {
                return;
            }

            newName = newName.trim();

            // Validate new name
            if (!storageService.isValidProjectName(newName)) {
                showError(I18nManager.get("error.invalid_project_name"));
                return;
            }

            // Check if name already exists
            if (storageService.projectExists(newName)) {
                showError(I18nManager.get("error.project_already_exists") + ": " + newName);
                return;
            }

            // Rename project
            storageService.renameProject(selected, newName);

            // If renaming current project, update and reload
            if (selected.equals(currentProject)) {
                appConfig.setCurrentProject(newName);
                appConfig.save();

                // Update UI
                setTitle(I18nManager.get("app.name") + " - " + newName);
                leftPanel.getProjectSwitcher().refresh();
            } else {
                // Just refresh project list
                leftPanel.getProjectSwitcher().refresh();
            }

            showInfo(I18nManager.get("info.project_renamed") + ": " + selected + " → " + newName);

        } catch (IOException e) {
            log.error("Failed to rename project", e);
            showError(I18nManager.get("error.rename_project_failed") + ": " + e.getMessage());
        }
    }
}
