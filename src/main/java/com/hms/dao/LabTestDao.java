package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.LabTest;
import com.hms.model.LabTestStatus;
import com.hms.model.LabTestType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LabTestDao {

    private static final String SELECT = """
            SELECT lt.id, lt.patient_id, p.name AS patient_name, lt.doctor_id, d.name AS doctor_name,
                   t.name AS test_name, lt.price, lt.status, lt.result, lt.ordered_at, lt.completed_at, lt.bill_id
            FROM lab_tests lt
            JOIN patients p ON p.id = lt.patient_id
            JOIN doctors d ON d.id = lt.doctor_id
            JOIN lab_test_types t ON t.id = lt.test_type_id
            """;

    private final SqlExecutor sql;

    public LabTestDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<LabTestType> findTypes() {
        return sql.query("SELECT id, name, price FROM lab_test_types ORDER BY name",
                rs -> new LabTestType(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price")));
    }

    public Optional<LabTestType> findType(int typeId) {
        return sql.queryOne("SELECT id, name, price FROM lab_test_types WHERE id = ?",
                rs -> new LabTestType(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price")), typeId);
    }

    /** Lists tests, optionally only one patient's or the ones one doctor ordered. */
    public List<LabTest> find(Integer patientId, Integer doctorId, boolean pendingOnly) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (patientId != null) {
            where.append(" AND lt.patient_id = ?");
            params.add(patientId);
        }
        if (doctorId != null) {
            where.append(" AND lt.doctor_id = ?");
            params.add(doctorId);
        }
        if (pendingOnly) {
            where.append(" AND lt.status = 'ORDERED'");
        }
        return sql.query(SELECT + where + " ORDER BY lt.ordered_at DESC", LabTestDao::map, params.toArray());
    }

    public Optional<LabTest> findById(int id) {
        return sql.queryOne(SELECT + " WHERE lt.id = ?", LabTestDao::map, id);
    }

    public List<LabTest> findByBill(int billId) {
        return sql.query(SELECT + " WHERE lt.bill_id = ? ORDER BY lt.ordered_at", LabTestDao::map, billId);
    }

    public int insert(int patientId, int doctorId, int testTypeId, BigDecimal price, LocalDateTime orderedAt) {
        return sql.insert("""
                INSERT INTO lab_tests (patient_id, doctor_id, test_type_id, price, ordered_at)
                VALUES (?, ?, ?, ?, ?)""",
                patientId, doctorId, testTypeId, price, orderedAt);
    }

    /** Saves a result for a test that is still ORDERED; returns false if it was already completed. */
    public boolean recordResult(int testId, String result, LocalDateTime completedAt) {
        return sql.update("""
                UPDATE lab_tests SET result = ?, status = 'COMPLETED', completed_at = ?
                WHERE id = ? AND status = 'ORDERED'""",
                result, completedAt, testId) == 1;
    }

    /** Attaches the patient's not-yet-billed tests to a bill. Returns how many were attached. */
    public int claimForBill(int patientId, int billId) {
        return sql.update("UPDATE lab_tests SET bill_id = ? WHERE patient_id = ? AND bill_id IS NULL",
                billId, patientId);
    }

    public int countPending(Integer doctorId) {
        if (doctorId == null) {
            return sql.queryInt("SELECT COUNT(*) FROM lab_tests WHERE status = 'ORDERED'");
        }
        return sql.queryInt("SELECT COUNT(*) FROM lab_tests WHERE status = 'ORDERED' AND doctor_id = ?", doctorId);
    }

    static LabTest map(ResultSet rs) throws SQLException {
        return new LabTest(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("test_name"),
                rs.getBigDecimal("price"),
                LabTestStatus.valueOf(rs.getString("status")),
                rs.getString("result"),
                rs.getObject("ordered_at", LocalDateTime.class),
                rs.getObject("completed_at", LocalDateTime.class),
                rs.getObject("bill_id", Integer.class));
    }
}
