package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Header;
import net.ooml.jpostman.model.enums.BodyType;
import net.ooml.jpostman.ui.components.common.KeyValueTablePanel;
import net.ooml.jpostman.ui.components.common.SyntaxHighlightTextPane;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Request Body Panel
 */
public class RequestBodyPanel extends JPanel {
    private static final String CARD_TEXT = "TEXT";
    private static final String CARD_FORM = "FORM";
    private static final String CARD_NONE = "NONE";

    private JComboBox<BodyType> bodyTypeComboBox;
    private JButton formatButton;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Different content views
    private SyntaxHighlightTextPane bodyTextPane;
    private KeyValueTablePanel formDataPanel;
    private KeyValueTablePanel urlencodedPanel;
    private JPanel nonePanel;

    // Callback for when body type changes
    private ContentTypeChangeListener contentTypeChangeListener;

    public RequestBodyPanel() {
        initializeUI();
    }

    /**
     * Functional interface for Content-Type change notification
     */
    @FunctionalInterface
    public interface ContentTypeChangeListener {
        void onContentTypeChanged(String contentType);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel with body type selector and format button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Body Type:"));

        bodyTypeComboBox = new JComboBox<>(BodyType.values());
        bodyTypeComboBox.addActionListener(e -> onBodyTypeChanged());
        topPanel.add(bodyTypeComboBox);

        // Format button
        formatButton = new JButton("Format");
        formatButton.setToolTipText("Format and beautify JSON/XML content");
        formatButton.addActionListener(e -> formatContent());
        topPanel.add(formatButton);

        add(topPanel, BorderLayout.NORTH);

        // Content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // None panel
        nonePanel = new JPanel(new BorderLayout());
        JLabel noneLabel = new JLabel("No body content", SwingConstants.CENTER);
        noneLabel.setForeground(Color.GRAY);
        nonePanel.add(noneLabel, BorderLayout.CENTER);
        contentPanel.add(nonePanel, CARD_NONE);

        // Syntax highlighting text pane for raw/JSON/XML content
        bodyTextPane = new SyntaxHighlightTextPane(SyntaxConstants.SYNTAX_STYLE_JSON);
        contentPanel.add(bodyTextPane, CARD_TEXT);

        // Form data table
        formDataPanel = new KeyValueTablePanel();
        contentPanel.add(formDataPanel, "FORM_DATA");

        // URL encoded table
        urlencodedPanel = new KeyValueTablePanel();
        contentPanel.add(urlencodedPanel, "FORM_URLENCODED");

        add(contentPanel, BorderLayout.CENTER);

        // Show initial card
        onBodyTypeChanged();
    }

    private void onBodyTypeChanged() {
        BodyType type = getBodyType();
        if (type == null) {
            return;
        }

        String contentType = null;

        switch (type) {
            case NONE:
                cardLayout.show(contentPanel, CARD_NONE);
                formatButton.setEnabled(false);
                break;
            case FORM_DATA:
                cardLayout.show(contentPanel, "FORM_DATA");
                formatButton.setEnabled(false);
                contentType = "multipart/form-data";
                break;
            case X_WWW_FORM_URLENCODED:
                cardLayout.show(contentPanel, "FORM_URLENCODED");
                formatButton.setEnabled(false);
                contentType = "application/x-www-form-urlencoded";
                break;
            case JSON:
                bodyTextPane.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
                cardLayout.show(contentPanel, CARD_TEXT);
                formatButton.setEnabled(true);
                contentType = "application/json";
                break;
            case XML:
                bodyTextPane.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                cardLayout.show(contentPanel, CARD_TEXT);
                formatButton.setEnabled(true);
                contentType = "application/xml";
                break;
            case RAW:
            default:
                bodyTextPane.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                cardLayout.show(contentPanel, CARD_TEXT);
                formatButton.setEnabled(false);
                break;
        }

        // Notify listener about content type change
        if (contentType != null && contentTypeChangeListener != null) {
            contentTypeChangeListener.onContentTypeChanged(contentType);
        }
    }

    /**
     * Set Content-Type change listener
     */
    public void setContentTypeChangeListener(ContentTypeChangeListener listener) {
        this.contentTypeChangeListener = listener;
    }

    /**
     * Format content in the text pane
     */
    private void formatContent() {
        bodyTextPane.formatContent();
    }

    public BodyType getBodyType() {
        return (BodyType) bodyTypeComboBox.getSelectedItem();
    }

    public void setBodyType(BodyType type) {
        bodyTypeComboBox.setSelectedItem(type);
    }

    public String getBodyContent() {
        BodyType type = getBodyType();
        if (type == null) {
            return "";
        }

        switch (type) {
            case FORM_DATA:
                return convertHeadersToString(formDataPanel.getHeaders());
            case X_WWW_FORM_URLENCODED:
                return convertHeadersToString(urlencodedPanel.getHeaders());
            case NONE:
                return "";
            default:
                return bodyTextPane.getText();
        }
    }

    public void setBodyContent(String content) {
        BodyType type = getBodyType();
        if (type == null || content == null) {
            return;
        }

        switch (type) {
            case FORM_DATA:
                formDataPanel.setHeaders(convertStringToHeaders(content));
                break;
            case X_WWW_FORM_URLENCODED:
                urlencodedPanel.setHeaders(convertStringToHeaders(content));
                break;
            case NONE:
                // No content for NONE type
                break;
            default:
                bodyTextPane.setText(content);
                break;
        }
    }

    /**
     * Convert headers (key-value pairs) to string format
     */
    private String convertHeadersToString(List<Header> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        return headers.stream()
                .filter(h -> h.getKey() != null && !h.getKey().isEmpty())
                .map(h -> h.getKey() + "=" + (h.getValue() != null ? h.getValue() : ""))
                .collect(Collectors.joining("&"));
    }

    /**
     * Convert string format to headers (key-value pairs)
     */
    private List<Header> convertStringToHeaders(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        String[] pairs = content.split("&");
        return java.util.Arrays.stream(pairs)
                .map(pair -> {
                    String[] kv = pair.split("=", 2);
                    return Header.builder()
                            .key(kv[0])
                            .value(kv.length > 1 ? kv[1] : "")
                            .enabled(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void clear() {
        bodyTypeComboBox.setSelectedItem(BodyType.NONE);
        bodyTextPane.clear();
        formDataPanel.clear();
        urlencodedPanel.clear();
    }

    /**
     * Update theme for syntax highlighting
     */
    public void updateTheme() {
        if (bodyTextPane != null) {
            bodyTextPane.updateTheme();
        }
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Body panel doesn't have much localized text
        revalidate();
        repaint();
    }
}
