package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.security.PasswordHasher;
import com.hms.security.Role;
import com.hms.security.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private Database db;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        auth = new AuthService(db, new PasswordHasher(1_000));
    }

    @Test
    void eachDemoRoleCanLogIn() {
        Session admin = auth.login("admin", "Admin@123".toCharArray());
        assertEquals(Role.ADMIN, admin.role());
        assertNull(admin.doctorId());

        Session doctor = auth.login("dr.mehta", "Doctor@123".toCharArray());
        assertEquals(Role.DOCTOR, doctor.role());
        assertEquals(1, doctor.doctorId());
        assertEquals("Dr. Arjun Mehta", doctor.user().displayName());

        Session patient = auth.login("priya", "Patient@123".toCharArray());
        assertEquals(Role.PATIENT, patient.role());
        assertEquals(1, patient.patientId());
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameMessage() {
        HmsException wrongPassword = assertThrows(HmsException.class,
                () -> auth.login("admin", "nope".toCharArray()));
        HmsException unknownUser = assertThrows(HmsException.class,
                () -> auth.login("nobody", "nope".toCharArray()));
        assertEquals(wrongPassword.getMessage(), unknownUser.getMessage());
    }

    @Test
    void sqlInjectionDoesNotLogYouIn() {
        // The original project built its login query by string concatenation, so this got in.
        assertThrows(HmsException.class, () -> auth.login("admin' -- ", "x".toCharArray()));
        assertThrows(HmsException.class, () -> auth.login("' OR '1'='1", "' OR '1'='1".toCharArray()));
    }

    @Test
    void passwordArrayIsWipedAfterLogin() {
        char[] password = "Admin@123".toCharArray();
        auth.login("admin", password);
        assertArrayEquals(new char[9], password);
    }

    @Test
    void disabledAccountCannotLogIn() {
        db.update("UPDATE users SET active = FALSE WHERE username = 'priya'");
        HmsException error = assertThrows(HmsException.class, () -> auth.login("priya", "Patient@123".toCharArray()));
        assertEquals("This account has been disabled. Please contact the administrator.", error.getMessage());
    }

    @Test
    void changePasswordReplacesTheOldOne() {
        Session priya = auth.login("priya", "Patient@123".toCharArray());
        auth.changePassword(priya, "Patient@123".toCharArray(), "NewPass99".toCharArray(), "NewPass99".toCharArray());
        assertThrows(HmsException.class, () -> auth.login("priya", "Patient@123".toCharArray()));
        assertEquals(1, auth.login("priya", "NewPass99".toCharArray()).patientId());
    }

    @Test
    void changePasswordChecksCurrentPasswordAndStrength() {
        Session priya = auth.login("priya", "Patient@123".toCharArray());
        assertThrows(ValidationException.class, () -> auth.changePassword(priya,
                "wrong".toCharArray(), "NewPass99".toCharArray(), "NewPass99".toCharArray()));
        assertThrows(ValidationException.class, () -> auth.changePassword(priya,
                "Patient@123".toCharArray(), "NewPass99".toCharArray(), "Different1".toCharArray()));
        assertThrows(ValidationException.class, () -> auth.changePassword(priya,
                "Patient@123".toCharArray(), "short1".toCharArray(), "short1".toCharArray()));
    }
}
