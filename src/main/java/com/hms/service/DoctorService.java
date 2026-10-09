package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.DepartmentDao;
import com.hms.dao.DoctorDao;
import com.hms.dao.UserDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.Department;
import com.hms.model.Doctor;
import com.hms.security.Session;

import java.util.List;

import static com.hms.security.Permission.MANAGE_DOCTORS;

/** Doctors and departments. Everyone may see the list of doctors (patients need it to book). */
public final class DoctorService {

    private final Database db;
    private final Session session;
    private final DoctorDao doctors;
    private final DepartmentDao departments;

    public DoctorService(Database db, Session session) {
        this.db = db;
        this.session = session;
        this.doctors = new DoctorDao(db);
        this.departments = new DepartmentDao(db);
    }

    public List<Doctor> list() {
        return doctors.findAll();
    }

    public List<Department> departments() {
        return departments.findAll();
    }

    public int create(Doctor doctor) {
        session.require(MANAGE_DOCTORS);
        return doctors.insert(validate(doctor));
    }

    public void update(Doctor doctor) {
        session.require(MANAGE_DOCTORS);
        if (!doctors.update(validate(doctor))) {
            throw new HmsException("Doctor #" + doctor.id() + " was not found.");
        }
    }

    /** Only a doctor with no appointments, stays, records or tests can be deleted. */
    public void delete(int doctorId) {
        session.require(MANAGE_DOCTORS);
        try {
            db.transaction(tx -> {
                DoctorDao dao = new DoctorDao(tx);
                Doctor doctor = dao.findById(doctorId)
                        .orElseThrow(() -> new HmsException("Doctor #" + doctorId + " was not found."));
                dao.delete(doctorId);
                if (doctor.userId() != null) {
                    new UserDao(tx).delete(doctor.userId());
                }
                return null;
            });
        } catch (DataAccessException e) {
            if (e.isRowStillReferenced()) {
                throw new HmsException("This doctor has appointments, patients or records linked to them, "
                        + "so they can't be deleted. Disable their login instead.");
            }
            throw e;
        }
    }

    public int addDepartment(String name, String phone) {
        session.require(MANAGE_DOCTORS);
        String cleanName = Validator.requireText("Department name", name, 80);
        String cleanPhone = Validator.requireLandline("Department phone", phone);
        try {
            return departments.insert(cleanName, cleanPhone);
        } catch (DataAccessException e) {
            if (e.isDuplicateKey()) {
                throw new HmsException("A department called " + cleanName + " already exists.");
            }
            throw e;
        }
    }

    static Doctor validate(Doctor d) {
        if (d.departmentId() <= 0) {
            throw new ValidationException("Please choose a department.");
        }
        return new Doctor(
                d.id(),
                Validator.requireName("Name", d.name()),
                Validator.requireText("Specialization", d.specialization(), 100),
                d.departmentId(),
                d.departmentName(),
                Validator.requireMobile("Phone", d.phone()),
                Validator.optionalEmail(d.email()),
                d.userId());
    }
}
