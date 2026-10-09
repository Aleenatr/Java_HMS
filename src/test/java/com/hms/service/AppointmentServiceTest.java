package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentServiceTest {

    private static final LocalTime TEN_AM = LocalTime.of(10, 0);
    private static final LocalDate NEXT_WEEK = LocalDate.now().plusDays(5);

    private Database db;
    private AppointmentService asAdmin;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
        asAdmin = new AppointmentService(db, TestDatabase.admin());
    }

    @Test
    void slotsAreThirtyMinutesWithALunchBreak() {
        assertEquals(14, AppointmentService.SLOTS.size());
        assertFalse(AppointmentService.SLOTS.contains(LocalTime.of(13, 0)));
        assertTrue(AppointmentService.SLOTS.contains(LocalTime.of(16, 30)));
    }

    @Test
    void bookedSlotIsNoLongerFree() {
        assertTrue(asAdmin.freeSlots(4, NEXT_WEEK).contains(TEN_AM));
        asAdmin.book(5, 4, NEXT_WEEK, TEN_AM, "Headache");
        assertFalse(asAdmin.freeSlots(4, NEXT_WEEK).contains(TEN_AM));
        assertTrue(asAdmin.freeSlots(3, NEXT_WEEK).contains(TEN_AM), "other doctors are unaffected");
        assertTrue(asAdmin.freeSlots(4, LocalDate.now().minusDays(1)).isEmpty(), "nothing is free in the past");
    }

    @Test
    void doubleBookingIsBlockedByTheDatabase() {
        asAdmin.book(5, 4, NEXT_WEEK, TEN_AM, "Headache");
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.book(6, 4, NEXT_WEEK, TEN_AM, "Dizzy"));
        assertEquals("That slot was just taken. Please pick another time.", error.getMessage());
    }

    @Test
    void cancelledSlotCanBeBookedAgain() {
        int first = asAdmin.book(5, 4, NEXT_WEEK, TEN_AM, "Headache");
        asAdmin.cancel(first);
        int second = asAdmin.book(6, 4, NEXT_WEEK, TEN_AM, "Dizzy");
        assertTrue(second > first);
        assertEquals(2, db.queryInt("SELECT COUNT(*) FROM appointments WHERE doctor_id = 4 AND appointment_date = ?",
                NEXT_WEEK), "the cancelled booking is kept as history");
    }

    @Test
    void bookingRulesAreChecked() {
        assertThrows(ValidationException.class,
                () -> asAdmin.book(5, 4, LocalDate.now().minusDays(1), TEN_AM, "Past"));
        assertThrows(ValidationException.class,
                () -> asAdmin.book(5, 4, LocalDate.now().plusDays(120), TEN_AM, "Too far"));
        assertThrows(ValidationException.class,
                () -> asAdmin.book(5, 4, NEXT_WEEK, LocalTime.of(13, 15), "Lunch time"));
        assertThrows(ValidationException.class, () -> asAdmin.book(5, 4, NEXT_WEEK, TEN_AM, "  "));
    }

    @Test
    void patientsBookAndCancelOnlyForThemselves() {
        AppointmentService asPriya = new AppointmentService(db, TestDatabase.priya());
        int own = asPriya.book(1, 4, NEXT_WEEK, TEN_AM, "Migraine");
        assertThrows(AccessDeniedException.class, () -> asPriya.book(2, 4, NEXT_WEEK, LocalTime.of(11, 0), "x"));

        int someoneElses = asAdmin.book(5, 4, NEXT_WEEK, LocalTime.of(11, 0), "Headache");
        assertThrows(AccessDeniedException.class, () -> asPriya.cancel(someoneElses));
        asPriya.cancel(own);
        assertTrue(asPriya.list(false).stream().allMatch(a -> a.patientId() == 1));
    }

    @Test
    void doctorsSeeAndCompleteOnlyTheirOwnAppointments() {
        AppointmentService asMehta = new AppointmentService(db, TestDatabase.drMehta());
        List<Appointment> mine = asMehta.list(false);
        assertFalse(mine.isEmpty());
        assertTrue(mine.stream().allMatch(a -> a.doctorId() == 1));

        Appointment todayWithMehta = mine.stream()
                .filter(a -> a.date().equals(LocalDate.now()) && a.status() == AppointmentStatus.SCHEDULED)
                .findFirst().orElseThrow();
        asMehta.complete(todayWithMehta.id());
        assertThrows(HmsException.class, () -> asMehta.complete(todayWithMehta.id()), "already completed");

        Appointment withRao = new AppointmentService(db, TestDatabase.drRao()).list(true).get(0);
        assertThrows(AccessDeniedException.class, () -> asMehta.complete(withRao.id()));
        assertThrows(AccessDeniedException.class, () -> asMehta.book(1, 1, NEXT_WEEK, TEN_AM, "x"),
                "doctors don't book");
    }

    @Test
    void futureAppointmentCannotBeCompleted() {
        AppointmentService asMehta = new AppointmentService(db, TestDatabase.drMehta());
        Appointment tomorrow = asMehta.list(true).stream()
                .filter(a -> a.date().isAfter(LocalDate.now())).findFirst().orElseThrow();
        assertThrows(HmsException.class, () -> asMehta.complete(tomorrow.id()));
    }
}
