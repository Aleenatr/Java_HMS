package com.hms.ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Small factory methods so every screen's buttons and labels look the same. */
public final class Ui {

    private Ui() {
    }

    public static JButton button(String text, Runnable onClick) {
        JButton button = new JButton(text);
        button.addActionListener(e -> onClick.run());
        return button;
    }

    public static JButton primaryButton(String text, Runnable onClick) {
        JButton button = button(text, onClick);
        stylePrimary(button);
        return button;
    }

    public static void stylePrimary(JButton button) {
        button.putClientProperty(FlatClientProperties.STYLE,
                "background: #0F766E; foreground: #FFFFFF; hoverBackground: #115E59; "
                        + "pressedBackground: #134E4A; borderWidth: 0; focusWidth: 0; font: bold");
    }

    public static JButton dangerButton(String text, Runnable onClick) {
        JButton button = button(text, onClick);
        button.putClientProperty(FlatClientProperties.STYLE, "foreground: #B91C1C");
        return button;
    }

    public static JLabel heading(String text, String styleClass) {
        JLabel label = new JLabel(text);
        label.putClientProperty(FlatClientProperties.STYLE_CLASS, styleClass);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    /** Runs the action after every keystroke, paste or delete in the field. */
    public static void onTextChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    public static JTextField searchField(String placeholder, Runnable onEnter) {
        JTextField field = new JTextField(22);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        field.addActionListener(e -> onEnter.run());
        return field;
    }
}
