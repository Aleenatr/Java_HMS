package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.Bill;
import com.hms.model.BillStatus;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdmissionServiceTest {

    // Demo data: room 1 = "101" (general, ₹1,200/day, available); room 7 = "301" (occupied by Rahul).
    private Database db;
    private AdmissionService asAdmin;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        asAdmin = new AdmissionService(db, TestDatabase.admin());
    }

    private String roomStatus(int roomId) {
        return db.queryOne("SELECT status FROM rooms WHERE id = ?", rs -> rs.getString(1), roomId).orElseThrow();
    }

    @Test
    void admittingOccupiesTheRoomAndLocksInTheRate() {
        int admissionId = asAdmin.admit(5, 1, 4, "Severe migraine");
        assertEquals("OCCUPIED", roomStatus(1));
        db.update("UPDATE rooms SET daily_rate = 9999 WHERE id = 1");
        assertEquals(0, new BigDecimal("1200.00").compareTo(db.queryOne(
                "SELECT daily_rate FROM admissions WHERE id = ?", rs -> rs.getBigDecimal(1), admissionId).orElseThrow()));
    }

    @Test
    void savedAdmissionTimeIsNeverInTheFuture() {
        // MySQL rounds fractions of a second, so an unrounded "now" could be stored up to half a second ahead
        // and the Days column would then see discharge-before-admission. Three admissions make a miss unlikely.
        int[][] patientAndRoom = {{5, 1}, {7, 2}, {8, 3}};
        for (int[] pair : patientAndRoom) {
            int id = asAdmin.admit(pair[0], pair[1], 4, "Observation");
            LocalDateTime saved = db.queryOne("SELECT admitted_at FROM admissions WHERE id = ?",
                    rs -> rs.getObject(1, LocalDateTime.class), id).orElseThrow();
            assertTrue(!saved.isAfter(LocalDateTime.now()), "admission saved in the future: " + saved);
        }
    }

    @Test
    void occupiedRoomCannotBeTakenAndNothingIsSaved() {
        int before = db.queryInt("SELECT COUNT(*) FROM admissions");
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.admit(5, 7, 4, "Migraine"));
        assertTrue(error.getMessage().contains("no longer available"));
        assertEquals(before, db.queryInt("SELECT COUNT(*) FROM admissions"));
    }

    @Test
    void patientCannotBeAdmittedTwice() {
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.admit(2, 1, 1, "Again"));
        assertEquals("This patient is already admitted.", error.getMessage());
        assertEquals("AVAILABLE", roomStatus(1), "the room claim was rolled back");
    }

    @Test
    void uniqueKeyStopsASecondOpenAdmissionEvenWithoutTheServiceCheck() {
        // Bypass the service and insert directly: MySQL itself refuses.
        assertThrows(RuntimeException.class, () -> db.insert("""
                INSERT INTO admissions (patient_id, room_id, doctor_id, reason, daily_rate, admitted_at)
                VALUES (2, 1, 1, 'x', 1200, NOW())"""));
    }

    @Test
    void dischargeFreesTheRoomAndBillsAllThreeModules() {
        // Rahul (patient 2): admitted 3 days ago in room 301 at ₹4,500/day,
        // unbilled medicines ₹180 (aspirin 30 x 1.50 + atorvastatin 15 x 9.00), lab ₹900 (lipid 600 + ECG 300).
        Bill bill = asAdmin.discharge(2);
        assertEquals("AVAILABLE", roomStatus(7));
        assertEquals(new BigDecimal("13500.00"), bill.roomCharges());
        assertEquals(new BigDecimal("180.00"), bill.medicineCharges());
        assertEquals(new BigDecimal("900.00"), bill.labCharges());
        assertEquals(new BigDecimal("14580.00"), bill.total());
        assertEquals(BillStatus.UNPAID, bill.status());
        assertEquals(0, db.queryInt("SELECT COUNT(*) FROM lab_tests WHERE patient_id = 2 AND bill_id IS NULL"));
        assertEquals(0, db.queryInt("SELECT COUNT(*) FROM medicine_dispenses WHERE patient_id = 2 AND bill_id IS NULL"));
    }

    @Test
    void dischargingTwiceIsRefused() {
        asAdmin.discharge(2);
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.discharge(2));
        assertTrue(error.getMessage().contains("already been discharged"));
        assertEquals(3, db.queryInt("SELECT COUNT(*) FROM bills"), "two demo bills plus one discharge bill");
    }

    @Test
    void readmissionAfterDischargeIsAllowed() {
        asAdmin.discharge(2);
        asAdmin.admit(2, 1, 1, "Chest pain again");
        assertEquals(2, db.queryInt("SELECT COUNT(*) FROM admissions WHERE patient_id = 2"));
    }

    @Test
    void doctorsSeeOnlyTheirInpatientsAndCannotAdmit() {
        AdmissionService asMehta = new AdmissionService(db, TestDatabase.drMehta());
        assertEquals(1, asMehta.list(true).size());
        assertEquals("Rahul Verma", asMehta.list(true).get(0).patientName());
        assertThrows(AccessDeniedException.class, () -> asMehta.admit(5, 1, 1, "x"));
        assertThrows(AccessDeniedException.class, () -> asMehta.discharge(2));
    }
}
