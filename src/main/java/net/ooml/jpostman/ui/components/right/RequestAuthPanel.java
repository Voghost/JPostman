package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.AuthConfig;
import net.ooml.jpostman.model.enums.AuthType;

import javax.swing.*;
import java.awt.*;

/**
 * Request Auth Panel
 */
public class RequestAuthPanel extends JPanel {
    private JComboBox<AuthType> authTypeComboBox;
    private JPanel authConfigPanel;
    private CardLayout cardLayout;

    // Auth fields
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField tokenField;
    private JTextField apiKeyField;
    private JTextField apiKeyHeaderField;

    public RequestAuthPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel with auth type selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Auth Type:"));

        authTypeComboBox = new JComboBox<>(AuthType.values());
        authTypeComboBox.addActionListener(e -> onAuthTypeChanged());
        topPanel.add(authTypeComboBox);

        add(topPanel, BorderLayout.NORTH);

        // Card layout for different auth panels
        cardLayout = new CardLayout();
        authConfigPanel = new JPanel(cardLayout);

        authConfigPanel.add(createNonePanel(), AuthType.NONE.name());
        authConfigPanel.add(createBasicAuthPanel(), AuthType.BASIC.name());
        authConfigPanel.add(createBearerTokenPanel(), AuthType.BEARER.name());
        authConfigPanel.add(createApiKeyPanel(), AuthType.API_KEY.name());

        add(authConfigPanel, BorderLayout.CENTER);
    }

    private JPanel createNonePanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("No authentication"));
        return panel;
    }

    private JPanel createBasicAuthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel createBearerTokenPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Token:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tokenField = new JTextField(30);
        panel.add(tokenField, gbc);

        return panel;
    }

    private JPanel createApiKeyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Header Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        apiKeyHeaderField = new JTextField(20);
        apiKeyHeaderField.setText("X-API-Key");
        panel.add(apiKeyHeaderField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("API Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        apiKeyField = new JTextField(30);
        panel.add(apiKeyField, gbc);

        return panel;
    }

    private void onAuthTypeChanged() {
        AuthType type = (AuthType) authTypeComboBox.getSelectedItem();
        cardLayout.show(authConfigPanel, type.name());
    }

    public AuthConfig getAuthConfig() {
        AuthType type = (AuthType) authTypeComboBox.getSelectedItem();

        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder().type(type);

        switch (type) {
            case BASIC:
                builder.username(usernameField.getText())
                        .password(new String(passwordField.getPassword()));
                break;
            case BEARER:
                builder.token(tokenField.getText());
                break;
            case API_KEY:
                builder.apiKey(apiKeyField.getText())
                        .apiKeyHeader(apiKeyHeaderField.getText());
                break;
        }

        return builder.build();
    }

    public void setAuthConfig(AuthConfig auth) {
        if (auth == null) {
            authTypeComboBox.setSelectedItem(AuthType.NONE);
            return;
        }

        authTypeComboBox.setSelectedItem(auth.getType());

        switch (auth.getType()) {
            case BASIC:
                usernameField.setText(auth.getUsername());
                passwordField.setText(auth.getPassword());
                break;
            case BEARER:
                tokenField.setText(auth.getToken());
                break;
            case API_KEY:
                apiKeyField.setText(auth.getApiKey());
                apiKeyHeaderField.setText(auth.getApiKeyHeader());
                break;
        }
    }

    public void clear() {
        authTypeComboBox.setSelectedItem(AuthType.NONE);
        usernameField.setText("");
        passwordField.setText("");
        tokenField.setText("");
        apiKeyField.setText("");
        apiKeyHeaderField.setText("X-API-Key");
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Auth panel doesn't have localized text
        revalidate();
        repaint();
    }
}
