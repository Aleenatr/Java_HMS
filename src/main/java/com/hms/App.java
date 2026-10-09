package com.hms;

import com.hms.config.DbConfig;
import com.hms.db.Database;
import com.hms.db.SchemaInitializer;
import com.hms.security.PasswordHasher;
import com.hms.service.AuthService;
import com.hms.ui.Dialogs;
import com.hms.ui.LoginFrame;
import com.hms.ui.Theme;

import javax.swing.SwingUtilities;

/**
 * Starts the application: read the database settings, create the tables if needed,
 * then show the sign-in window.
 *
 * <pre>
 * UI (com.hms.ui)  ->  services (com.hms.service)  ->  DAOs (com.hms.dao)  ->  MySQL
 * </pre>
 * The UI never runs SQL; services check permissions and validate input; DAOs hold all SQL.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Theme.install();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            SwingUtilities.invokeLater(() -> Dialogs.error(null, "Something unexpected went wrong ("
                    + error.getClass().getSimpleName() + "). The details were printed to the console."));
        });
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        Database db = new Database(DbConfig.load());
        try {
            db.checkConnection();
            new SchemaInitializer(db).initialize();
        } catch (HmsException e) {
            Dialogs.error(null, "Could not open the database.\n\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running and that db.properties has the right"
                    + "\nusername and password (copy db.properties.example to start).");
            System.exit(1);
        }
        PasswordHasher hasher = new PasswordHasher();
        new LoginFrame(db, new AuthService(db, hasher), hasher).setVisible(true);
    }
}
