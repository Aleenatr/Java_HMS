package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Dispense;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public final class DispenseDao {

    private static final String SELECT = """
            SELECT ds.id, ds.patient_id, p.name AS patient_name, ds.medicine_id, m.name AS medicine_name,
                   ds.quantity, ds.unit_price, ds.dispensed_at, ds.bill_id
            FROM medicine_dispenses ds
            JOIN patients p ON p.id = ds.patient_id
            JOIN medicines m ON m.id = ds.medicine_id
            """;

    private final SqlExecutor sql;

    public DispenseDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Dispense> findRecent(int limit) {
        return sql.query(SELECT + " ORDER BY ds.dispensed_at DESC, ds.id DESC LIMIT ?", DispenseDao::map, limit);
    }

    public List<Dispense> findByBill(int billId) {
        return sql.query(SELECT + " WHERE ds.bill_id = ? ORDER BY ds.dispensed_at", DispenseDao::map, billId);
    }

    public int insert(int patientId, int medicineId, int quantity, BigDecimal unitPrice, LocalDateTime at) {
        return sql.insert("""
                INSERT INTO medicine_dispenses (patient_id, medicine_id, quantity, unit_price, dispensed_at)
                VALUES (?, ?, ?, ?, ?)""",
                patientId, medicineId, quantity, unitPrice, at);
    }

    /** Attaches the patient's not-yet-billed medicines to a bill. Returns how many were attached. */
    public int claimForBill(int patientId, int billId) {
        return sql.update("UPDATE medicine_dispenses SET bill_id = ? WHERE patient_id = ? AND bill_id IS NULL",
                billId, patientId);
    }

    static Dispense map(ResultSet rs) throws SQLException {
        return new Dispense(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("medicine_id"),
                rs.getString("medicine_name"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getObject("dispensed_at", LocalDateTime.class),
                rs.getObject("bill_id", Integer.class));
    }
}
