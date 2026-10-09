package com.hms.service;

import com.hms.HmsException;
import com.hms.TestDatabase;
import com.hms.db.Database;
import com.hms.model.Doctor;
import com.hms.model.Room;
import com.hms.model.RoomStatus;
import com.hms.model.RoomType;
import com.hms.security.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomAndDoctorServiceTest {

    private Database db;

    @BeforeEach
    void setUp() {
        db = TestDatabase.fresh();
    }

    @Test
    void roomsCanBeAddedButNumbersAreUnique() {
        RoomService rooms = new RoomService(db, TestDatabase.admin());
        rooms.create(new Room(0, "303", RoomType.PRIVATE, new BigDecimal("5000.00"), RoomStatus.AVAILABLE));
        HmsException duplicate = assertThrows(HmsException.class, () -> rooms.create(
                new Room(0, "303", RoomType.PRIVATE, new BigDecimal("5000.00"), RoomStatus.AVAILABLE)));
        assertEquals("Room 303 already exists.", duplicate.getMessage());
    }

    @Test
    void onlyAnEmptyRoomGoesUnderMaintenance() {
        RoomService rooms = new RoomService(db, TestDatabase.admin());
        assertThrows(HmsException.class, () -> rooms.setMaintenance(7, true), "room 301 is occupied");
        rooms.setMaintenance(1, true);
        assertTrue(rooms.available().stream().noneMatch(r -> r.id() == 1));
        rooms.setMaintenance(1, false);
        assertTrue(rooms.available().stream().anyMatch(r -> r.id() == 1));
    }

    @Test
    void doctorsAreListedForEveryoneButManagedByAdmins() {
        assertEquals(6, new DoctorService(db, TestDatabase.priya()).list().size());
        DoctorService asPriya = new DoctorService(db, TestDatabase.priya());
        assertThrows(AccessDeniedException.class, () -> asPriya.create(
                new Doctor(0, "Dr. Fake", "None", 1, null, "9876500000", null, null)));

        DoctorService asAdmin = new DoctorService(db, TestDatabase.admin());
        int id = asAdmin.create(new Doctor(0, "Dr. Ritu Bansal", "Dermatologist", 5, null, "9876500099", null, null));
        assertEquals("General Medicine", asAdmin.list().stream().filter(d -> d.id() == id).findFirst()
                .orElseThrow().departmentName());
        assertThrows(ValidationException.class, () -> asAdmin.create(
                new Doctor(0, "Dr. No Department", "X", 0, null, "9876500098", null, null)));
    }

    @Test
    void doctorWithPatientsCannotBeDeleted() {
        DoctorService asAdmin = new DoctorService(db, TestDatabase.admin());
        HmsException error = assertThrows(HmsException.class, () -> asAdmin.delete(1));
        assertTrue(error.getMessage().contains("can't be deleted"));
        asAdmin.delete(6);
        assertEquals(5, asAdmin.list().size());
    }
}
