package com.hms.security;

import com.hms.model.User;

/**
 * Who is logged in. doctorId is set only for doctors and patientId only for patients;
 * services use them to limit doctors and patients to their own data.
 */
public record Session(User user, Integer doctorId, Integer patientId) {

    public Role role() {
        return user.role();
    }

    public boolean can(Permission permission) {
        return role().can(permission);
    }

    /** Every service method starts with this check. */
    public void require(Permission permission) {
        if (!can(permission)) {
            throw new AccessDeniedException("Access denied: a " + role() + " cannot " + permission.description() + ".");
        }
    }

    public boolean isAdmin() {
        return role() == Role.ADMIN;
    }

    public boolean isDoctor() {
        return role() == Role.DOCTOR;
    }

    public boolean isPatient() {
        return role() == Role.PATIENT;
    }
}
