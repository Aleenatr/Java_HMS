package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AppointmentDao {

    private static final String SELECT = """
            SELECT ap.id, ap.patient_id, p.name AS patient_name, ap.doctor_id, d.name AS doctor_name,
                   ap.appointment_date, ap.slot, ap.reason, ap.status
            FROM appointments ap
            JOIN patients p ON p.id = ap.patient_id
            JOIN doctors d ON d.id = ap.doctor_id
            """;

    private final SqlExecutor sql;

    public AppointmentDao(SqlExecutor sql) {
        this.sql = sql;
    }

    /**
     * Lists appointments, optionally only one patient's or one doctor's.
     * The WHERE clause is built from fixed pieces and every value is still a ? parameter.
     *
     * @param upcomingOnly only scheduled appointments from today on, soonest first
     */
    public List<Appointment> find(Integer patientId, Integer doctorId, boolean upcomingOnly) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (patientId != null) {
            where.append(" AND ap.patient_id = ?");
            params.add(patientId);
        }
        if (doctorId != null) {
            where.append(" AND ap.doctor_id = ?");
            params.add(doctorId);
        }
        if (upcomingOnly) {
            where.append(" AND ap.status = 'SCHEDULED' AND ap.appointment_date >= ?");
            params.add(LocalDate.now());
        }
        String order = upcomingOnly
                ? " ORDER BY ap.appointment_date, ap.slot"
                : " ORDER BY ap.appointment_date DESC, ap.slot DESC";
        return sql.query(SELECT + where + order, AppointmentDao::map, params.toArray());
    }

    public Optional<Appointment> findById(int id) {
        return sql.queryOne(SELECT + " WHERE ap.id = ?", AppointmentDao::map, id);
    }

    /** Time slots the doctor already has on that date (cancelled ones don't count). */
    public List<LocalTime> bookedSlots(int doctorId, LocalDate date) {
        return sql.query("""
                SELECT slot FROM appointments
                WHERE doctor_id = ? AND appointment_date = ? AND status <> 'CANCELLED'""",
                rs -> rs.getObject("slot", LocalTime.class), doctorId, date);
    }

    public int insert(int patientId, int doctorId, LocalDate date, LocalTime slot, String reason) {
        return sql.insert("""
                INSERT INTO appointments (patient_id, doctor_id, appointment_date, slot, reason)
                VALUES (?, ?, ?, ?, ?)""",
                patientId, doctorId, date, slot, reason);
    }

    /** Changes the status only if it is still {@code from}, e.g. you can't complete a cancelled visit. */
    public boolean changeStatus(int id, AppointmentStatus from, AppointmentStatus to) {
        return sql.update("UPDATE appointments SET status = ? WHERE id = ? AND status = ?", to, id, from) == 1;
    }

    public int countScheduledOn(LocalDate date, Integer doctorId) {
        if (doctorId == null) {
            return sql.queryInt("""
                    SELECT COUNT(*) FROM appointments WHERE appointment_date = ? AND status = 'SCHEDULED'""", date);
        }
        return sql.queryInt("""
                SELECT COUNT(*) FROM appointments
                WHERE appointment_date = ? AND status = 'SCHEDULED' AND doctor_id = ?""", date, doctorId);
    }

    static Appointment map(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getObject("appointment_date", LocalDate.class),
                rs.getObject("slot", LocalTime.class),
                rs.getString("reason"),
                AppointmentStatus.valueOf(rs.getString("status")));
    }
}
