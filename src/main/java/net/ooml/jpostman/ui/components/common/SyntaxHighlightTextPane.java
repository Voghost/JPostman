package net.ooml.jpostman.ui.components.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.formdev.flatlaf.FlatLaf;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Syntax highlighting text pane using RSyntaxTextArea
 */
public class SyntaxHighlightTextPane extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(SyntaxHighlightTextPane.class);

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final ObjectMapper jsonMapper;
    private String currentSyntax = SyntaxConstants.SYNTAX_STYLE_JSON;

    public SyntaxHighlightTextPane() {
        this(SyntaxConstants.SYNTAX_STYLE_JSON);
    }

    public SyntaxHighlightTextPane(String syntaxStyle) {
        setLayout(new BorderLayout());

        // Initialize text area
        textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(syntaxStyle);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setTabSize(2);
        textArea.setMarkOccurrences(true);

        // Apply theme based on current LAF
        applyTheme();

        // Create scroll pane
        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize JSON mapper for formatting
        jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.currentSyntax = syntaxStyle;
    }

    /**
     * Apply RSyntaxTextArea theme based on current FlatLaf theme
     */
    private void applyTheme() {
        try {
            String themePath;
            if (isDarkTheme()) {
                // Use dark theme
                themePath = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
            } else {
                // Use light theme
                themePath = "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
            }

            Theme theme = Theme.load(getClass().getResourceAsStream(themePath));
            theme.apply(textArea);

            log.debug("Applied syntax theme: {}", themePath);
        } catch (IOException e) {
            log.warn("Failed to load syntax highlighting theme, using default", e);
            // Fallback to manual color setting
            if (isDarkTheme()) {
                textArea.setBackground(new Color(43, 43, 43));
                textArea.setForeground(new Color(169, 183, 198));
                textArea.setCaretColor(Color.WHITE);
                textArea.setCurrentLineHighlightColor(new Color(60, 60, 60));
            }
        }
    }

    /**
     * Check if current theme is dark
     */
    private boolean isDarkTheme() {
        // Check if FlatLaf is installed and if it's a dark theme
        try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            if (laf instanceof FlatLaf) {
                return FlatLaf.isLafDark();
            }
            // Fallback: check background color brightness
            Color bg = UIManager.getColor("Panel.background");
            if (bg != null) {
                // Calculate brightness using perceived luminance formula
                int brightness = (int) (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue());
                return brightness < 128;
            }
        } catch (Exception e) {
            log.debug("Could not detect theme type", e);
        }
        return false;
    }

    /**
     * Update theme when LAF changes
     */
    public void updateTheme() {
        applyTheme();
        textArea.repaint();
    }

    /**
     * Get text content
     */
    public String getText() {
        return textArea.getText();
    }

    /**
     * Set text content
     */
    public void setText(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    /**
     * Set syntax highlighting style
     */
    public void setSyntaxStyle(String syntaxStyle) {
        this.currentSyntax = syntaxStyle;
        textArea.setSyntaxEditingStyle(syntaxStyle);
    }

    /**
     * Format and beautify content based on syntax type
     */
    public void formatContent() {
        String content = textArea.getText();
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        try {
            String formatted = null;
            switch (currentSyntax) {
                case SyntaxConstants.SYNTAX_STYLE_JSON:
                    formatted = formatJson(content);
                    break;
                case SyntaxConstants.SYNTAX_STYLE_XML:
                    formatted = formatXml(content);
                    break;
                default:
                    // No formatting for other types
                    return;
            }

            if (formatted != null) {
                setText(formatted);
            }
        } catch (Exception e) {
            log.error("Failed to format content", e);
            JOptionPane.showMessageDialog(this,
                    "Failed to format content: " + e.getMessage(),
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Format JSON content
     */
    private String formatJson(String json) throws IOException {
        Object obj = jsonMapper.readValue(json, Object.class);
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * Format XML content (simple indentation)
     */
    private String formatXml(String xml) {
        try {
            javax.xml.parsers.DocumentBuilder db =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

            javax.xml.transform.Transformer transformer =
                    javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(doc),
                    new javax.xml.transform.stream.StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to format XML", e);
            return xml; // Return original if formatting fails
        }
    }

    /**
     * Set editable
     */
    public void setEditable(boolean editable) {
        textArea.setEditable(editable);
    }

    /**
     * Clear content
     */
    public void clear() {
        textArea.setText("");
    }

    /**
     * Get the underlying text area
     */
    public RSyntaxTextArea getTextArea() {
        return textArea;
    }
}
