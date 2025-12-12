package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Response;
import net.ooml.jpostman.ui.components.common.SyntaxHighlightTextPane;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Response Panel - Display HTTP response
 */
public class ResponsePanel extends JPanel {
    private JLabel statusLabel;
    private JLabel durationLabel;
    private JLabel sizeLabel;
    private SyntaxHighlightTextPane responseBodyPane;
    private JTextArea responseHeadersArea;
    private JTabbedPane tabbedPane;
    private JButton formatButton;

    public ResponsePanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel with status info and format button
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        statusLabel = new JLabel("Status: -");
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));

        durationLabel = new JLabel("Time: -");
        statusPanel.add(durationLabel);
        statusPanel.add(Box.createHorizontalStrut(20));

        sizeLabel = new JLabel("Size: -");
        statusPanel.add(sizeLabel);
        statusPanel.add(Box.createHorizontalStrut(20));

        // Format button
        formatButton = new JButton("Format");
        formatButton.setToolTipText("Format and beautify response");
        formatButton.addActionListener(e -> formatResponse());
        formatButton.setEnabled(false);
        statusPanel.add(formatButton);

        add(statusPanel, BorderLayout.NORTH);

        // Tabbed pane for body and headers
        tabbedPane = new JTabbedPane();

        // Body tab with syntax highlighting
        responseBodyPane = new SyntaxHighlightTextPane(SyntaxConstants.SYNTAX_STYLE_JSON);
        responseBodyPane.setEditable(false);
        tabbedPane.addTab("Body", responseBodyPane);

        // Headers tab
        responseHeadersArea = new JTextArea();
        responseHeadersArea.setEditable(false);
        responseHeadersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane headersScrollPane = new JScrollPane(responseHeadersArea);
        tabbedPane.addTab("Headers", headersScrollPane);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Format response body
     */
    private void formatResponse() {
        responseBodyPane.formatContent();
    }

    /**
     * Display response
     */
    public void displayResponse(Response response) {
        if (response == null) {
            clear();
            return;
        }

        // Update status
        String statusText = String.format("Status: %s %s",
                response.getStatusCode() != null ? response.getStatusCode() : "-",
                response.getStatusText() != null ? response.getStatusText() : "");
        statusLabel.setText(statusText);

        // Set status color
        if (response.isSuccessful()) {
            statusLabel.setForeground(new Color(0, 128, 0));
        } else {
            statusLabel.setForeground(new Color(200, 0, 0));
        }

        // Update duration
        durationLabel.setText(String.format("Time: %dms",
                response.getDuration() != null ? response.getDuration() : 0));

        // Update size
        sizeLabel.setText(String.format("Size: %d bytes",
                response.getSize() != null ? response.getSize() : 0));

        // Detect content type and set syntax highlighting
        String contentType = getContentType(response);
        String syntaxStyle = detectSyntaxStyle(contentType, response.getBody());
        responseBodyPane.setSyntaxStyle(syntaxStyle);

        // Enable format button for JSON/XML
        formatButton.setEnabled(
                SyntaxConstants.SYNTAX_STYLE_JSON.equals(syntaxStyle) ||
                SyntaxConstants.SYNTAX_STYLE_XML.equals(syntaxStyle)
        );

        // Update body
        responseBodyPane.setText(response.getBody() != null ? response.getBody() : "");

        // Update headers
        StringBuilder headerText = new StringBuilder();
        if (response.getHeaders() != null) {
            for (net.ooml.jpostman.model.Header header : response.getHeaders()) {
                headerText.append(header.getKey())
                        .append(": ")
                        .append(header.getValue())
                        .append("\n");
            }
        }
        responseHeadersArea.setText(headerText.toString());
        responseHeadersArea.setCaretPosition(0);
    }

    /**
     * Get Content-Type from response headers
     */
    private String getContentType(Response response) {
        if (response.getHeaders() != null) {
            for (net.ooml.jpostman.model.Header header : response.getHeaders()) {
                if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                    return header.getValue();
                }
            }
        }
        return "";
    }

    /**
     * Detect syntax highlighting style based on content type and body
     */
    private String detectSyntaxStyle(String contentType, String body) {
        if (contentType == null) {
            contentType = "";
        }

        // Check content type
        if (contentType.contains("application/json") || contentType.contains("text/json")) {
            return SyntaxConstants.SYNTAX_STYLE_JSON;
        } else if (contentType.contains("application/xml") || contentType.contains("text/xml")) {
            return SyntaxConstants.SYNTAX_STYLE_XML;
        } else if (contentType.contains("text/html")) {
            return SyntaxConstants.SYNTAX_STYLE_HTML;
        } else if (contentType.contains("text/javascript") || contentType.contains("application/javascript")) {
            return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
        }

        // Try to detect from body content
        if (body != null && !body.trim().isEmpty()) {
            String trimmed = body.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return SyntaxConstants.SYNTAX_STYLE_JSON;
            } else if (trimmed.startsWith("<")) {
                return SyntaxConstants.SYNTAX_STYLE_XML;
            }
        }

        // Default to plain text
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    /**
     * Clear response
     */
    public void clear() {
        statusLabel.setText("Status: -");
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        durationLabel.setText("Time: -");
        sizeLabel.setText("Size: -");
        responseBodyPane.clear();
        responseHeadersArea.setText("");
        formatButton.setEnabled(false);
    }

    /**
     * Update theme for syntax highlighting
     */
    public void updateTheme() {
        if (responseBodyPane != null) {
            responseBodyPane.updateTheme();
        }
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Response panel doesn't have localized text
        revalidate();
        repaint();
    }
}
