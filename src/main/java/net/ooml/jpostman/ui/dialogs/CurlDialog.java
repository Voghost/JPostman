package net.ooml.jpostman.ui.dialogs;

import net.ooml.jpostman.model.Request;
import net.ooml.jpostman.util.CurlGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Dialog for displaying and copying cURL commands
 */
public class CurlDialog extends JDialog {

    private JTextArea curlTextArea;
    private Request request;

    public CurlDialog(Frame parent, Request request) {
        super(parent, "cURL Command", true);
        this.request = request;
        initializeUI();
        updateCurlCommand();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(700, 400);
        setLocationRelativeTo(getParent());

        // Text area for cURL command
        curlTextArea = new JTextArea();
        curlTextArea.setEditable(false);
        curlTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        curlTextArea.setLineWrap(false);
        curlTextArea.setWrapStyleWord(false);

        JScrollPane scrollPane = new JScrollPane(curlTextArea);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyCurlToClipboard());
        buttonPanel.add(copyButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateCurlCommand() {
        if (request != null) {
            String curlCommand = CurlGenerator.generateCurl(request);
            curlTextArea.setText(curlCommand);
            curlTextArea.setCaretPosition(0);
        } else {
            curlTextArea.setText("No request available");
        }
    }

    private void copyCurlToClipboard() {
        String curlCommand = curlTextArea.getText();
        if (curlCommand != null && !curlCommand.isEmpty()) {
            StringSelection selection = new StringSelection(curlCommand);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);

            JOptionPane.showMessageDialog(this,
                    "cURL command copied to clipboard!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
