package com.hms.security;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.hms.security.Permission.*;

/**
 * The three user roles and what each may do (role-based access control).
 *
 * <p>Administrators run the hospital desk but cannot read clinical notes.
 * Doctors handle care. Patients see only their own data; the services
 * apply that "own data only" rule in their SQL.
 */
public enum Role {
    ADMIN("Administrator", EnumSet.of(
            VIEW_PATIENTS, MANAGE_PATIENTS, MANAGE_DOCTORS, MANAGE_ROOMS, MANAGE_ADMISSIONS,
            BOOK_APPOINTMENTS, COMPLETE_APPOINTMENTS, RECORD_LAB_RESULTS, VIEW_LAB_TESTS,
            MANAGE_PHARMACY, MANAGE_BILLING, VIEW_BILLS, MANAGE_USERS)),

    DOCTOR("Doctor", EnumSet.of(
            VIEW_PATIENTS, COMPLETE_APPOINTMENTS, WRITE_MEDICAL_RECORDS, VIEW_MEDICAL_RECORDS,
            ORDER_LAB_TESTS, RECORD_LAB_RESULTS, VIEW_LAB_TESTS)),

    PATIENT("Patient", EnumSet.of(
            BOOK_APPOINTMENTS, VIEW_MEDICAL_RECORDS, VIEW_LAB_TESTS, VIEW_BILLS));

    private final String label;
    private final Set<Permission> permissions;

    Role(String label, Set<Permission> permissions) {
        this.label = label;
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public boolean can(Permission permission) {
        return permissions.contains(permission);
    }

    public Set<Permission> permissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return label;
    }
}
