package com.hms.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Small message boxes used by every screen. */
public final class Dialogs {

    private Dialogs() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Can't do that", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Shows fixed-width text (a bill) with Print and Save buttons. Print opens the system print dialog. */
    public static void showDocument(Component parent, String title, String text, String suggestedFileName) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                Dialog.ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JButton print = new JButton("Print");
        print.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                error(dialog, "Printing failed: " + ex.getMessage());
            }
        });
        JButton save = new JButton("Save as text file");
        save.addActionListener(e -> saveText(dialog, text, suggestedFileName));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        buttons.add(print);
        buttons.add(save);
        buttons.add(close);
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 640), Math.min(Math.max(dialog.getHeight(), 420), 720));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void saveText(Component parent, String text, String suggestedFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(suggestedFileName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(chooser.getSelectedFile().toPath(), text, StandardCharsets.UTF_8);
            info(parent, "Saved to " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            error(parent, "Could not save the file: " + ex.getMessage());
        }
    }
}
