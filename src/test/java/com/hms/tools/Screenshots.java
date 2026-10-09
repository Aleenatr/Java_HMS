package com.hms.tools;

import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.security.PasswordHasher;
import com.hms.security.Session;
import com.hms.service.AuthService;
import com.hms.service.Services;
import com.hms.ui.LoginView;
import com.hms.ui.MainView;
import com.hms.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Renders the README screenshots without opening any window (headless), using the
 * test database's demo data. Not a test: run it by hand, see the README.
 */
public final class Screenshots {

    private static final int WIDTH = 1320;
    private static final int HEIGHT = 800;

    private Screenshots() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        Path out = Path.of(args.length > 0 ? args[0] : "docs/screenshots");
        Files.createDirectories(out);
        Theme.install();
        Database db = TestDatabase.fresh();
        PasswordHasher hasher = new PasswordHasher(1_000);
        AuthService auth = new AuthService(db, hasher);

        SwingUtilities.invokeAndWait(() -> {
            save(new LoginView(auth, session -> { }), 860, 500, out.resolve("login.png"));

            MainView admin = main(db, TestDatabase.admin(), auth, hasher);
            shoot(admin, "Dashboard", out.resolve("admin-dashboard.png"));
            shoot(admin, "Admissions", out.resolve("admin-admissions.png"));
            shoot(admin, "Pharmacy", out.resolve("admin-pharmacy.png"));
            shoot(admin, "Billing", out.resolve("admin-billing.png"));
            shoot(admin, "Users", out.resolve("admin-users.png"));

            MainView doctor = main(db, TestDatabase.drMehta(), auth, hasher);
            shoot(doctor, "My appointments", out.resolve("doctor-appointments.png"));
            shoot(doctor, "Medical records", out.resolve("doctor-records.png"));

            MainView patient = main(db, TestDatabase.priya(), auth, hasher);
            shoot(patient, "Dashboard", out.resolve("patient-dashboard.png"));
            shoot(patient, "My bills", out.resolve("patient-bills.png"));

            Services adminServices = Services.create(db, TestDatabase.admin(), auth, hasher);
            JTextArea bill = new JTextArea(adminServices.billing().printableBill(1));
            bill.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            bill.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            bill.setBackground(Color.WHITE);
            save(bill, 640, 560, out.resolve("bill.png"));
        });
        System.out.println("Screenshots written to " + out.toAbsolutePath());
    }

    private static MainView main(Database db, Session session, AuthService auth, PasswordHasher hasher) {
        return new MainView(Services.create(db, session, auth, hasher), () -> { });
    }

    private static void shoot(MainView view, String screen, Path file) {
        view.open(screen);
        save(view, WIDTH, HEIGHT, file);
    }

    /** Lays the component out at the given size and paints it into a PNG at 2x for sharp text. */
    private static void save(JComponent component, int width, int height, Path file) {
        component.setSize(width, height);
        component.addNotify();
        component.validate();
        BufferedImage image = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.scale(2, 2);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        component.paint(g);
        g.dispose();
        try {
            ImageIO.write(image, "png", file.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
