package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.AppointmentDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.security.AccessDeniedException;
import com.hms.security.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.hms.security.Permission.BOOK_APPOINTMENTS;
import static com.hms.security.Permission.COMPLETE_APPOINTMENTS;

/**
 * Booking, cancelling and completing appointments.
 * Admins see all appointments, doctors their own, patients their own.
 */
public final class AppointmentService {

    /** 30-minute slots: 09:00-13:00 and 14:00-17:00 (lunch break 13:00-14:00). */
    public static final List<LocalTime> SLOTS = buildSlots();

    private static final int MAX_DAYS_AHEAD = 90;

    private final Session session;
    private final AppointmentDao appointments;

    public AppointmentService(Database db, Session session) {
        this.session = session;
        this.appointments = new AppointmentDao(db);
    }

    private static List<LocalTime> buildSlots() {
        List<LocalTime> slots = new ArrayList<>();
        for (LocalTime t = LocalTime.of(9, 0); t.isBefore(LocalTime.of(13, 0)); t = t.plusMinutes(30)) {
            slots.add(t);
        }
        for (LocalTime t = LocalTime.of(14, 0); t.isBefore(LocalTime.of(17, 0)); t = t.plusMinutes(30)) {
            slots.add(t);
        }
        return List.copyOf(slots);
    }

    public List<Appointment> list(boolean upcomingOnly) {
        Integer patientId = session.isPatient() ? session.patientId() : null;
        Integer doctorId = session.isDoctor() ? session.doctorId() : null;
        return appointments.find(patientId, doctorId, upcomingOnly);
    }

    /** Slots the doctor still has free that day. For today, slots that have already passed are left out. */
    public List<LocalTime> freeSlots(int doctorId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return List.of();
        }
        List<LocalTime> taken = appointments.bookedSlots(doctorId, date);
        LocalTime now = LocalTime.now();
        return SLOTS.stream()
                .filter(slot -> !taken.contains(slot))
                .filter(slot -> !date.equals(LocalDate.now()) || slot.isAfter(now))
                .toList();
    }

    public int book(int patientId, int doctorId, LocalDate date, LocalTime slot, String reason) {
        session.require(BOOK_APPOINTMENTS);
        if (session.isPatient() && patientId != session.patientId()) {
            throw new AccessDeniedException("Access denied: patients can only book for themselves.");
        }
        Validator.requireSelected("Date", date);
        Validator.requireSelected("Time slot", slot);
        String cleanReason = Validator.requireText("Reason for visit", reason, 255);
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new ValidationException("The appointment date cannot be in the past.");
        }
        if (date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new ValidationException("Appointments can be booked at most " + MAX_DAYS_AHEAD + " days ahead.");
        }
        if (!SLOTS.contains(slot)) {
            throw new ValidationException("Please pick one of the listed time slots.");
        }
        if (date.equals(today) && !slot.isAfter(LocalTime.now())) {
            throw new ValidationException("That time has already passed today. Please pick a later slot.");
        }
        try {
            return appointments.insert(patientId, doctorId, date, slot, cleanReason);
        } catch (DataAccessException e) {
            // The UNIQUE key (doctor, date, slot) is the final guard against double booking.
            if (e.isDuplicateKey()) {
                throw new HmsException("That slot was just taken. Please pick another time.");
            }
            throw e;
        }
    }

    public void cancel(int appointmentId) {
        session.require(BOOK_APPOINTMENTS);
        Appointment appointment = find(appointmentId);
        if (session.isPatient() && appointment.patientId() != session.patientId()) {
            throw new AccessDeniedException("Access denied: you can only cancel your own appointments.");
        }
        if (!appointments.changeStatus(appointmentId, AppointmentStatus.SCHEDULED, AppointmentStatus.CANCELLED)) {
            throw new HmsException("Only a scheduled appointment can be cancelled.");
        }
    }

    public void complete(int appointmentId) {
        session.require(COMPLETE_APPOINTMENTS);
        Appointment appointment = find(appointmentId);
        if (session.isDoctor() && appointment.doctorId() != session.doctorId()) {
            throw new AccessDeniedException("Access denied: you can only complete your own appointments.");
        }
        if (appointment.date().isAfter(LocalDate.now())) {
            throw new HmsException("A future appointment can't be marked as completed.");
        }
        if (!appointments.changeStatus(appointmentId, AppointmentStatus.SCHEDULED, AppointmentStatus.COMPLETED)) {
            throw new HmsException("Only a scheduled appointment can be marked as completed.");
        }
    }

    private Appointment find(int appointmentId) {
        return appointments.findById(appointmentId)
                .orElseThrow(() -> new HmsException("Appointment #" + appointmentId + " was not found."));
    }
}
