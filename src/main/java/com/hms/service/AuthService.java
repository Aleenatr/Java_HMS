package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.DoctorDao;
import com.hms.dao.PatientDao;
import com.hms.dao.UserDao;
import com.hms.db.Database;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.security.PasswordHasher;
import com.hms.security.Session;

import java.util.Arrays;
import java.util.Optional;

/** Logging in and changing your own password. */
public final class AuthService {

    private static final String WRONG_LOGIN = "Invalid username or password.";

    private final UserDao users;
    private final DoctorDao doctors;
    private final PatientDao patients;
    private final PasswordHasher hasher;
    // When the username doesn't exist we still check the password against this hash, so a wrong
    // username takes as long as a wrong password and nobody can probe which usernames exist.
    private final String decoyHash;

    public AuthService(Database db, PasswordHasher hasher) {
        this.users = new UserDao(db);
        this.doctors = new DoctorDao(db);
        this.patients = new PatientDao(db);
        this.hasher = hasher;
        this.decoyHash = hasher.hash("decoy-password-1".toCharArray());
    }

    /** Checks the password and returns the session. The password array is wiped afterwards. */
    public Session login(String username, char[] password) {
        try {
            if (username == null || username.isBlank() || password.length == 0) {
                throw new ValidationException("Enter your username and password.");
            }
            Optional<UserDao.Credentials> found = users.findCredentials(username.strip());
            if (found.isEmpty()) {
                hasher.verify(password, decoyHash);
                throw new HmsException(WRONG_LOGIN);
            }
            UserDao.Credentials credentials = found.get();
            if (!hasher.verify(password, credentials.passwordHash())) {
                throw new HmsException(WRONG_LOGIN);
            }
            if (!credentials.user().active()) {
                throw new HmsException("This account has been disabled. Please contact the administrator.");
            }
            return sessionFor(credentials.user());
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private Session sessionFor(User user) {
        return switch (user.role()) {
            case ADMIN -> new Session(user, null, null);
            case DOCTOR -> new Session(user, doctors.findByUserId(user.id()).map(Doctor::id)
                    .orElseThrow(() -> notLinked("doctor")), null);
            case PATIENT -> new Session(user, null, patients.findByUserId(user.id()).map(Patient::id)
                    .orElseThrow(() -> notLinked("patient")));
        };
    }

    private static HmsException notLinked(String profile) {
        return new HmsException("This login is not linked to a " + profile + " profile. Please contact the administrator.");
    }

    public void changePassword(Session session, char[] current, char[] newPassword, char[] confirmation) {
        try {
            String stored = users.findCredentials(session.user().username())
                    .map(UserDao.Credentials::passwordHash)
                    .orElseThrow(() -> new HmsException("Your account no longer exists."));
            if (!hasher.verify(current, stored)) {
                throw new ValidationException("Your current password is not correct.");
            }
            if (!Arrays.equals(newPassword, confirmation)) {
                throw new ValidationException("The new passwords don't match.");
            }
            Validator.requireStrongPassword(newPassword);
            users.updatePasswordHash(session.user().id(), hasher.hash(newPassword));
        } finally {
            Arrays.fill(current, '\0');
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }
}
