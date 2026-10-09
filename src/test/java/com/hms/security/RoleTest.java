package com.hms.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.hms.security.Permission.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    void administratorRunsTheDeskButCannotReadClinicalNotes() {
        assertTrue(Role.ADMIN.can(MANAGE_PATIENTS));
        assertTrue(Role.ADMIN.can(MANAGE_BILLING));
        assertTrue(Role.ADMIN.can(MANAGE_USERS));
        assertFalse(Role.ADMIN.can(VIEW_MEDICAL_RECORDS));
        assertFalse(Role.ADMIN.can(WRITE_MEDICAL_RECORDS));
    }

    @Test
    void doctorHandlesCareButNotMoneyOrAccounts() {
        assertTrue(Role.DOCTOR.can(WRITE_MEDICAL_RECORDS));
        assertTrue(Role.DOCTOR.can(ORDER_LAB_TESTS));
        assertFalse(Role.DOCTOR.can(MANAGE_BILLING));
        assertFalse(Role.DOCTOR.can(MANAGE_PATIENTS));
        assertFalse(Role.DOCTOR.can(MANAGE_USERS));
    }

    @Test
    void patientCanBookAndReadButNotSeeOtherPatients() {
        assertTrue(Role.PATIENT.can(BOOK_APPOINTMENTS));
        assertTrue(Role.PATIENT.can(VIEW_BILLS));
        assertFalse(Role.PATIENT.can(VIEW_PATIENTS));
        assertFalse(Role.PATIENT.can(MANAGE_BILLING));
        assertFalse(Role.PATIENT.can(WRITE_MEDICAL_RECORDS));
    }

    @Test
    void everyPermissionBelongsToSomeRole() {
        for (Permission permission : Permission.values()) {
            assertTrue(Arrays.stream(Role.values()).anyMatch(role -> role.can(permission)),
                    permission + " is not granted to any role");
        }
    }
}
