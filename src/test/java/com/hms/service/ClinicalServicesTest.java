package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.LabTest;
import com.hms.model.LabTestStatus;
import com.hms.model.MedicalRecord;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Medical records and lab tests: who may read and write clinical data. */
class ClinicalServicesTest {

    private Database db;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
    }

    @Test
    void doctorsWriteNotesSignedInTheirOwnName() {
        MedicalRecordService asRao = new MedicalRecordService(db, TestDatabase.drRao());
        asRao.add(3, LocalDate.now(), "Seasonal allergy", "Cetirizine 5 ml at night", null);
        MedicalRecord latest = asRao.forPatient(3).get(0);
        assertEquals("Dr. Kavita Rao", latest.doctorName());
        assertEquals("Seasonal allergy", latest.diagnosis());
        assertThrows(ValidationException.class,
                () -> asRao.add(3, LocalDate.now().plusDays(1), "Future visit", null, null));
    }

    @Test
    void administratorsCannotReadClinicalNotes() {
        MedicalRecordService asAdmin = new MedicalRecordService(db, TestDatabase.admin());
        assertThrows(AccessDeniedException.class, () -> asAdmin.forPatient(1));
        assertThrows(AccessDeniedException.class, () -> asAdmin.add(1, LocalDate.now(), "x", null, null));
    }

    @Test
    void patientsReadOnlyTheirOwnRecords() {
        MedicalRecordService asPriya = new MedicalRecordService(db, TestDatabase.priya());
        List<MedicalRecord> own = asPriya.forPatient(1);
        assertEquals(1, own.size());
        assertThrows(AccessDeniedException.class, () -> asPriya.forPatient(2));
        assertThrows(AccessDeniedException.class, () -> asPriya.add(1, LocalDate.now(), "Self-diagnosis", null, null));
    }

    @Test
    void orderedTestKeepsThePriceFromOrderTime() {
        LabService asMehta = new LabService(db, TestDatabase.drMehta());
        int testId = asMehta.order(7, 2);
        db.update("UPDATE lab_test_types SET price = 5000 WHERE id = 2");
        LabTest test = asMehta.list(false).stream().filter(t -> t.id() == testId).findFirst().orElseThrow();
        assertEquals(new BigDecimal("600.00"), test.price());
        assertEquals("Dr. Arjun Mehta", test.doctorName());
        assertEquals(LabTestStatus.ORDERED, test.status());
    }

    @Test
    void resultIsRecordedOnce() {
        LabService asAdmin = new LabService(db, TestDatabase.admin());
        int pending = asAdmin.list(true).get(0).id();
        asAdmin.recordResult(pending, "Within normal limits");
        assertThrows(HmsException.class, () -> asAdmin.recordResult(pending, "Changed my mind"));
        assertThrows(ValidationException.class, () -> asAdmin.recordResult(pending, " "));
    }

    @Test
    void doctorsRecordResultsOnlyForTestsTheyOrdered() {
        int mehtasTest = new LabService(db, TestDatabase.drMehta()).list(true).get(0).id();
        assertThrows(AccessDeniedException.class,
                () -> new LabService(db, TestDatabase.drRao()).recordResult(mehtasTest, "Normal"));
        new LabService(db, TestDatabase.drMehta()).recordResult(mehtasTest, "Normal");
    }

    @Test
    void labListsAreScopedByRole() {
        assertEquals(6, new LabService(db, TestDatabase.admin()).list(false).size());
        assertTrue(new LabService(db, TestDatabase.drMehta()).list(false).stream().allMatch(t -> t.doctorId() == 1));
        LabService asPriya = new LabService(db, TestDatabase.priya());
        assertTrue(asPriya.list(false).stream().allMatch(t -> t.patientId() == 1));
        assertThrows(AccessDeniedException.class, () -> asPriya.forPatient(2));
        assertThrows(AccessDeniedException.class, () -> asPriya.order(1, 1));
        assertThrows(AccessDeniedException.class, () -> new LabService(db, TestDatabase.admin()).order(1, 1),
                "only doctors order tests");
    }
}
