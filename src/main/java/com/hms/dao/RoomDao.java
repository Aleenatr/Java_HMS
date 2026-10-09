package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Room;
import com.hms.model.RoomStatus;
import com.hms.model.RoomType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class RoomDao {

    private static final String SELECT = "SELECT id, room_number, type, daily_rate, status FROM rooms";

    private final SqlExecutor sql;

    public RoomDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Room> findAll() {
        return sql.query(SELECT + " ORDER BY room_number", RoomDao::map);
    }

    public List<Room> findAvailable() {
        return sql.query(SELECT + " WHERE status = 'AVAILABLE' ORDER BY room_number", RoomDao::map);
    }

    public Optional<Room> findById(int id) {
        return sql.queryOne(SELECT + " WHERE id = ?", RoomDao::map, id);
    }

    public int insert(Room r) {
        return sql.insert("INSERT INTO rooms (room_number, type, daily_rate) VALUES (?, ?, ?)",
                r.roomNumber(), r.type(), r.dailyRate());
    }

    public boolean update(Room r) {
        return sql.update("UPDATE rooms SET room_number = ?, type = ?, daily_rate = ? WHERE id = ?",
                r.roomNumber(), r.type(), r.dailyRate(), r.id()) == 1;
    }

    /**
     * Claims the room. The WHERE clause only matches a room that is still AVAILABLE, so if two
     * people try to take the same room at once, exactly one UPDATE changes a row; the other gets false.
     */
    public boolean occupy(int roomId) {
        return changeStatus(roomId, RoomStatus.AVAILABLE, RoomStatus.OCCUPIED);
    }

    public boolean release(int roomId) {
        return changeStatus(roomId, RoomStatus.OCCUPIED, RoomStatus.AVAILABLE);
    }

    public boolean changeStatus(int roomId, RoomStatus from, RoomStatus to) {
        return sql.update("UPDATE rooms SET status = ? WHERE id = ? AND status = ?", to, roomId, from) == 1;
    }

    public int countByStatus(RoomStatus status) {
        return sql.queryInt("SELECT COUNT(*) FROM rooms WHERE status = ?", status);
    }

    public int count() {
        return sql.queryInt("SELECT COUNT(*) FROM rooms");
    }

    static Room map(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("room_number"),
                RoomType.valueOf(rs.getString("type")),
                rs.getBigDecimal("daily_rate"),
                RoomStatus.valueOf(rs.getString("status")));
    }
}
