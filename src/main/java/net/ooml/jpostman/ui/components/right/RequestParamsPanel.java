package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Header;
import net.ooml.jpostman.ui.components.common.KeyValueTablePanel;

import javax.swing.*;
import java.util.List;

/**
 * Request Query Parameters Panel
 */
public class RequestParamsPanel extends JPanel {
    private final KeyValueTablePanel tablePanel;

    public RequestParamsPanel() {
        this.tablePanel = new KeyValueTablePanel(false); // Disable autocomplete dropdown for params
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new java.awt.BorderLayout());
        add(tablePanel, java.awt.BorderLayout.CENTER);
    }

    public List<Header> getParams() {
        return tablePanel.getHeaders();
    }

    public void setParams(List<Header> params) {
        tablePanel.setHeaders(params);
    }

    public void clear() {
        tablePanel.clear();
    }

    /**
     * Set data change listener
     */
    public void setDataChangeListener(Runnable listener) {
        tablePanel.setDataChangeListener(listener);
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // Params panel doesn't have localized text
        revalidate();
        repaint();
    }
}
