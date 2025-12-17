package net.ooml.jpostman.ui.components.left;

import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

        // Add right-click context menu to combo box
        projectComboBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });

        add(projectComboBox, BorderLayout.CENTER);

        // Button panel (holds + button and refresh button)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        buttonPanel.setOpaque(false);

        // New project button
        JButton newProjectButton = new JButton("+");
        newProjectButton.setToolTipText(I18nManager.get("project.new"));
        newProjectButton.addActionListener(e -> mainFrame.onNewProject());
        newProjectButton.setPreferredSize(new Dimension(25, 25));
        buttonPanel.add(newProjectButton);

        // Refresh button
        refreshButton = new JButton("⟳");
        refreshButton.setToolTipText(I18nManager.get("project.refresh"));
        refreshButton.addActionListener(e -> loadProjects());
        refreshButton.setPreferredSize(new Dimension(25, 25));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.EAST);

        // Load projects
        loadProjects();
    }

    /**
     * Show context menu for project operations
     */
    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem newProject = new JMenuItem(I18nManager.get("project.new"));
        newProject.addActionListener(ev -> mainFrame.onNewProject());
        menu.add(newProject);

        JMenuItem switchProject = new JMenuItem(I18nManager.get("project.switch"));
        switchProject.addActionListener(ev -> mainFrame.onSwitchProject());
        menu.add(switchProject);

        menu.addSeparator();

        JMenuItem renameProject = new JMenuItem(I18nManager.get("project.rename"));
        renameProject.addActionListener(ev -> mainFrame.onRenameProject());
        menu.add(renameProject);

        JMenuItem deleteProject = new JMenuItem(I18nManager.get("project.delete"));
        deleteProject.addActionListener(ev -> mainFrame.onDeleteProject());
        menu.add(deleteProject);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Load available projects
     */
    public void loadProjects() {
        try {
            String currentProject = mainFrame.getCurrentProject();

            projectComboBox.removeAllItems();

            // Get all projects from storage
            java.util.List<String> projects = mainFrame.getStorageService().listAllProjects();

            if (projects.isEmpty()) {
                projectComboBox.addItem(currentProject);
            } else {
                for (String project : projects) {
                    projectComboBox.addItem(project);
                }
            }

            // Select current project
            projectComboBox.setSelectedItem(currentProject);

            log.debug("Loaded {} projects", projects.size());
        } catch (Exception e) {
            log.error("Failed to load projects", e);
            // Fallback to current project
            projectComboBox.addItem(mainFrame.getCurrentProject());
        }
    }

    /**
     * Handle project selection change
     */
    private void onProjectChanged() {
        // Prevent triggering during programmatic updates
        if (projectComboBox.getSelectedItem() == null) {
            return;
        }

        String selectedProject = (String) projectComboBox.getSelectedItem();
        String currentProject = mainFrame.getCurrentProject();

        if (!selectedProject.equals(currentProject)) {
            log.info("User selected project: {}", selectedProject);
            mainFrame.switchProject(selectedProject);
        }
    }

    /**
     * Get currently selected project
     */
    public String getSelectedProject() {
        return (String) projectComboBox.getSelectedItem();
    }

    /**
     * Public method to refresh project list (called from MainFrame)
     */
    public void refresh() {
        loadProjects();
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
