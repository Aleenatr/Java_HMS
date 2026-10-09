package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.DispenseDao;
import com.hms.dao.MedicineDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.Dispense;
import com.hms.model.Medicine;
import com.hms.security.Session;
import com.hms.util.Times;

import java.util.List;

import static com.hms.security.Permission.MANAGE_PHARMACY;

/** Medicine stock and dispensing. Dispensed medicines are charged on the patient's next bill. */
public final class PharmacyService {

    private static final int MAX_QUANTITY = 100_000;

    private final Database db;
    private final Session session;
    private final MedicineDao medicines;

    public PharmacyService(Database db, Session session) {
        this.db = db;
        this.session = session;
        this.medicines = new MedicineDao(db);
    }

    public List<Medicine> list(String search) {
        session.require(MANAGE_PHARMACY);
        return search == null || search.isBlank() ? medicines.findAll() : medicines.search(search);
    }

    public int add(Medicine medicine) {
        session.require(MANAGE_PHARMACY);
        Medicine valid = validate(medicine);
        if (valid.stock() < 0 || valid.stock() > MAX_QUANTITY) {
            throw new ValidationException("Opening stock must be between 0 and " + MAX_QUANTITY + ".");
        }
        try {
            return medicines.insert(valid);
        } catch (DataAccessException e) {
            throw duplicateName(e, valid);
        }
    }

    public void update(Medicine medicine) {
        session.require(MANAGE_PHARMACY);
        Medicine valid = validate(medicine);
        try {
            if (!medicines.update(valid)) {
                throw new HmsException("Medicine #" + medicine.id() + " was not found.");
            }
        } catch (DataAccessException e) {
            throw duplicateName(e, valid);
        }
    }

    public void delete(int medicineId) {
        session.require(MANAGE_PHARMACY);
        try {
            if (!medicines.delete(medicineId)) {
                throw new HmsException("Medicine #" + medicineId + " was not found.");
            }
        } catch (DataAccessException e) {
            if (e.isRowStillReferenced()) {
                throw new HmsException("This medicine has been dispensed before, so it can't be deleted.");
            }
            throw e;
        }
    }

    public void restock(int medicineId, int quantity) {
        session.require(MANAGE_PHARMACY);
        checkQuantity(quantity);
        if (!medicines.restock(medicineId, quantity)) {
            throw new HmsException("Medicine #" + medicineId + " was not found.");
        }
    }

    /**
     * Dispenses as one transaction: take the stock, then record the charge at today's price.
     * If there isn't enough stock, nothing changes.
     */
    public int dispense(int patientId, int medicineId, int quantity) {
        session.require(MANAGE_PHARMACY);
        checkQuantity(quantity);
        return db.transaction(tx -> {
            MedicineDao stock = new MedicineDao(tx);
            Medicine medicine = stock.findById(medicineId)
                    .orElseThrow(() -> new HmsException("Medicine #" + medicineId + " was not found."));
            if (!stock.takeStock(medicineId, quantity)) {
                throw new HmsException("Not enough " + medicine.name() + " in stock: only "
                        + medicine.stock() + " left.");
            }
            return new DispenseDao(tx).insert(patientId, medicineId, quantity, medicine.unitPrice(),
                    Times.now());
        });
    }

    public List<Dispense> recentDispenses() {
        session.require(MANAGE_PHARMACY);
        return new DispenseDao(db).findRecent(50);
    }

    private static void checkQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new ValidationException("Quantity must be between 1 and " + MAX_QUANTITY + ".");
        }
    }

    private static RuntimeException duplicateName(DataAccessException e, Medicine medicine) {
        if (e.isDuplicateKey()) {
            return new HmsException(medicine.name() + " is already in the inventory.");
        }
        return e;
    }

    static Medicine validate(Medicine m) {
        Validator.requireSelected("Unit price", m.unitPrice());
        if (m.unitPrice().signum() < 0) {
            throw new ValidationException("Unit price cannot be negative.");
        }
        if (m.reorderLevel() < 0) {
            throw new ValidationException("Reorder level cannot be negative.");
        }
        return new Medicine(
                m.id(),
                Validator.requireText("Medicine name", m.name(), 100),
                Validator.requireText("Category", m.category(), 50),
                Validator.optionalText("Manufacturer", m.manufacturer(), 100),
                m.unitPrice(),
                m.stock(),
                m.reorderLevel());
    }
}
