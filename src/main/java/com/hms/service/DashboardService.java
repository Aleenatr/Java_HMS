package com.hms.service;

import com.hms.dao.AdmissionDao;
import com.hms.dao.AppointmentDao;
import com.hms.dao.BillDao;
import com.hms.dao.DoctorDao;
import com.hms.dao.LabTestDao;
import com.hms.dao.MedicineDao;
import com.hms.dao.PatientDao;
import com.hms.dao.RoomDao;
import com.hms.db.Database;
import com.hms.model.Appointment;
import com.hms.model.LabTest;
import com.hms.model.RoomStatus;
import com.hms.security.Session;
import com.hms.util.Formats;

import java.time.LocalDate;
import java.util.List;

/** The numbers on the home screen. Each role gets figures about its own work. */
public final class DashboardService {

    /** One tile on the dashboard. */
    public record Stat(String label, String value) {
    }

    private final Database db;
    private final Session session;

    public DashboardService(Database db, Session session) {
        this.db = db;
        this.session = session;
    }

    public List<Stat> stats() {
        return switch (session.role()) {
            case ADMIN -> adminStats();
            case DOCTOR -> doctorStats();
            case PATIENT -> patientStats();
        };
    }

    private List<Stat> adminStats() {
        RoomDao rooms = new RoomDao(db);
        BillDao bills = new BillDao(db);
        LocalDate today = LocalDate.now();
        return List.of(
                new Stat("Patients", String.valueOf(new PatientDao(db).count())),
                new Stat("Doctors", String.valueOf(new DoctorDao(db).count())),
                new Stat("Admitted now", String.valueOf(new AdmissionDao(db).countOpen())),
                new Stat("Rooms available", rooms.countByStatus(RoomStatus.AVAILABLE) + " of " + rooms.count()),
                new Stat("Appointments today", String.valueOf(new AppointmentDao(db).countScheduledOn(today, null))),
                new Stat("Pending lab tests", String.valueOf(new LabTestDao(db).countPending(null))),
                new Stat("Medicines to reorder", String.valueOf(new MedicineDao(db).countLowStock())),
                new Stat("Unpaid bills", bills.countUnpaid(null) + "  ·  " + Formats.money(bills.sumUnpaid(null))));
    }

    private List<Stat> doctorStats() {
        int doctorId = session.doctorId();
        AppointmentDao appointments = new AppointmentDao(db);
        return List.of(
                new Stat("My appointments today",
                        String.valueOf(appointments.countScheduledOn(LocalDate.now(), doctorId))),
                new Stat("My upcoming appointments",
                        String.valueOf(appointments.find(null, doctorId, true).size())),
                new Stat("My admitted patients",
                        String.valueOf(new AdmissionDao(db).findByDoctor(doctorId, true).size())),
                new Stat("Lab results awaited", String.valueOf(new LabTestDao(db).countPending(doctorId))));
    }

    private List<Stat> patientStats() {
        int patientId = session.patientId();
        List<Appointment> upcoming = new AppointmentDao(db).find(patientId, null, true);
        String next = upcoming.isEmpty() ? "None booked" : Formats.date(upcoming.get(0).date())
                + ", " + Formats.time(upcoming.get(0).slot());
        List<LabTest> tests = new LabTestDao(db).find(patientId, null, false);
        long ready = tests.stream().filter(t -> t.result() != null).count();
        BillDao bills = new BillDao(db);
        return List.of(
                new Stat("Next appointment", next),
                new Stat("Upcoming appointments", String.valueOf(upcoming.size())),
                new Stat("Lab results ready", ready + " of " + tests.size()),
                new Stat("Amount due", Formats.money(bills.sumUnpaid(patientId))));
    }

    /** The list under the tiles: scheduled appointments from today on (scoped like the Appointments screen). */
    public List<Appointment> upcomingAppointments() {
        Integer patientId = session.isPatient() ? session.patientId() : null;
        Integer doctorId = session.isDoctor() ? session.doctorId() : null;
        return new AppointmentDao(db).find(patientId, doctorId, true);
    }
}
