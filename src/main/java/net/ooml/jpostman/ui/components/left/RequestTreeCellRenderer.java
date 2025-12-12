package net.ooml.jpostman.ui.components.left;

import net.ooml.jpostman.model.Collection;
import net.ooml.jpostman.model.Request;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom tree cell renderer for displaying requests with name and date
 */
public class RequestTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                   boolean sel, boolean expanded,
                                                   boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof Request) {
                Request request = (Request) userObject;
                String displayText = formatRequestDisplay(request);
                setText(displayText);
            } else if (userObject instanceof Collection) {
                Collection collection = (Collection) userObject;
                String displayText = formatCollectionDisplay(collection);
                setText(displayText);
            }
        }

        return this;
    }

    /**
     * Format request display: Name only
     */
    private String formatRequestDisplay(Request request) {
        return request.getName();
    }

    /**
     * Format collection display: Name only
     */
    private String formatCollectionDisplay(Collection collection) {
        return collection.getName();
    }
}
