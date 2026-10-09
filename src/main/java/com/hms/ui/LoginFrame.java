package com.hms.ui;

import com.hms.db.Database;
import com.hms.security.PasswordHasher;
import com.hms.security.Session;
import com.hms.service.AuthService;
import com.hms.service.Services;

import javax.swing.JFrame;
import java.awt.Dimension;

/** The sign-in window. A successful login opens the main window with that user's services. */
public final class LoginFrame extends JFrame {

    public LoginFrame(Database db, AuthService auth, PasswordHasher hasher) {
        super("Hospital Management System - Sign in");
        LoginView view = new LoginView(auth, session -> openMainWindow(db, auth, hasher, session));
        setContentPane(view);
        getRootPane().setDefaultButton(view.signInButton());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(new Dimension(860, 500));
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void openMainWindow(Database db, AuthService auth, PasswordHasher hasher, Session session) {
        Services services = Services.create(db, session, auth, hasher);
        new MainFrame(db, auth, hasher, services).setVisible(true);
        dispose();
    }
}
