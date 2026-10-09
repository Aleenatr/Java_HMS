package com.hms.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.hms.HmsException;
import com.hms.security.Session;
import com.hms.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/** The sign-in screen. On success it hands the new Session to onLogin. */
public final class LoginView extends JPanel {

    private final AuthService auth;
    private final Consumer<Session> onLogin;
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JLabel error = new JLabel(" ");
    private final JButton signIn = Ui.primaryButton("Sign in", this::signIn);

    public LoginView(AuthService auth, Consumer<Session> onLogin) {
        super(new GridLayout(1, 2));
        this.auth = auth;
        this.onLogin = onLogin;
        add(brandPanel());
        add(formPanel());
    }

    public JButton signInButton() {
        return signIn;
    }

    private JPanel brandPanel() {
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(Theme.ACCENT);
        JPanel text = new JPanel(new GridLayout(0, 1, 0, 8));
        text.setOpaque(false);
        JLabel title = Ui.heading("<html>Hospital<br>Management System</html>", "h1");
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("<html>Patients, doctors, appointments,<br>rooms, pharmacy, lab and billing.</html>");
        subtitle.setForeground(Theme.ACCENT_LIGHT);
        text.add(title);
        text.add(subtitle);
        brand.add(text);
        return brand;
    }

    private JPanel formPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        username.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username");
        password.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        password.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        username.addActionListener(e -> password.requestFocusInWindow());
        password.addActionListener(e -> signIn());
        signIn.setPreferredSize(new Dimension(signIn.getPreferredSize().width, 36));
        error.setForeground(Theme.DANGER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 6, 0);
        form.add(Ui.heading("Sign in", "h2"), c);
        c.insets = new Insets(0, 0, 22, 0);
        form.add(Ui.muted("Use the account given to you by the hospital."), c);
        c.insets = new Insets(0, 0, 4, 0);
        form.add(new JLabel("Username"), c);
        c.insets = new Insets(0, 0, 12, 0);
        form.add(username, c);
        c.insets = new Insets(0, 0, 4, 0);
        form.add(new JLabel("Password"), c);
        c.insets = new Insets(0, 0, 18, 0);
        form.add(password, c);
        form.add(signIn, c);
        c.insets = new Insets(0, 0, 0, 0);
        form.add(error, c);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void signIn() {
        try {
            Session session = auth.login(username.getText(), password.getPassword());
            error.setText(" ");
            password.setText("");
            onLogin.accept(session);
        } catch (HmsException e) {
            error.setText(e.getMessage());
            password.setText("");
            password.requestFocusInWindow();
        }
    }
}
