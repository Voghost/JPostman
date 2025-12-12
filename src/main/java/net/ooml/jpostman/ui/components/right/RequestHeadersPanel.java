package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Header;
import net.ooml.jpostman.ui.components.common.KeyValueTablePanel;

import javax.swing.*;
import java.util.List;

/**
 * Request Headers Panel
 */
public class RequestHeadersPanel extends JPanel {
    private final KeyValueTablePanel tablePanel;

    public RequestHeadersPanel() {
        this.tablePanel = new KeyValueTablePanel();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new java.awt.BorderLayout());
        add(tablePanel, java.awt.BorderLayout.CENTER);
    }

    public List<Header> getHeaders() {
        return tablePanel.getHeaders();
    }

    public void setHeaders(List<Header> headers) {
        tablePanel.setHeaders(headers);
    }

    public void clear() {
        tablePanel.clear();
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Headers panel doesn't have localized text
        revalidate();
        repaint();
    }
}
