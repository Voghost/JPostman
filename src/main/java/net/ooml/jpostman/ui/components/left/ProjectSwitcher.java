package net.ooml.jpostman.ui.components.left;

import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Project Switcher - Dropdown to switch between projects
 */
public class ProjectSwitcher extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(ProjectSwitcher.class);

    private final MainFrame mainFrame;
    private JComboBox<String> projectComboBox;
    private JLabel label;
    private JButton refreshButton;

    public ProjectSwitcher(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Label
        label = new JLabel(I18nManager.get("project.label") + ":");
        add(label, BorderLayout.WEST);

        // Combo box
        projectComboBox = new JComboBox<>();
        projectComboBox.addActionListener(e -> onProjectChanged());
        add(projectComboBox, BorderLayout.CENTER);

        // Refresh button
        refreshButton = new JButton("⟳");
        refreshButton.setToolTipText(I18nManager.get("project.refresh"));
        refreshButton.addActionListener(e -> loadProjects());
        add(refreshButton, BorderLayout.EAST);

        // Load projects
        loadProjects();
    }

    /**
     * Load available projects
     */
    public void loadProjects() {
        try {
            String currentProject = mainFrame.getCurrentProject();

            projectComboBox.removeAllItems();

            // Get all projects from storage
            // For now, just show the current project
            projectComboBox.addItem(currentProject);
            projectComboBox.setSelectedItem(currentProject);

            log.debug("Projects loaded");
        } catch (Exception e) {
            log.error("Failed to load projects", e);
        }
    }

    /**
     * Handle project selection change
     */
    private void onProjectChanged() {
        String selectedProject = (String) projectComboBox.getSelectedItem();
        if (selectedProject != null && !selectedProject.equals(mainFrame.getCurrentProject())) {
            log.info("Switching to project: {}", selectedProject);
            // TODO: Implement project switching
            mainFrame.showInfo("Project switching will be implemented soon");
        }
    }

    /**
     * Get currently selected project
     */
    public String getSelectedProject() {
        return (String) projectComboBox.getSelectedItem();
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        if (label != null) {
            label.setText(I18nManager.get("project.label") + ":");
        }
        if (refreshButton != null) {
            refreshButton.setToolTipText(I18nManager.get("project.refresh"));
        }
        revalidate();
        repaint();
    }
}
