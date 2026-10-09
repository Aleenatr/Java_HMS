package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.DoctorDao;
import com.hms.dao.PatientDao;
import com.hms.dao.UserDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.User;
import com.hms.security.PasswordHasher;
import com.hms.security.Role;
import com.hms.security.Session;

import java.util.Arrays;
import java.util.List;

import static com.hms.security.Permission.MANAGE_USERS;

/** Login accounts: the administrator creates them and assigns the role. */
public final class UserService {

    private final Database db;
    private final Session session;
    private final UserDao users;
    private final PasswordHasher hasher;

    public UserService(Database db, Session session, PasswordHasher hasher) {
        this.db = db;
        this.session = session;
        this.users = new UserDao(db);
        this.hasher = hasher;
    }

    public List<User> list() {
        session.require(MANAGE_USERS);
        return users.findAll();
    }

    public int createAdmin(String username, char[] password) {
        return createLogin(Role.ADMIN, 0, username, password);
    }

    public int createDoctorLogin(int doctorId, String username, char[] password) {
        return createLogin(Role.DOCTOR, doctorId, username, password);
    }

    public int createPatientLogin(int patientId, String username, char[] password) {
        return createLogin(Role.PATIENT, patientId, username, password);
    }

    /**
     * One transaction: create the account, then link it to the doctor or patient profile.
     * If the profile already has a login, the link fails and the new account is rolled back.
     */
    private int createLogin(Role role, int profileId, String username, char[] password) {
        session.require(MANAGE_USERS);
        try {
            String cleanUsername = Validator.requireUsername(username);
            Validator.requireStrongPassword(password);
            String hash = hasher.hash(password);
            return db.transaction(tx -> {
                int userId = new UserDao(tx).insert(cleanUsername, hash, role);
                boolean linked = switch (role) {
                    case ADMIN -> true;
                    case DOCTOR -> new DoctorDao(tx).linkUser(profileId, userId);
                    case PATIENT -> new PatientDao(tx).linkUser(profileId, userId);
                };
                if (!linked) {
                    throw new HmsException("This " + role.toString().toLowerCase() + " already has a login.");
                }
                return userId;
            });
        } catch (DataAccessException e) {
            if (e.isDuplicateKey()) {
                throw new HmsException("The username " + username.strip() + " is already taken.");
            }
            throw e;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public void resetPassword(int userId, char[] newPassword) {
        session.require(MANAGE_USERS);
        try {
            Validator.requireStrongPassword(newPassword);
            if (!users.updatePasswordHash(userId, hasher.hash(newPassword))) {
                throw new HmsException("User #" + userId + " was not found.");
            }
        } finally {
            Arrays.fill(newPassword, '\0');
        }
    }

    /** A disabled account can't log in. You can't disable your own account. */
    public void setActive(int userId, boolean active) {
        session.require(MANAGE_USERS);
        if (!active && userId == session.user().id()) {
            throw new HmsException("You can't disable your own account.");
        }
        if (!users.setActive(userId, active)) {
            throw new HmsException("User #" + userId + " was not found.");
        }
    }
}
