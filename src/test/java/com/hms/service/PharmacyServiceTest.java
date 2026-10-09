package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.Medicine;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PharmacyServiceTest {

    // Demo data: medicine 8 = Salbutamol Inhaler, ₹145.00, 15 in stock, reorder at 20.
    private Database db;
    private PharmacyService asAdmin;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        asAdmin = new PharmacyService(db, TestDatabase.admin());
    }

    private int stock(int medicineId) {
        return db.queryInt("SELECT stock FROM medicines WHERE id = ?", medicineId);
    }

    @Test
    void dispensingTakesStockAndRecordsTodaysPrice() {
        int dispenseId = asAdmin.dispense(3, 8, 2);
        assertEquals(13, stock(8));
        db.update("UPDATE medicines SET unit_price = 999 WHERE id = 8");
        assertEquals(0, new BigDecimal("145.00").compareTo(db.queryOne(
                "SELECT unit_price FROM medicine_dispenses WHERE id = ?", rs -> rs.getBigDecimal(1), dispenseId)
                .orElseThrow()), "the charge keeps the price at the time of dispensing");
    }

    @Test
    void cannotDispenseMoreThanIsInStock() {
        int dispensesBefore = db.queryInt("SELECT COUNT(*) FROM medicine_dispenses");
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.dispense(3, 8, 16));
        assertEquals("Not enough Salbutamol Inhaler in stock: only 15 left.", error.getMessage());
        assertEquals(15, stock(8));
        assertEquals(dispensesBefore, db.queryInt("SELECT COUNT(*) FROM medicine_dispenses"));
    }

    @Test
    void restockAndLowStockFlag() {
        assertTrue(asAdmin.list("Salbutamol").get(0).isLowStock());
        asAdmin.restock(8, 100);
        assertEquals(115, stock(8));
        assertTrue(!asAdmin.list("Salbutamol").get(0).isLowStock());
    }

    @Test
    void medicineRulesAreChecked() {
        assertThrows(ValidationException.class, () -> asAdmin.dispense(3, 8, 0));
        assertThrows(ValidationException.class, () -> asAdmin.add(
                new Medicine(0, "Cetirizine", "Allergy", null, new BigDecimal("-1"), 10, 5)));
        HmsException duplicate = assertThrows(HmsException.class, () -> asAdmin.add(
                new Medicine(0, "ORS Sachet", "Rehydration", null, new BigDecimal("18"), 10, 5)));
        assertTrue(duplicate.getMessage().contains("already in the inventory"));
    }

    @Test
    void dispensedMedicineCannotBeDeletedButANewOneCan() {
        assertThrows(HmsException.class, () -> asAdmin.delete(1));
        int id = asAdmin.add(new Medicine(0, "Cetirizine 10 mg", "Allergy", "Cipla", new BigDecimal("3.00"), 50, 10));
        asAdmin.delete(id);
        assertEquals(0, db.queryInt("SELECT COUNT(*) FROM medicines WHERE id = ?", id));
    }

    @Test
    void onlyTheAdministratorRunsThePharmacy() {
        PharmacyService asDoctor = new PharmacyService(db, TestDatabase.drMehta());
        assertThrows(AccessDeniedException.class, () -> asDoctor.list(null));
        assertThrows(AccessDeniedException.class, () -> asDoctor.dispense(1, 1, 1));
    }
}
