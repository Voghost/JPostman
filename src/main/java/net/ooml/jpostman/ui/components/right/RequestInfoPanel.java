package net.ooml.jpostman.ui.components.right;

import net.ooml.jpostman.model.Request;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Request Info Panel - Displays request metadata (ID, timestamps, etc.)
 */
public class RequestInfoPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JLabel idLabel;
    private JLabel createdLabel;
    private JLabel updatedLabel;

    public RequestInfoPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // ID
        JLabel idTitleLabel = new JLabel("ID:");
        idTitleLabel.setFont(idTitleLabel.getFont().deriveFont(Font.BOLD));
        add(idTitleLabel);

        idLabel = new JLabel("-");
        idLabel.setPreferredSize(new Dimension(280, 20)); // 增加宽度以显示更多ID字符
        add(idLabel);

        // Separator
        add(new JLabel("|"));

        // Created
        JLabel createdTitleLabel = new JLabel("Created:");
        createdTitleLabel.setFont(createdTitleLabel.getFont().deriveFont(Font.BOLD));
        add(createdTitleLabel);

        createdLabel = new JLabel("-");
        add(createdLabel);

        // Separator
        add(new JLabel("|"));

        // Updated
        JLabel updatedTitleLabel = new JLabel("Updated:");
        updatedTitleLabel.setFont(updatedTitleLabel.getFont().deriveFont(Font.BOLD));
        add(updatedTitleLabel);

        updatedLabel = new JLabel("-");
        add(updatedLabel);
    }

    /**
     * Update the displayed request info
     */
    public void setRequest(Request request) {
        if (request == null) {
            idLabel.setText("-");
            createdLabel.setText("-");
            updatedLabel.setText("-");
        } else {
            // ID (show full ID, it will be truncated by label width if too long)
            String id = request.getId();
            idLabel.setText(id != null ? id : "-");
            idLabel.setToolTipText(id); // Tooltip shows full ID on hover

            // Created
            if (request.getCreatedAt() != null) {
                createdLabel.setText(request.getCreatedAt().format(DATE_FORMATTER));
            } else {
                createdLabel.setText("-");
            }

            // Updated
            if (request.getUpdatedAt() != null) {
                updatedLabel.setText(request.getUpdatedAt().format(DATE_FORMATTER));
            } else {
                updatedLabel.setText("-");
            }
        }
    }

    /**
     * Clear the displayed info
     */
    public void clear() {
        setRequest(null);
    }

    /**
     * Refresh UI after language change
     */
    public void refreshUI() {
        // RequestInfoPanel displays data, not localized text, so no update needed
        revalidate();
        repaint();
    }
}
