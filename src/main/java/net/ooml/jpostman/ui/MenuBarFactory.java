package net.ooml.jpostman.ui;

import net.ooml.jpostman.ui.i18n.I18nManager;
import net.ooml.jpostman.ui.i18n.LanguageType;
import net.ooml.jpostman.ui.theme.ThemeManager;
import net.ooml.jpostman.ui.theme.ThemeType;
import net.ooml.jpostman.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Menu Bar Factory - Creates menu bar for the application
 */
public class MenuBarFactory {
    private static final Logger log = LoggerFactory.getLogger(MenuBarFactory.class);

    private final MainFrame mainFrame;

    public MenuBarFactory(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Create menu bar
     */
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        menuBar.add(createFileMenu());

        // Edit menu
        menuBar.add(createEditMenu());

        // View menu
        menuBar.add(createViewMenu());

        // Request menu
        menuBar.add(createRequestMenu());

        // Tools menu
        menuBar.add(createToolsMenu());

        // Help menu
        menuBar.add(createHelpMenu());

        return menuBar;
    }

    /**
     * Create File menu
     */
    private JMenu createFileMenu() {
        JMenu menu = new JMenu(I18nManager.get("menu.file"));
        menu.setMnemonic(KeyEvent.VK_F);

        // New Request
        JMenuItem newRequest = new JMenuItem(I18nManager.get("menu.file.new_request"));
        newRequest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, getMenuShortcutKeyMask()));
        newRequest.addActionListener(e -> mainFrame.onNewRequest());
        menu.add(newRequest);

        // New Collection
        JMenuItem newCollection = new JMenuItem(I18nManager.get("menu.file.new_collection"));
        newCollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                getMenuShortcutKeyMask() | InputEvent.SHIFT_DOWN_MASK));
        newCollection.addActionListener(e -> mainFrame.onNewCollection());
        menu.add(newCollection);

        menu.addSeparator();

        // New Project
        JMenuItem newProject = new JMenuItem(I18nManager.get("menu.file.new_project"));
        newProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                getMenuShortcutKeyMask() | InputEvent.SHIFT_DOWN_MASK));
        newProject.addActionListener(e -> mainFrame.onNewProject());
        menu.add(newProject);

        // Switch Project
        JMenuItem switchProject = new JMenuItem(I18nManager.get("menu.file.switch_project"));
        switchProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, getMenuShortcutKeyMask()));
        switchProject.addActionListener(e -> mainFrame.onSwitchProject());
        menu.add(switchProject);

        menu.addSeparator();

        // Rename Project
        JMenuItem renameProject = new JMenuItem(I18nManager.get("menu.file.rename_project"));
        renameProject.addActionListener(e -> mainFrame.onRenameProject());
        menu.add(renameProject);

        // Delete Project
        JMenuItem deleteProject = new JMenuItem(I18nManager.get("menu.file.delete_project"));
        deleteProject.addActionListener(e -> mainFrame.onDeleteProject());
        menu.add(deleteProject);

        menu.addSeparator();

        // Save
        JMenuItem save = new JMenuItem(I18nManager.get("menu.file.save"));
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, getMenuShortcutKeyMask()));
        save.addActionListener(e -> mainFrame.onSave());
        menu.add(save);

        menu.addSeparator();

        // Import
        JMenuItem importItem = new JMenuItem(I18nManager.get("menu.file.import"));
        importItem.addActionListener(e -> mainFrame.onImport());
        menu.add(importItem);

        // Export
        JMenuItem export = new JMenuItem(I18nManager.get("menu.file.export"));
        export.addActionListener(e -> mainFrame.onExport());
        menu.add(export);

        menu.addSeparator();

        // Exit (not on macOS, handled by system)
        if (!OSUtil.isMacOS()) {
            JMenuItem exit = new JMenuItem(I18nManager.get("menu.file.exit"));
            exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, getMenuShortcutKeyMask()));
            exit.addActionListener(e -> mainFrame.onExit());
            menu.add(exit);
        }

        return menu;
    }

    /**
     * Create Edit menu
     */
    private JMenu createEditMenu() {
        JMenu menu = new JMenu(I18nManager.get("menu.edit"));
        menu.setMnemonic(KeyEvent.VK_E);

        // Cut
        JMenuItem cut = new JMenuItem(I18nManager.get("menu.edit.cut"));
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, getMenuShortcutKeyMask()));
        menu.add(cut);

        // Copy
        JMenuItem copy = new JMenuItem(I18nManager.get("menu.edit.copy"));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, getMenuShortcutKeyMask()));
        menu.add(copy);

        // Paste
        JMenuItem paste = new JMenuItem(I18nManager.get("menu.edit.paste"));
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, getMenuShortcutKeyMask()));
        menu.add(paste);

        menu.addSeparator();

        // Delete
        JMenuItem delete = new JMenuItem(I18nManager.get("menu.edit.delete"));
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        delete.addActionListener(e -> mainFrame.onDelete());
        menu.add(delete);

        return menu;
    }

    /**
     * Create View menu
     */
    private JMenu createViewMenu() {
        JMenu menu = new JMenu(I18nManager.get("menu.view"));
        menu.setMnemonic(KeyEvent.VK_V);

        // Theme submenu
        JMenu themeMenu = new JMenu(I18nManager.get("menu.view.theme"));
        ButtonGroup themeGroup = new ButtonGroup();

        for (ThemeType theme : ThemeType.values()) {
            JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(theme.getDisplayName());
            themeItem.setSelected(theme == ThemeManager.getInstance().getCurrentTheme());
            themeItem.addActionListener(e -> {
                try {
                    ThemeManager.getInstance().changeTheme(theme);
                    // Update syntax highlighting theme
                    mainFrame.getRightPanel().updateTheme();
                } catch (Exception ex) {
                    log.error("Failed to change theme", ex);
                    JOptionPane.showMessageDialog(mainFrame,
                            "Failed to change theme: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
            themeGroup.add(themeItem);
            themeMenu.add(themeItem);
        }
        menu.add(themeMenu);

        menu.addSeparator();

        // Language submenu
        JMenu languageMenu = new JMenu(I18nManager.get("menu.view.language"));
        ButtonGroup languageGroup = new ButtonGroup();

        for (LanguageType language : LanguageType.values()) {
            JRadioButtonMenuItem languageItem = new JRadioButtonMenuItem(language.getDisplayName());
            languageItem.setSelected(language == I18nManager.getCurrentLanguage());
            languageItem.addActionListener(e -> {
                try {
                    I18nManager.changeLanguage(language);

                    // Save language to config
                    mainFrame.getAppConfig().setLanguage(language.getCode());
                    mainFrame.getAppConfig().save();

                    // Refresh UI immediately
                    mainFrame.refreshUI();

                    log.info("Language changed to: {}", language.getDisplayName());
                } catch (Exception ex) {
                    log.error("Failed to change language", ex);
                    JOptionPane.showMessageDialog(mainFrame,
                            "Failed to change language: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
            languageGroup.add(languageItem);
            languageMenu.add(languageItem);
        }
        menu.add(languageMenu);

        menu.addSeparator();

        // Show/Hide panels
        JCheckBoxMenuItem showSidebar = new JCheckBoxMenuItem(I18nManager.get("menu.view.show_sidebar"));
        showSidebar.setSelected(true);
        showSidebar.addActionListener(e -> mainFrame.toggleSidebar(showSidebar.isSelected()));
        menu.add(showSidebar);

        return menu;
    }

    /**
     * Create Request menu
     */
    private JMenu createRequestMenu() {
        JMenu menu = new JMenu(I18nManager.get("menu.request"));
        menu.setMnemonic(KeyEvent.VK_R);

        // Send
        JMenuItem send = new JMenuItem(I18nManager.get("menu.request.send"));
        send.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, getMenuShortcutKeyMask()));
        send.addActionListener(e -> mainFrame.onSendRequest());
        menu.add(send);

        menu.addSeparator();

        // Duplicate
        JMenuItem duplicate = new JMenuItem(I18nManager.get("menu.request.duplicate"));
        duplicate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, getMenuShortcutKeyMask()));
        duplicate.addActionListener(e -> mainFrame.onDuplicateRequest());
        menu.add(duplicate);

        return menu;
    }

    /**
     * Create Tools menu
     */
    private JMenu createToolsMenu() {
        JMenu menu = new JMenu(I18nManager.get("menu.tools"));
        menu.setMnemonic(KeyEvent.VK_T);

        // Environment
        JMenuItem environment = new JMenuItem(I18nManager.get("menu.tools.environment"));
        environment.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, getMenuShortcutKeyMask()));
        environment.addActionListener(e -> mainFrame.onManageEnvironment());
        menu.add(environment);

        // Variables
        JMenuItem variables = new JMenuItem(I18nManager.get("menu.tools.variables"));
        variables.addActionListener(e -> mainFrame.onManageVariables());
        menu.add(variables);

        menu.addSeparator();

        // Settings (not on macOS, handled by system)
        if (!OSUtil.isMacOS()) {
            JMenuItem settings = new JMenuItem(I18nManager.get("menu.tools.settings"));
            settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, getMenuShortcutKeyMask()));
            settings.addActionListener(e -> mainFrame.onSettings());
            menu.add(settings);
        }

        return menu;
    }

    /**
     * Create Help menu
     */
    private JMenu createHelpMenu() {
        JMenu menu = new JMenu(I18nManager.get("menu.help"));
        menu.setMnemonic(KeyEvent.VK_H);

        // Documentation
        JMenuItem documentation = new JMenuItem(I18nManager.get("menu.help.documentation"));
        documentation.addActionListener(e -> mainFrame.onDocumentation());
        menu.add(documentation);

        // About (not on macOS, handled by system)
        if (!OSUtil.isMacOS()) {
            menu.addSeparator();
            JMenuItem about = new JMenuItem(I18nManager.get("menu.help.about"));
            about.addActionListener(e -> mainFrame.onAbout());
            menu.add(about);
        }

        return menu;
    }

    /**
     * Get menu shortcut key mask based on OS
     */
    private int getMenuShortcutKeyMask() {
        return OSUtil.isMacOS()
                ? InputEvent.META_DOWN_MASK  // Command on macOS
                : InputEvent.CTRL_DOWN_MASK; // Ctrl on other OS
    }
}
