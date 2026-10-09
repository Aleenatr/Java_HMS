package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.PatientDao;
import com.hms.dao.UserDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.Patient;
import com.hms.security.AccessDeniedException;
import com.hms.security.Session;

import java.util.List;

import static com.hms.security.Permission.MANAGE_PATIENTS;
import static com.hms.security.Permission.VIEW_PATIENTS;

public final class PatientService {

    private final Database db;
    private final Session session;
    private final PatientDao patients;

    public PatientService(Database db, Session session) {
        this.db = db;
        this.session = session;
        this.patients = new PatientDao(db);
    }

    public List<Patient> list(String search) {
        session.require(VIEW_PATIENTS);
        return search == null || search.isBlank() ? patients.findAll() : patients.search(search);
    }

    /** Admins and doctors can open any patient; a patient can open only their own record. */
    public Patient get(int patientId) {
        boolean own = session.isPatient() && session.patientId() == patientId;
        if (!session.can(VIEW_PATIENTS) && !own) {
            throw new AccessDeniedException("Access denied: you can only view your own details.");
        }
        return patients.findById(patientId)
                .orElseThrow(() -> new HmsException("Patient #" + patientId + " was not found."));
    }

    public int create(Patient patient) {
        session.require(MANAGE_PATIENTS);
        return patients.insert(validate(patient));
    }

    public void update(Patient patient) {
        session.require(MANAGE_PATIENTS);
        if (!patients.update(validate(patient))) {
            throw new HmsException("Patient #" + patient.id() + " was not found.");
        }
    }

    /**
     * Deletes a patient who has no history yet (e.g. registered by mistake), together with
     * their login. Foreign keys block deleting anyone with appointments, stays or bills.
     */
    public void delete(int patientId) {
        session.require(MANAGE_PATIENTS);
        try {
            db.transaction(tx -> {
                PatientDao dao = new PatientDao(tx);
                Patient patient = dao.findById(patientId)
                        .orElseThrow(() -> new HmsException("Patient #" + patientId + " was not found."));
                dao.delete(patientId);
                if (patient.userId() != null) {
                    new UserDao(tx).delete(patient.userId());
                }
                return null;
            });
        } catch (DataAccessException e) {
            if (e.isRowStillReferenced()) {
                throw new HmsException("This patient has appointments, hospital stays, records or bills, "
                        + "so the record can't be deleted. Medical history must be kept.");
            }
            throw e;
        }
    }

    static Patient validate(Patient p) {
        return new Patient(
                p.id(),
                Validator.requireName("Name", p.name()),
                Validator.requireSelected("Gender", p.gender()),
                Validator.requireBirthDate(p.dateOfBirth()),
                Validator.requireMobile("Phone", p.phone()),
                Validator.optionalEmail(p.email()),
                Validator.optionalText("Address", p.address(), 255),
                p.userId());
    }
}
