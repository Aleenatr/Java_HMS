package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.Gender;
import com.hms.model.Patient;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientServiceTest {

    private Database db;
    private PatientService asAdmin;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        asAdmin = new PatientService(db, TestDatabase.admin());
    }

    private static Patient newPatient(String name, String phone) {
        return new Patient(0, name, Gender.FEMALE, LocalDate.of(1995, 3, 1), phone, "", "", null);
    }

    @Test
    void adminRegistersAndFindsAPatient() {
        int id = asAdmin.create(newPatient("  Meera Nair ", "+91 99887 76655"));
        Patient saved = asAdmin.get(id);
        assertEquals("Meera Nair", saved.name(), "text is trimmed");
        assertEquals("9988776655", saved.phone(), "phone is normalised");
        assertEquals(null, saved.email(), "blank optional fields become NULL");
        assertEquals(1, asAdmin.list("meera").size());
        assertEquals(1, asAdmin.list("99887").size());
    }

    @Test
    void invalidInputIsRejectedBeforeReachingTheDatabase() {
        int before = db.queryInt("SELECT COUNT(*) FROM patients");
        assertThrows(ValidationException.class, () -> asAdmin.create(newPatient("Meera", "123")));
        assertThrows(ValidationException.class, () -> asAdmin.create(newPatient("", "9988776655")));
        assertThrows(ValidationException.class, () -> asAdmin.create(new Patient(0, "Meera", Gender.FEMALE,
                LocalDate.now().plusDays(1), "9988776655", null, null, null)));
        assertEquals(before, db.queryInt("SELECT COUNT(*) FROM patients"));
    }

    @Test
    void searchTreatsWildcardsAsText() {
        assertEquals(0, asAdmin.list("%").size());
        assertEquals(0, asAdmin.list("_").size());
    }

    @Test
    void updateChangesTheRecord() {
        Patient priya = asAdmin.get(1);
        asAdmin.update(new Patient(1, priya.name(), priya.gender(), priya.dateOfBirth(), "9000000001",
                "new@example.com", "New address", priya.userId()));
        assertEquals("9000000001", asAdmin.get(1).phone());
        assertEquals(4, asAdmin.get(1).userId(), "the login link is untouched");
    }

    @Test
    void patientWithHistoryCannotBeDeleted() {
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.delete(1));
        assertTrue(error.getMessage().contains("can't be deleted"));
        assertEquals("Priya Sharma", asAdmin.get(1).name());
    }

    @Test
    void newPatientWithoutHistoryCanBeDeleted() {
        int id = asAdmin.create(newPatient("Wrong Entry", "9988776655"));
        asAdmin.delete(id);
        assertThrows(HmsException.class, () -> asAdmin.get(id));
    }

    @Test
    void doctorsCanViewButNotEdit() {
        PatientService asDoctor = new PatientService(db, TestDatabase.drMehta());
        assertEquals(8, asDoctor.list(null).size());
        assertThrows(AccessDeniedException.class, () -> asDoctor.create(newPatient("Meera", "9988776655")));
        assertThrows(AccessDeniedException.class, () -> asDoctor.delete(8));
    }

    @Test
    void patientsSeeOnlyThemselves() {
        PatientService asPriya = new PatientService(db, TestDatabase.priya());
        assertEquals("Priya Sharma", asPriya.get(1).name());
        assertThrows(AccessDeniedException.class, () -> asPriya.get(2));
        assertThrows(AccessDeniedException.class, () -> asPriya.list(null));
    }
}
