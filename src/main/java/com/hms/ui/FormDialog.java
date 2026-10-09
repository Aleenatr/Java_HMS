package com.hms.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.hms.HmsException;
import com.hms.service.Validator;
import com.hms.util.Formats;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;

/**
 * Builds a simple "label: field" form and shows it in a modal dialog.
 *
 * <pre>
 * FormDialog form = new FormDialog(this, "Add patient");
 * JTextField name = form.text("Name", "");
 * if (form.show("Save", () -> service.create(...))) { refresh(); }
 * </pre>
 *
 * If the save action throws (say, a ValidationException), the message appears in red
 * and the dialog stays open so the user can fix the field.
 */
public final class FormDialog {

    private final Component parent;
    private final String title;
    private final JPanel form = new JPanel(new GridBagLayout());
    private final JLabel error = new JLabel(" ");
    private int row;

    public FormDialog(Component parent, String title) {
        this.parent = parent;
        this.title = title;
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 6, 22));
        error.setForeground(Theme.DANGER);
    }

    public JTextField text(String label, String value) {
        return add(label, new JTextField(value == null ? "" : value, 24));
    }

    /** A date typed as DD-MM-YYYY; read it back with Validator.parseDate. */
    public JTextField date(String label, LocalDate value) {
        JTextField field = text(label, Formats.date(value));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "DD-MM-YYYY");
        return field;
    }

    public JPasswordField password(String label) {
        return add(label, new JPasswordField(24));
    }

    public <E> JComboBox<E> combo(String label, List<E> items, E selected) {
        JComboBox<E> combo = new JComboBox<>(new java.util.Vector<>(items));
        combo.setSelectedItem(selected);
        if (selected == null && !items.isEmpty()) {
            combo.setSelectedIndex(0);
        }
        return add(label, combo);
    }

    public JTextArea area(String label, String value, int rows) {
        JTextArea area = new JTextArea(value == null ? "" : value, rows, 24);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        add(label, new JScrollPane(area));
        return area;
    }

    /** A grey hint line under the fields above it. */
    public void note(String text) {
        JLabel note = new JLabel("<html><div style='width:330px'>" + escapeHtml(text) + "</div></html>");
        note.setForeground(Theme.MUTED_TEXT);
        GridBagConstraints c = constraints(1, row++);
        c.insets = new Insets(0, 0, 10, 0);
        form.add(note, c);
    }

    /** Adds any component as a labelled row. */
    public <C extends JComponent> C add(String label, C component) {
        JLabel caption = new JLabel(label);
        GridBagConstraints left = constraints(0, row);
        left.anchor = component instanceof JScrollPane ? GridBagConstraints.NORTHEAST : GridBagConstraints.EAST;
        left.insets = new Insets(component instanceof JScrollPane ? 6 : 0, 0, 10, 12);
        form.add(caption, left);
        GridBagConstraints right = constraints(1, row++);
        right.insets = new Insets(0, 0, 10, 0);
        form.add(component, right);
        return component;
    }

    private static GridBagConstraints constraints(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.fill = x == 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.weightx = x == 1 ? 1 : 0;
        return c;
    }

    /** Labels wrap text with HTML; this keeps a stray "<" in user data from being read as a tag. */
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** The combo box's chosen item, or a "Please choose a ..." message if the list was empty. */
    @SuppressWarnings("unchecked")
    public static <E> E selected(JComboBox<E> combo, String field) {
        return Validator.requireSelected(field, (E) combo.getSelectedItem());
    }

    /** The form without a window, used to render screenshots. */
    public JPanel panel() {
        return form;
    }

    /** Shows the dialog and returns true once onSave has run without an error. */
    public boolean show(String saveLabel, Runnable onSave) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                Dialog.ModalityType.APPLICATION_MODAL);
        boolean[] saved = {false};

        JButton save = new JButton(saveLabel);
        Ui.stylePrimary(save);
        save.addActionListener(e -> {
            try {
                onSave.run();
                saved[0] = true;
                dialog.dispose();
            } catch (HmsException ex) {
                error.setText("<html><div style='width:380px'>" + escapeHtml(ex.getMessage()) + "</div></html>");
                dialog.pack();
            }
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancel);
        buttons.add(save);
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 22, 16, 22));
        bottom.add(error, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(save);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return saved[0];
    }
}
