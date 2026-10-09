package com.hms.service;

import com.hms.HmsException;
import com.hms.dao.RoomDao;
import com.hms.db.DataAccessException;
import com.hms.db.Database;
import com.hms.model.Room;
import com.hms.model.RoomStatus;
import com.hms.security.Session;

import java.util.List;

import static com.hms.security.Permission.MANAGE_ADMISSIONS;
import static com.hms.security.Permission.MANAGE_ROOMS;

public final class RoomService {

    private final Session session;
    private final RoomDao rooms;

    public RoomService(Database db, Session session) {
        this.session = session;
        this.rooms = new RoomDao(db);
    }

    public List<Room> list() {
        session.require(MANAGE_ROOMS);
        return rooms.findAll();
    }

    public List<Room> available() {
        session.require(MANAGE_ADMISSIONS);
        return rooms.findAvailable();
    }

    public int create(Room room) {
        session.require(MANAGE_ROOMS);
        try {
            return rooms.insert(validate(room));
        } catch (DataAccessException e) {
            throw duplicateNumber(e, room);
        }
    }

    /** Changing the rate never touches current patients: their rate was copied when they were admitted. */
    public void update(Room room) {
        session.require(MANAGE_ROOMS);
        try {
            if (!rooms.update(validate(room))) {
                throw new HmsException("Room #" + room.id() + " was not found.");
            }
        } catch (DataAccessException e) {
            throw duplicateNumber(e, room);
        }
    }

    /** Takes an empty room out of use, or puts it back. Occupied rooms can't be changed. */
    public void setMaintenance(int roomId, boolean underMaintenance) {
        session.require(MANAGE_ROOMS);
        boolean changed = underMaintenance
                ? rooms.changeStatus(roomId, RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE)
                : rooms.changeStatus(roomId, RoomStatus.MAINTENANCE, RoomStatus.AVAILABLE);
        if (!changed) {
            throw new HmsException(underMaintenance
                    ? "Only an available (empty) room can be put under maintenance."
                    : "This room is not under maintenance.");
        }
    }

    private static RuntimeException duplicateNumber(DataAccessException e, Room room) {
        if (e.isDuplicateKey()) {
            return new HmsException("Room " + room.roomNumber().strip() + " already exists.");
        }
        return e;
    }

    static Room validate(Room r) {
        String number = Validator.requireText("Room number", r.roomNumber(), 10);
        Validator.requireSelected("Room type", r.type());
        Validator.requireSelected("Daily rate", r.dailyRate());
        if (r.dailyRate().signum() < 0) {
            throw new ValidationException("Daily rate cannot be negative.");
        }
        return new Room(r.id(), number, r.type(), r.dailyRate(), r.status());
    }
}
