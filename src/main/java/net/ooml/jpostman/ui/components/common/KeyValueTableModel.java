package net.ooml.jpostman.ui.components.common;

import net.ooml.jpostman.model.Header;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for key-value pairs (headers, query params, etc.)
 */
public class KeyValueTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Enabled", "Key", "Value", "Description"};
    private final List<Row> rows;

    public static class Row {
        public boolean enabled;
        public String key;
        public String value;
        public String description;

        public Row() {
            this.enabled = true;
            this.key = "";
            this.value = "";
            this.description = "";
        }

        public Row(boolean enabled, String key, String value, String description) {
            this.enabled = enabled;
            this.key = key;
            this.value = value;
            this.description = description;
        }

        public static Row fromHeader(Header header) {
            return new Row(
                    header.getEnabled() != null ? header.getEnabled() : true,
                    header.getKey(),
                    header.getValue(),
                    header.getDescription()
            );
        }

        public Header toHeader() {
            return Header.builder()
                    .key(key)
                    .value(value)
                    .description(description)
                    .enabled(enabled)
                    .build();
        }
    }

    public KeyValueTableModel() {
        this.rows = new ArrayList<>();
        addEmptyRow();
    }

    public KeyValueTableModel(List<Header> headers) {
        this.rows = new ArrayList<>();
        if (headers != null) {
            for (Header header : headers) {
                rows.add(Row.fromHeader(header));
            }
        }
        addEmptyRow();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= rows.size()) {
            return null;
        }

        Row row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.enabled;
            case 1:
                return row.key;
            case 2:
                return row.value;
            case 3:
                return row.description;
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= rows.size()) {
            return;
        }

        Row row = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                row.enabled = (Boolean) aValue;
                break;
            case 1:
                row.key = (String) aValue;
                break;
            case 2:
                row.value = (String) aValue;
                break;
            case 3:
                row.description = (String) aValue;
                break;
        }

        // If last row is edited, add a new empty row
        if (rowIndex == rows.size() - 1 && !row.key.isEmpty()) {
            addEmptyRow();
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Add empty row at the end
     */
    private void addEmptyRow() {
        rows.add(new Row());
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    /**
     * Add a row
     */
    public void addRow(Row row) {
        int lastIndex = rows.size() - 1;
        rows.add(lastIndex, row);
        fireTableRowsInserted(lastIndex, lastIndex);
    }

    /**
     * Remove a row
     */
    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size() - 1) { // Don't remove last empty row
            rows.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    /**
     * Clear all rows except the empty one
     */
    public void clearRows() {
        int size = rows.size();
        rows.clear();
        addEmptyRow();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }

    /**
     * Get all headers (excluding empty rows)
     */
    public List<Header> getHeaders() {
        List<Header> headers = new ArrayList<>();
        for (Row row : rows) {
            if (!row.key.isEmpty()) {
                headers.add(row.toHeader());
            }
        }
        return headers;
    }

    /**
     * Set headers
     */
    public void setHeaders(List<Header> headers) {
        clearRows();
        if (headers != null) {
            for (Header header : headers) {
                addRow(Row.fromHeader(header));
            }
        }
    }

    public List<Row> getRows() {
        return rows;
    }
}
