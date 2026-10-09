package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.LabTestDao;
import com.hms.db.Database;
import com.hms.model.LabTest;
import com.hms.model.LabTestType;
import com.hms.security.AccessDeniedException;
import com.hms.security.Session;
import com.hms.util.Times;

import java.util.List;

import static com.hms.security.Permission.ORDER_LAB_TESTS;
import static com.hms.security.Permission.RECORD_LAB_RESULTS;
import static com.hms.security.Permission.VIEW_LAB_TESTS;
import static com.hms.security.Permission.VIEW_PATIENTS;

/** Lab tests: doctors order them, the lab desk (admin) or a doctor records the result. */
public final class LabService {

    private final Session session;
    private final LabTestDao tests;

    public LabService(Database db, Session session) {
        this.session = session;
        this.tests = new LabTestDao(db);
    }

    public List<LabTestType> testTypes() {
        session.require(ORDER_LAB_TESTS);
        return tests.findTypes();
    }

    /** Admins see every test, doctors the tests they ordered, patients their own. */
    public List<LabTest> list(boolean pendingOnly) {
        session.require(VIEW_LAB_TESTS);
        Integer patientId = session.isPatient() ? session.patientId() : null;
        Integer doctorId = session.isDoctor() ? session.doctorId() : null;
        return tests.find(patientId, doctorId, pendingOnly);
    }

    /** One patient's tests, for the patient history screen. */
    public List<LabTest> forPatient(int patientId) {
        session.require(VIEW_LAB_TESTS);
        boolean own = session.isPatient() && session.patientId() == patientId;
        if (!own && !session.can(VIEW_PATIENTS)) {
            throw new AccessDeniedException("Access denied: you can only view your own lab tests.");
        }
        return tests.find(patientId, null, false);
    }

    /** Orders a test as the logged-in doctor. The price is copied from the price list now. */
    public int order(int patientId, int testTypeId) {
        session.require(ORDER_LAB_TESTS);
        LabTestType type = tests.findType(testTypeId)
                .orElseThrow(() -> new HmsException("Unknown lab test."));
        return tests.insert(patientId, session.doctorId(), type.id(), type.price(), Times.now());
    }

    /** The lab desk (admin) can record any result; a doctor only for tests they ordered. */
    public void recordResult(int testId, String result) {
        session.require(RECORD_LAB_RESULTS);
        if (session.isDoctor()) {
            LabTest test = tests.findById(testId)
                    .orElseThrow(() -> new HmsException("Lab test #" + testId + " was not found."));
            if (test.doctorId() != session.doctorId()) {
                throw new AccessDeniedException("Access denied: you can only record results for tests you ordered.");
            }
        }
        String cleanResult = Validator.requireText("Result", result, 1000);
        if (!tests.recordResult(testId, cleanResult, Times.now())) {
            throw new HmsException("This test already has a result (or does not exist).");
        }
    }
}
