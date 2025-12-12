package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.ui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabbed Request Panel - Manages multiple open requests in tabs
 */
public class TabbedRequestPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(TabbedRequestPanel.class);

    private final MainFrame mainFrame;
    private final JTabbedPane tabbedPane;
    private final EmptyStatePanel emptyStatePanel;
    private final Map<String, RequestEditorPanel> openTabs; // requestId -> panel

    public TabbedRequestPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.tabbedPane = new JTabbedPane();
        this.emptyStatePanel = new EmptyStatePanel(mainFrame);
        this.openTabs = new HashMap<>();

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        // Show empty state initially
        showEmptyState();
    }

    /**
     * Show empty state panel
     */
    private void showEmptyState() {
        removeAll();
        add(emptyStatePanel, BorderLayout.CENTER);
        revalidate();
        repaint();
        log.debug("Showing empty state");
    }

    /**
     * Show tabbed pane
     */
    private void showTabbedPane() {
        removeAll();
        add(tabbedPane, BorderLayout.CENTER);
        revalidate();
        repaint();
        log.debug("Showing tabbed pane");
    }

    /**
     * Open a request in a new tab or switch to existing tab
     */
    public void openRequest(Request request) {
        if (request == null) {
            return;
        }

        String requestId = request.getId();

        // Check if request is already open
        if (openTabs.containsKey(requestId)) {
            // Switch to existing tab
            RequestEditorPanel panel = openTabs.get(requestId);
            int index = tabbedPane.indexOfComponent(panel);
            if (index >= 0) {
                tabbedPane.setSelectedIndex(index);
                log.debug("Switched to existing tab for request: {}", request.getName());
                return;
            }
        }

        // If first tab, show tabbed pane
        if (openTabs.isEmpty()) {
            showTabbedPane();
        }

        // Create new tab
        RequestEditorPanel editorPanel = new RequestEditorPanel(mainFrame);
        editorPanel.loadRequest(request);

        openTabs.put(requestId, editorPanel);

        // Add tab with close button
        tabbedPane.addTab(request.getName(), editorPanel);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(index, createTabComponent(request.getName(), requestId));
        tabbedPane.setSelectedIndex(index);

        log.info("Opened request in new tab: {}", request.getName());
    }

    /**
     * Open a new request tab
     */
    public void openNewRequestTab() {
        // If first tab, show tabbed pane
        if (openTabs.isEmpty()) {
            showTabbedPane();
        }

        RequestEditorPanel editorPanel = new RequestEditorPanel(mainFrame);

        // Create a temporary request for the tab
        Request newRequest = editorPanel.getCurrentRequest();
        String tempId = newRequest.getId();

        openTabs.put(tempId, editorPanel);

        // Add tab with close button
        tabbedPane.addTab("New Request", editorPanel);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(index, createTabComponent("New Request", tempId));
        tabbedPane.setSelectedIndex(index);

        log.info("Opened new request tab");
    }

    /**
     * Create tab component with title and close button
     */
    private JPanel createTabComponent(String title, String requestId) {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tabPanel.setOpaque(false);

        // Tab title label
        JLabel titleLabel = new JLabel(title);
        tabPanel.add(titleLabel);

        // Close button
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(17, 17));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setToolTipText("Close tab");

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setContentAreaFilled(false);
            }
        });

        closeButton.addActionListener(e -> closeTab(requestId));
        tabPanel.add(closeButton);

        return tabPanel;
    }

    /**
     * Close a tab by request ID
     */
    public void closeTab(String requestId) {
        RequestEditorPanel panel = openTabs.get(requestId);
        if (panel == null) {
            return;
        }

        int index = tabbedPane.indexOfComponent(panel);
        if (index >= 0) {
            tabbedPane.removeTabAt(index);
            openTabs.remove(requestId);
            log.info("Closed tab for request ID: {}", requestId);

            // If no more tabs, show empty state
            if (openTabs.isEmpty()) {
                showEmptyState();
            }
        }
    }

    /**
     * Get currently active editor panel
     */
    public RequestEditorPanel getCurrentEditor() {
        int index = tabbedPane.getSelectedIndex();
        if (index >= 0) {
            Component component = tabbedPane.getComponentAt(index);
            if (component instanceof RequestEditorPanel) {
                return (RequestEditorPanel) component;
            }
        }
        return null;
    }

    /**
     * Update theme for all open tabs
     */
    public void updateTheme() {
        for (RequestEditorPanel panel : openTabs.values()) {
            panel.updateTheme();
        }
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Refresh all open editor panels
        for (RequestEditorPanel panel : openTabs.values()) {
            panel.refreshUI();
        }

        // Refresh empty state panel
        if (emptyStatePanel != null) {
            emptyStatePanel.refreshUI();
        }

        revalidate();
        repaint();
    }

    /**
     * Get tab count
     */
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    /**
     * Update tab title for a specific request
     */
    public void updateTabTitle(String requestId, String newTitle) {
        RequestEditorPanel panel = openTabs.get(requestId);
        if (panel == null) {
            return;
        }

        int index = tabbedPane.indexOfComponent(panel);
        if (index >= 0) {
            // Update the tab component with new title
            tabbedPane.setTabComponentAt(index, createTabComponent(newTitle, requestId));
            log.debug("Updated tab title to: {}", newTitle);
        }
    }
}
