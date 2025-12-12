package net.ooml.jpostman.ui.components.common;

import net.ooml.jpostman.model.Header;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

/**
 * Reusable Key-Value Table Panel for headers, params, etc.
 */
public class KeyValueTablePanel extends JPanel {
    private final KeyValueTableModel tableModel;
    private final JTable table;
    private final boolean enableKeyAutocomplete;

    public KeyValueTablePanel() {
        this(new KeyValueTableModel(), true);
    }

    public KeyValueTablePanel(boolean enableKeyAutocomplete) {
        this(new KeyValueTableModel(), enableKeyAutocomplete);
    }

    public KeyValueTablePanel(List<Header> headers) {
        this(new KeyValueTableModel(headers), true);
    }

    private KeyValueTablePanel(KeyValueTableModel tableModel, boolean enableKeyAutocomplete) {
        this.tableModel = tableModel;
        this.table = new JTable(tableModel);
        this.enableKeyAutocomplete = enableKeyAutocomplete;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Configure table
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);

        // Set column widths
        TableColumn enabledColumn = table.getColumnModel().getColumn(0);
        enabledColumn.setPreferredWidth(50);
        enabledColumn.setMaxWidth(50);

        TableColumn keyColumn = table.getColumnModel().getColumn(1);
        keyColumn.setPreferredWidth(200); // Key

        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Value
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Description

        // Add autocomplete for common HTTP headers (only if enabled)
        if (enableKeyAutocomplete) {
            JComboBox<String> keyComboBox = new JComboBox<>(getCommonHeaders());
            keyComboBox.setEditable(true);
            keyColumn.setCellEditor(new javax.swing.DefaultCellEditor(keyComboBox));
        }

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Toolbar
        add(createToolbar(), BorderLayout.SOUTH);
    }

    /**
     * Get list of common HTTP headers
     */
    private String[] getCommonHeaders() {
        return new String[]{
            "Accept",
            "Accept-Encoding",
            "Accept-Language",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Content-Type",
            "Content-Length",
            "Cookie",
            "Host",
            "Origin",
            "Referer",
            "User-Agent",
            "X-API-Key",
            "X-Requested-With",
            "If-Modified-Since",
            "If-None-Match",
            "Range",
            "X-CSRF-Token",
            "X-Forwarded-For"
        };
    }

    /**
     * Create toolbar with action buttons
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Add button
        JButton addButton = new JButton("+");
        addButton.setToolTipText("Add Row");
        addButton.addActionListener(e -> addRow());
        toolbar.add(addButton);

        // Delete button
        JButton deleteButton = new JButton("-");
        deleteButton.setToolTipText("Delete Row");
        deleteButton.addActionListener(e -> deleteSelectedRow());
        toolbar.add(deleteButton);

        // Clear button
        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> clearAll());
        toolbar.add(clearButton);

        return toolbar;
    }

    /**
     * Add new row
     */
    private void addRow() {
        tableModel.addRow(new KeyValueTableModel.Row());
    }

    /**
     * Delete selected row
     */
    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
        }
    }

    /**
     * Clear all rows
     */
    private void clearAll() {
        tableModel.clearRows();
    }

    /**
     * Get headers from table
     */
    public List<Header> getHeaders() {
        return tableModel.getHeaders();
    }

    /**
     * Set headers
     */
    public void setHeaders(List<Header> headers) {
        tableModel.setHeaders(headers);
    }

    /**
     * Clear table
     */
    public void clear() {
        tableModel.clearRows();
    }

    /**
     * Set data change listener
     */
    public void setDataChangeListener(Runnable listener) {
        tableModel.addTableModelListener(e -> listener.run());
    }
}
