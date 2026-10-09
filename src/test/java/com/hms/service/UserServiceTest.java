package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.security.AccessDeniedException;
import com.hms.security.PasswordHasher;
import com.hms.security.Role;
import com.hms.security.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private Database db;
    private UserService asAdmin;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        PasswordHasher hasher = new PasswordHasher(1_000);
        asAdmin = new UserService(db, TestDatabase.admin(), hasher);
        auth = new AuthService(db, hasher);
    }

    @Test
    void newPatientLoginIsLinkedToTheirRecord() {
        asAdmin.createPatientLogin(3, "ananya.g", "Welcome123".toCharArray());
        Session session = auth.login("ananya.g", "Welcome123".toCharArray());
        assertEquals(Role.PATIENT, session.role());
        assertEquals(3, session.patientId());
        assertEquals("Ananya Gupta", session.user().displayName());
    }

    @Test
    void newDoctorLoginWorks() {
        asAdmin.createDoctorLogin(3, "dr.khan", "Welcome123".toCharArray());
        assertEquals(3, auth.login("dr.khan", "Welcome123".toCharArray()).doctorId());
    }

    @Test
    void secondLoginForTheSamePersonIsRolledBack() {
        int users = db.queryInt("SELECT COUNT(*) FROM users");
        HmsException error = assertThrows(HmsException.class,
                () -> asAdmin.createPatientLogin(1, "priya2", "Welcome123".toCharArray()));
        assertTrue(error.getMessage().contains("already has a login"));
        assertEquals(users, db.queryInt("SELECT COUNT(*) FROM users"), "no orphan account left behind");
    }

    @Test
    void usernamesAreUniqueAndPasswordsStrong() {
        HmsException taken = assertThrows(HmsException.class,
                () -> asAdmin.createAdmin("admin", "Welcome123".toCharArray()));
        assertEquals("The username admin is already taken.", taken.getMessage());
        assertThrows(ValidationException.class, () -> asAdmin.createAdmin("desk2", "short".toCharArray()));
    }

    @Test
    void disableAndResetPassword() {
        asAdmin.setActive(4, false);
        assertThrows(HmsException.class, () -> auth.login("priya", "Patient@123".toCharArray()));
        asAdmin.setActive(4, true);
        asAdmin.resetPassword(4, "Fresh1234".toCharArray());
        assertEquals(1, auth.login("priya", "Fresh1234".toCharArray()).patientId());
    }

    @Test
    void administratorCannotLockThemselvesOut() {
        assertThrows(HmsException.class, () -> asAdmin.setActive(1, false));
    }

    @Test
    void onlyAdministratorsManageAccounts() {
        UserService asDoctor = new UserService(db, TestDatabase.drMehta(), new PasswordHasher(1_000));
        assertThrows(AccessDeniedException.class, asDoctor::list);
        assertThrows(AccessDeniedException.class, () -> asDoctor.createAdmin("sneaky", "Welcome123".toCharArray()));
    }
}
