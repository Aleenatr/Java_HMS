package com.hms.service;

import com.hms.db.Database;
import com.hms.security.PasswordHasher;
import com.hms.security.Session;

/**
 * Every service, created once per login with that user's session.
 * The screens get this object and never talk to the database directly.
 */
public record Services(Session session, PatientService patients, DoctorService doctors, RoomService rooms,
                       AdmissionService admissions, AppointmentService appointments,
                       MedicalRecordService records, LabService lab, PharmacyService pharmacy,
                       BillingService billing, UserService users, DashboardService dashboard,
                       AuthService auth) {

    public static Services create(Database db, Session session, AuthService auth, PasswordHasher hasher) {
        return new Services(
                session,
                new PatientService(db, session),
                new DoctorService(db, session),
                new RoomService(db, session),
                new AdmissionService(db, session),
                new AppointmentService(db, session),
                new MedicalRecordService(db, session),
                new LabService(db, session),
                new PharmacyService(db, session),
                new BillingService(db, session),
                new UserService(db, session, hasher),
                new DashboardService(db, session),
                auth);
    }
}
