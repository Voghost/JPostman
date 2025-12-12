package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;

import javax.swing.*;
import java.awt.*;

/**
 * Empty State Panel - Displayed when no tabs are open
 */
public class EmptyStatePanel extends JPanel {
    private final MainFrame mainFrame;

    public EmptyStatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Icon or message
        JLabel messageLabel = new JLabel(I18nManager.get("emptystate.message"));
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 16f));
        messageLabel.setForeground(Color.GRAY);
        add(messageLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        // New Collection button
        JButton newCollectionBtn = new JButton(I18nManager.get("menu.file.new_collection"));
        newCollectionBtn.setPreferredSize(new Dimension(150, 35));
        newCollectionBtn.addActionListener(e -> mainFrame.getLeftPanel().createNewCollection());
        buttonPanel.add(newCollectionBtn);

        // New Request button
        JButton newRequestBtn = new JButton(I18nManager.get("menu.file.new_request"));
        newRequestBtn.setPreferredSize(new Dimension(150, 35));
        newRequestBtn.addActionListener(e -> mainFrame.getLeftPanel().createNewRequest());
        buttonPanel.add(newRequestBtn);

        add(buttonPanel, gbc);
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }
}
