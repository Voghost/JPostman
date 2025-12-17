package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.model.Response;
import net.ooml.jpostman.ui.components.common.SyntaxHighlightTextPane;
import net.ooml.jpostman.util.CurlGenerator;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Response Panel - Display HTTP response
 */
public class ResponsePanel extends JPanel {
    private JLabel statusLabel;
    private JLabel durationLabel;
    private JLabel sizeLabel;
    private SyntaxHighlightTextPane responseBodyPane;
    private JTable responseHeadersTable;
    private javax.swing.table.DefaultTableModel responseHeadersTableModel;
    private JTextArea requestInfoArea;
    private JTabbedPane tabbedPane;
    private JButton formatButton;

    // Store current request for request details
    private Request currentRequest;

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

        // Headers tab with table
        responseHeadersTableModel = new javax.swing.table.DefaultTableModel(
                new Object[]{"Key", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        responseHeadersTable = new JTable(responseHeadersTableModel);
        responseHeadersTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responseHeadersTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane headersScrollPane = new JScrollPane(responseHeadersTable);
        tabbedPane.addTab("Headers", headersScrollPane);

        // Request tab - shows the actual request sent
        requestInfoArea = new JTextArea();
        requestInfoArea.setEditable(false);
        requestInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane requestScrollPane = new JScrollPane(requestInfoArea);
        tabbedPane.addTab("Request", requestScrollPane);

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
    public void displayResponse(Response response, Request request) {
        // Store the request for cURL generation
        this.currentRequest = request;

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

        // Update headers table
        responseHeadersTableModel.setRowCount(0); // Clear existing rows
        if (response.getHeaders() != null) {
            for (net.ooml.jpostman.model.Header header : response.getHeaders()) {
                responseHeadersTableModel.addRow(new Object[]{
                        header.getKey(),
                        header.getValue()
                });
            }
        }

        // Update request info
        if (request != null) {
            displayRequestInfo(request);
        } else {
            requestInfoArea.setText("");
        }
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
     * Display request information
     */
    private void displayRequestInfo(Request request) {
        StringBuilder requestText = new StringBuilder();

        // Method and URL
        requestText.append(request.getMethod() != null ? request.getMethod().name() : "GET")
                .append(" ").append(request.getUrl() != null ? request.getUrl() : "")
                .append("\n\n");

        // Headers
        requestText.append("=== Headers ===\n");
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (net.ooml.jpostman.model.Header header : request.getHeaders()) {
                if (Boolean.TRUE.equals(header.getEnabled())) {
                    requestText.append(header.getKey())
                            .append(": ")
                            .append(header.getValue() != null ? header.getValue() : "")
                            .append("\n");
                }
            }
        } else {
            requestText.append("(No headers)\n");
        }

        // Query Parameters
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            requestText.append("\n=== Query Parameters ===\n");
            for (net.ooml.jpostman.model.Header param : request.getQueryParams()) {
                if (Boolean.TRUE.equals(param.getEnabled())) {
                    requestText.append(param.getKey())
                            .append("=")
                            .append(param.getValue() != null ? param.getValue() : "")
                            .append("\n");
                }
            }
        }

        // Body
        if (request.getBody() != null && request.getBody().getContent() != null &&
            !request.getBody().getContent().isEmpty()) {
            requestText.append("\n=== Body ===\n");
            requestText.append("Type: ").append(request.getBody().getType()).append("\n\n");
            requestText.append(request.getBody().getContent());
        }

        requestInfoArea.setText(requestText.toString());
        requestInfoArea.setCaretPosition(0);
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
        responseHeadersTableModel.setRowCount(0);
        requestInfoArea.setText("");
        currentRequest = null;
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
