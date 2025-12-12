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

/**
 * Main Frame - Main application window
 */
public class MainFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

    // Services
    private final StorageService storageService;
    private final EnvironmentService environmentService;
    private final HttpClientService httpClientService;
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
     * Setup macOS specific features
     */
    private void setupMacOS() {
        // Enable screen menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", I18nManager.get("app.name"));

        // Set dock icon (if available)
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            // Could set dock icon here if we have one
        } catch (Exception e) {
            log.debug("Taskbar not supported on this platform");
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
        String message = I18nManager.get("app.name") + "\n" +
                I18nManager.get("app.version") + ": " + Constants.APP_VERSION + "\n" +
                I18nManager.get("app.description") + "\n\n" +
                I18nManager.get("about.author") + ": LeifLiu\n" +
                I18nManager.get("about.email") + ": voghost2@gmail.com\n" +
                I18nManager.get("about.github") + ": http://github.com/voghost";

        JOptionPane.showMessageDialog(this,
                message,
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
}
