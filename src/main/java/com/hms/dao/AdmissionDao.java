package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Admission;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class AdmissionDao {

    private static final String SELECT = """
            SELECT a.id, a.patient_id, p.name AS patient_name, a.room_id, r.room_number,
                   a.doctor_id, d.name AS doctor_name, a.reason, a.daily_rate,
                   a.admitted_at, a.discharged_at, a.bill_id
            FROM admissions a
            JOIN patients p ON p.id = a.patient_id
            JOIN rooms r ON r.id = a.room_id
            JOIN doctors d ON d.id = a.doctor_id
            """;

    private final SqlExecutor sql;

    public AdmissionDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Admission> findAll(boolean openOnly) {
        String where = openOnly ? " WHERE a.discharged_at IS NULL" : "";
        return sql.query(SELECT + where + " ORDER BY a.admitted_at DESC", AdmissionDao::map);
    }

    public List<Admission> findByDoctor(int doctorId, boolean openOnly) {
        String open = openOnly ? " AND a.discharged_at IS NULL" : "";
        return sql.query(SELECT + " WHERE a.doctor_id = ?" + open + " ORDER BY a.admitted_at DESC",
                AdmissionDao::map, doctorId);
    }

    public Optional<Admission> findById(int id) {
        return sql.queryOne(SELECT + " WHERE a.id = ?", AdmissionDao::map, id);
    }

    public Optional<Admission> findOpenByPatient(int patientId) {
        return sql.queryOne(SELECT + " WHERE a.patient_id = ? AND a.discharged_at IS NULL", AdmissionDao::map,
                patientId);
    }

    public List<Admission> findByBill(int billId) {
        return sql.query(SELECT + " WHERE a.bill_id = ? ORDER BY a.admitted_at", AdmissionDao::map, billId);
    }

    public int insert(int patientId, int roomId, int doctorId, String reason, BigDecimal dailyRate,
                      LocalDateTime admittedAt) {
        return sql.insert("""
                INSERT INTO admissions (patient_id, room_id, doctor_id, reason, daily_rate, admitted_at)
                VALUES (?, ?, ?, ?, ?, ?)""",
                patientId, roomId, doctorId, reason, dailyRate, admittedAt);
    }

    /** Only an open admission can be discharged; returns false if it was already discharged. */
    public boolean markDischarged(int admissionId, LocalDateTime dischargedAt) {
        return sql.update("UPDATE admissions SET discharged_at = ? WHERE id = ? AND discharged_at IS NULL",
                dischargedAt, admissionId) == 1;
    }

    /** Attaches the patient's finished, not-yet-billed stays to a bill. Returns how many were attached. */
    public int claimForBill(int patientId, int billId) {
        return sql.update("""
                UPDATE admissions SET bill_id = ?
                WHERE patient_id = ? AND bill_id IS NULL AND discharged_at IS NOT NULL""",
                billId, patientId);
    }

    public int countOpen() {
        return sql.queryInt("SELECT COUNT(*) FROM admissions WHERE discharged_at IS NULL");
    }

    static Admission map(ResultSet rs) throws SQLException {
        return new Admission(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("reason"),
                rs.getBigDecimal("daily_rate"),
                rs.getObject("admitted_at", LocalDateTime.class),
                rs.getObject("discharged_at", LocalDateTime.class),
                rs.getObject("bill_id", Integer.class));
    }
}
