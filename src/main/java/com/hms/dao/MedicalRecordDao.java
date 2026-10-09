package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.MedicalRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class MedicalRecordDao {

    private static final String SELECT = """
            SELECT mr.id, mr.patient_id, p.name AS patient_name, mr.doctor_id, d.name AS doctor_name,
                   mr.visit_date, mr.diagnosis, mr.prescription, mr.notes
            FROM medical_records mr
            JOIN patients p ON p.id = mr.patient_id
            JOIN doctors d ON d.id = mr.doctor_id
            """;

    private final SqlExecutor sql;

    public MedicalRecordDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<MedicalRecord> findByPatient(int patientId) {
        return sql.query(SELECT + " WHERE mr.patient_id = ? ORDER BY mr.visit_date DESC, mr.id DESC",
                MedicalRecordDao::map, patientId);
    }

    public int insert(int patientId, int doctorId, LocalDate visitDate, String diagnosis, String prescription,
                      String notes) {
        return sql.insert("""
                INSERT INTO medical_records (patient_id, doctor_id, visit_date, diagnosis, prescription, notes)
                VALUES (?, ?, ?, ?, ?, ?)""",
                patientId, doctorId, visitDate, diagnosis, prescription, notes);
    }

    static MedicalRecord map(ResultSet rs) throws SQLException {
        return new MedicalRecord(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getObject("visit_date", LocalDate.class),
                rs.getString("diagnosis"),
                rs.getString("prescription"),
                rs.getString("notes"));
    }
}
