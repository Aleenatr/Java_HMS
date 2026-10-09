package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.Bill;
import com.hms.model.BillDetails;
import com.hms.model.BillStatus;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingServiceTest {

    private Database db;
    private BillingService asAdmin;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        asAdmin = new BillingService(db, TestDatabase.admin());
    }

    @Test
    void outpatientBillCollectsPharmacyAndLabCharges() {
        // Priya's unbilled charges: the thyroid profile (550) plus 5 paracetamol dispensed now (5 x 2.00).
        new PharmacyService(db, TestDatabase.admin()).dispense(1, 1, 5);
        Bill bill = asAdmin.generate(1);
        assertEquals(new BigDecimal("0.00"), bill.roomCharges(), "no hospital stay");
        assertEquals(new BigDecimal("10.00"), bill.medicineCharges());
        assertEquals(new BigDecimal("550.00"), bill.labCharges());
        assertEquals(new BigDecimal("560.00"), bill.total());
    }

    @Test
    void chargesAreNeverBilledTwiceAndNoEmptyBillIsLeft() {
        asAdmin.generate(1);
        int bills = db.queryInt("SELECT COUNT(*) FROM bills");
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.generate(1));
        assertEquals("This patient has no unbilled charges.", error.getMessage());
        assertEquals(bills, db.queryInt("SELECT COUNT(*) FROM bills"), "the empty bill was rolled back");
    }

    @Test
    void openStaysAreNotBilledUntilDischarge() {
        Bill bill = asAdmin.generate(2);
        assertEquals(new BigDecimal("0.00"), bill.roomCharges(), "Rahul is still admitted");
        assertEquals(new BigDecimal("180.00"), bill.medicineCharges());
    }

    @Test
    void newChargesGoOnTheNextBill() {
        asAdmin.generate(1);
        new PharmacyService(db, TestDatabase.admin()).dispense(1, 1, 5);
        Bill second = asAdmin.generate(1);
        assertEquals(new BigDecimal("0.00"), second.labCharges(), "the thyroid test was on the first bill");
        assertEquals(new BigDecimal("10.00"), second.medicineCharges());
        assertEquals(new BigDecimal("10.00"), second.total());
    }

    @Test
    void paymentIsRecordedOnce() {
        Bill bill = asAdmin.generate(1);
        asAdmin.markPaid(bill.id());
        Bill paid = asAdmin.list().stream().filter(b -> b.id() == bill.id()).findFirst().orElseThrow();
        assertEquals(BillStatus.PAID, paid.status());
        assertNotNull(paid.paidAt());
        assertThrows(HmsException.class, () -> asAdmin.markPaid(bill.id()));
    }

    @Test
    void printedBillListsEveryLine() {
        String text = asAdmin.printableBill(1);
        assertTrue(text.contains("Mohammed Irfan"));
        assertTrue(text.contains("Room 102"));
        assertTrue(text.contains("ORS Sachet"));
        assertTrue(text.contains("Dengue NS1 Antigen"));
        assertTrue(text.contains("₹6,078.00"));
    }

    @Test
    void patientsSeeOnlyTheirOwnBills() {
        Bill priyasBill = asAdmin.generate(1);
        BillingService asPriya = new BillingService(db, TestDatabase.priya());
        assertTrue(asPriya.list().stream().allMatch(b -> b.patientId() == 1));
        BillDetails details = asPriya.details(priyasBill.id());
        assertEquals("Priya Sharma", details.patient().name());
        assertThrows(AccessDeniedException.class, () -> asPriya.details(1), "bill 1 belongs to another patient");
        assertThrows(AccessDeniedException.class, () -> asPriya.generate(1));
        assertThrows(AccessDeniedException.class, () -> asPriya.markPaid(priyasBill.id()));
    }

    @Test
    void doctorsHaveNoAccessToBilling() {
        BillingService asDoctor = new BillingService(db, TestDatabase.drMehta());
        assertThrows(AccessDeniedException.class, asDoctor::list);
    }
}
