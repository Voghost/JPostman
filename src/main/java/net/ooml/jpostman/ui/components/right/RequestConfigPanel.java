package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.model.enums.HttpMethod;
import net.ooml.jpostman.ui.MainFrame;
import net.ooml.jpostman.ui.i18n.I18nManager;

import javax.swing.*;
import java.awt.*;

/**
 * Request Config Panel - Method + URL + Send button
 */
public class RequestConfigPanel extends JPanel {
    private final MainFrame mainFrame;

    private JComboBox<HttpMethod> methodComboBox;
    private JTextField urlField;
    private JButton sendButton;
    private JButton saveButton;
    private JButton curlButton;

    public RequestConfigPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Left panel: Method + URL
        JPanel leftPanel = new JPanel(new BorderLayout(5, 0));

        // Method combo box
        methodComboBox = new JComboBox<>(HttpMethod.values());
        methodComboBox.setPreferredSize(new Dimension(100, 30));
        leftPanel.add(methodComboBox, BorderLayout.WEST);

        // URL field
        urlField = new JTextField();
        urlField.setToolTipText("Enter request URL (supports {{variables}})");
        leftPanel.add(urlField, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);

        // Right panel: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        curlButton = new JButton("cURL");
        curlButton.addActionListener(e -> onViewCurl());
        buttonPanel.add(curlButton);

        saveButton = new JButton(I18nManager.get("button.save"));
        saveButton.addActionListener(e -> onSave());
        buttonPanel.add(saveButton);

        sendButton = new JButton(I18nManager.get("button.send"));
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.setBackground(new Color(40, 167, 69)); // Green color
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.addActionListener(e -> onSend());
        buttonPanel.add(sendButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    /**
     * Load request into panel
     */
    public void loadRequest(Request request) {
        if (request != null) {
            methodComboBox.setSelectedItem(request.getMethod());
            urlField.setText(request.getUrl());
        } else {
            clear();
        }
    }

    /**
     * Get current method
     */
    public HttpMethod getMethod() {
        return (HttpMethod) methodComboBox.getSelectedItem();
    }

    /**
     * Set method
     */
    public void setMethod(HttpMethod method) {
        methodComboBox.setSelectedItem(method);
    }

    /**
     * Get URL
     */
    public String getUrl() {
        return urlField.getText();
    }

    /**
     * Set URL
     */
    public void setUrl(String url) {
        urlField.setText(url);
    }

    /**
     * Clear panel
     */
    public void clear() {
        methodComboBox.setSelectedItem(HttpMethod.GET);
        urlField.setText("");
    }

    /**
     * Handle send button click
     */
    private void onSend() {
        // Will be handled by RequestEditorPanel
    }

    /**
     * Handle save button click
     */
    private void onSave() {
        // Will be handled by RequestEditorPanel
    }

    /**
     * Handle view cURL button click
     */
    private void onViewCurl() {
        // Will be handled by RequestEditorPanel
    }

    /**
     * Set send action
     */
    public void setSendAction(Runnable action) {
        for (java.awt.event.ActionListener listener : sendButton.getActionListeners()) {
            sendButton.removeActionListener(listener);
        }
        sendButton.addActionListener(e -> action.run());
    }

    /**
     * Set save action
     */
    public void setSaveAction(Runnable action) {
        for (java.awt.event.ActionListener listener : saveButton.getActionListeners()) {
            saveButton.removeActionListener(listener);
        }
        saveButton.addActionListener(e -> action.run());
    }

    /**
     * Set cURL action
     */
    public void setCurlAction(Runnable action) {
        for (java.awt.event.ActionListener listener : curlButton.getActionListeners()) {
            curlButton.removeActionListener(listener);
        }
        curlButton.addActionListener(e -> action.run());
    }

    /**
     * Set URL change listener
     */
    public void setUrlChangeListener(Runnable listener) {
        urlField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                listener.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                listener.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                listener.run();
            }
        });
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        if (sendButton != null) {
            sendButton.setText(I18nManager.get("button.send"));
            sendButton.setToolTipText(I18nManager.get("tooltip.send"));
        }
        if (saveButton != null) {
            saveButton.setText(I18nManager.get("button.save"));
            saveButton.setToolTipText(I18nManager.get("tooltip.save"));
        }
        revalidate();
        repaint();
    }
}
