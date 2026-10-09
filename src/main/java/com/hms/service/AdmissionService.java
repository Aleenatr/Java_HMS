package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.AdmissionDao;
import com.hms.dao.RoomDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.Admission;
import com.hms.model.Bill;
import com.hms.model.Room;
import com.hms.security.Session;
import com.hms.util.Times;

import java.time.LocalDateTime;
import java.util.List;

import static com.hms.security.Permission.MANAGE_ADMISSIONS;
import static com.hms.security.Permission.MANAGE_BILLING;
import static com.hms.security.Permission.VIEW_PATIENTS;

/** Admitting patients to rooms and discharging them. Both are transactions. */
public final class AdmissionService {

    private final Database db;
    private final Session session;
    private final AdmissionDao admissions;

    public AdmissionService(Database db, Session session) {
        this.db = db;
        this.session = session;
        this.admissions = new AdmissionDao(db);
    }

    /** Administrators see every stay; doctors see the patients they are looking after. */
    public List<Admission> list(boolean currentOnly) {
        if (session.isDoctor()) {
            session.require(VIEW_PATIENTS);
            return admissions.findByDoctor(session.doctorId(), currentOnly);
        }
        session.require(MANAGE_ADMISSIONS);
        return admissions.findAll(currentOnly);
    }

    /**
     * Admits a patient as one transaction: claim the room, then record the stay.
     * If the room was taken a moment earlier by someone else, or the patient is already
     * admitted, nothing is saved.
     */
    public int admit(int patientId, int roomId, int doctorId, String reason) {
        session.require(MANAGE_ADMISSIONS);
        String cleanReason = Validator.requireText("Reason for admission", reason, 255);
        try {
            return db.transaction(tx -> {
                AdmissionDao stays = new AdmissionDao(tx);
                RoomDao rooms = new RoomDao(tx);
                if (stays.findOpenByPatient(patientId).isPresent()) {
                    throw new HmsException("This patient is already admitted.");
                }
                Room room = rooms.findById(roomId).orElseThrow(() -> new HmsException("Room not found."));
                if (!rooms.occupy(roomId)) {
                    throw new HmsException("Room " + room.roomNumber() + " is no longer available. Please pick another room.");
                }
                return stays.insert(patientId, roomId, doctorId, cleanReason, room.dailyRate(), Times.now());
            });
        } catch (DataAccessException e) {
            if (e.isDuplicateKey()) {
                throw new HmsException("This patient is already admitted.");
            }
            if (e.isMissingReference()) {
                throw new HmsException("The selected patient or doctor no longer exists.");
            }
            throw e;
        }
    }

    /**
     * Discharges a patient as one transaction across three tables: close the stay, free the
     * room and create the final bill (room + pharmacy + lab). Either all three happen or none.
     */
    public Bill discharge(int admissionId) {
        session.require(MANAGE_ADMISSIONS);
        session.require(MANAGE_BILLING);
        return db.transaction(tx -> {
            AdmissionDao stays = new AdmissionDao(tx);
            Admission stay = stays.findById(admissionId)
                    .orElseThrow(() -> new HmsException("Admission #" + admissionId + " was not found."));
            LocalDateTime now = Times.now();
            if (!stays.markDischarged(admissionId, now)) {
                throw new HmsException(stay.patientName() + " has already been discharged.");
            }
            new RoomDao(tx).release(stay.roomId());
            return BillingService.createBill(tx, stay.patientId(), now);
        });
    }
}
