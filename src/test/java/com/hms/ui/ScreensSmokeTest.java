package com.hms.ui;

import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.security.PasswordHasher;
import com.hms.security.Session;
import com.hms.service.AuthService;
import com.hms.service.Services;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Builds the main window's content for each role and opens every screen, without a display
 * (headless). This catches screens that crash while loading, and checks each role sees the
 * right sidebar.
 */
class ScreensSmokeTest {

    private Database db;
    private PasswordHasher hasher;
    private AuthService auth;

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
        Theme.install();
    }

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        hasher = new PasswordHasher(1_000);
        auth = new AuthService(db, hasher);
    }

    private List<String> openEveryScreen(Session session) throws Exception {
        AtomicReference<List<String>> names = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            MainView view = new MainView(Services.create(db, session, auth, hasher), () -> { });
            for (String screen : view.screenNames()) {
                view.open(screen);
            }
            names.set(view.screenNames());
        });
        return names.get();
    }

    @Test
    void administratorScreens() throws Exception {
        assertEquals(List.of("Dashboard", "Patients", "Doctors", "Appointments", "Admissions", "Rooms",
                "Lab tests", "Pharmacy", "Billing", "Users"), openEveryScreen(TestDatabase.admin()));
    }

    @Test
    void doctorScreens() throws Exception {
        assertEquals(List.of("Dashboard", "Patients", "Doctors", "My appointments", "My inpatients",
                "Medical records", "Lab tests"), openEveryScreen(TestDatabase.drMehta()));
    }

    @Test
    void patientScreens() throws Exception {
        assertEquals(List.of("Dashboard", "Doctors", "My appointments", "My records", "My bills"),
                openEveryScreen(TestDatabase.priya()));
    }
}
