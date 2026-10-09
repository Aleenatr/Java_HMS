package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.AdmissionDao;
import com.hms.dao.BillDao;
import com.hms.dao.DispenseDao;
import com.hms.dao.LabTestDao;
import com.hms.dao.PatientDao;
import com.hms.db.Database;
import com.hms.db.SqlExecutor;
import com.hms.model.Admission;
import com.hms.model.Bill;
import com.hms.model.BillDetails;
import com.hms.model.Dispense;
import com.hms.model.LabTest;
import com.hms.model.Patient;
import com.hms.security.AccessDeniedException;
import com.hms.security.Session;
import com.hms.util.Times;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.hms.security.Permission.MANAGE_BILLING;
import static com.hms.security.Permission.VIEW_BILLS;

/**
 * Billing across three modules. A bill collects every not-yet-billed charge of a patient:
 * finished room stays (admissions), medicines (pharmacy) and lab tests.
 */
public final class BillingService {

    private final Database db;
    private final Session session;
    private final BillDao bills;

    public BillingService(Database db, Session session) {
        this.db = db;
        this.session = session;
        this.bills = new BillDao(db);
    }

    /** Administrators see every bill; patients only their own. */
    public List<Bill> list() {
        session.require(VIEW_BILLS);
        return bills.find(session.isPatient() ? session.patientId() : null);
    }

    public Bill generate(int patientId) {
        session.require(MANAGE_BILLING);
        return db.transaction(tx -> createBill(tx, patientId, Times.now()));
    }

    /**
     * Creates a bill inside the caller's transaction. "Claim first, then add up": the UPDATEs
     * stamp this bill's id on the patient's unbilled rows, and only rows carrying that id are
     * summed. A charge can therefore never be billed twice or skipped. If there was nothing to
     * claim, the exception rolls the whole transaction back, so no empty bill is left behind.
     */
    static Bill createBill(SqlExecutor tx, int patientId, LocalDateTime now) {
        BillDao billDao = new BillDao(tx);
        AdmissionDao admissions = new AdmissionDao(tx);
        DispenseDao dispenses = new DispenseDao(tx);
        LabTestDao labTests = new LabTestDao(tx);

        int billId = billDao.insert(patientId, now);
        int claimed = admissions.claimForBill(patientId, billId)
                + dispenses.claimForBill(patientId, billId)
                + labTests.claimForBill(patientId, billId);
        if (claimed == 0) {
            throw new HmsException("This patient has no unbilled charges.");
        }
        BigDecimal room = BillCalculator.roomCharges(admissions.findByBill(billId));
        BigDecimal medicine = BillCalculator.medicineCharges(dispenses.findByBill(billId));
        BigDecimal lab = BillCalculator.labCharges(labTests.findByBill(billId));
        billDao.setCharges(billId, room, medicine, lab);
        return billDao.findById(billId).orElseThrow();
    }

    public void markPaid(int billId) {
        session.require(MANAGE_BILLING);
        if (!bills.markPaid(billId, Times.now())) {
            throw new HmsException("Bill #" + billId + " is already paid or does not exist.");
        }
    }

    /** The bill with every line item, for printing. Patients can open only their own bills. */
    public BillDetails details(int billId) {
        session.require(VIEW_BILLS);
        Bill bill = bills.findById(billId).orElseThrow(() -> new HmsException("Bill #" + billId + " was not found."));
        if (session.isPatient() && bill.patientId() != session.patientId()) {
            throw new AccessDeniedException("Access denied: you can only view your own bills.");
        }
        Patient patient = new PatientDao(db).findById(bill.patientId()).orElseThrow();
        List<Admission> stays = new AdmissionDao(db).findByBill(billId);
        List<Dispense> medicines = new DispenseDao(db).findByBill(billId);
        List<LabTest> tests = new LabTestDao(db).findByBill(billId);
        return new BillDetails(bill, patient, stays, medicines, tests);
    }

    public String printableBill(int billId) {
        return BillFormatter.format(details(billId));
    }
}
