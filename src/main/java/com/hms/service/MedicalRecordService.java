package com.hms.service;

import com.hms.dao.MedicalRecordDao;
import com.hms.db.Database;
import com.hms.model.MedicalRecord;
import com.hms.security.AccessDeniedException;
import com.hms.security.Session;

import java.time.LocalDate;
import java.util.List;

import static com.hms.security.Permission.VIEW_MEDICAL_RECORDS;
import static com.hms.security.Permission.WRITE_MEDICAL_RECORDS;

/**
 * Clinical notes. Only doctors write them. Doctors can read any patient's history,
 * patients only their own, and administrators none (clinical privacy).
 */
public final class MedicalRecordService {

    private final Session session;
    private final MedicalRecordDao records;

    public MedicalRecordService(Database db, Session session) {
        this.session = session;
        this.records = new MedicalRecordDao(db);
    }

    public List<MedicalRecord> forPatient(int patientId) {
        session.require(VIEW_MEDICAL_RECORDS);
        if (session.isPatient() && patientId != session.patientId()) {
            throw new AccessDeniedException("Access denied: you can only view your own medical records.");
        }
        return records.findByPatient(patientId);
    }

    /** The record is always signed by the logged-in doctor; the doctor can't be chosen. */
    public int add(int patientId, LocalDate visitDate, String diagnosis, String prescription, String notes) {
        session.require(WRITE_MEDICAL_RECORDS);
        return records.insert(
                patientId,
                session.doctorId(),
                Validator.requireNotFuture("Visit date", visitDate),
                Validator.requireText("Diagnosis", diagnosis, 500),
                Validator.optionalText("Prescription", prescription, 1000),
                Validator.optionalText("Notes", notes, 1000));
    }
}
