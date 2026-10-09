package com.hms.ui;

import com.hms.db.Database;
import com.hms.security.PasswordHasher;
import com.hms.service.AuthService;
import com.hms.service.Services;

import javax.swing.JFrame;
import java.awt.Dimension;

/** The main window after login. Logging out closes it and shows the sign-in window again. */
public final class MainFrame extends JFrame {

    public MainFrame(Database db, AuthService auth, PasswordHasher hasher, Services services) {
        super("Hospital Management System");
        setContentPane(new MainView(services, () -> {
            dispose();
            new LoginFrame(db, auth, hasher).setVisible(true);
        }));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(new Dimension(1320, 800));
        setLocationRelativeTo(null);
    }
}
