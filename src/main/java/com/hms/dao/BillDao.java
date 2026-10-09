package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Bill;
import com.hms.model.BillStatus;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class BillDao {

    private static final String SELECT = """
            SELECT b.id, b.patient_id, p.name AS patient_name, b.room_charges, b.medicine_charges,
                   b.lab_charges, b.total, b.status, b.created_at, b.paid_at
            FROM bills b
            JOIN patients p ON p.id = b.patient_id
            """;

    private final SqlExecutor sql;

    public BillDao(SqlExecutor sql) {
        this.sql = sql;
    }

    /** All bills, or only one patient's when patientId is not null. */
    public List<Bill> find(Integer patientId) {
        if (patientId == null) {
            return sql.query(SELECT + " ORDER BY b.created_at DESC, b.id DESC", BillDao::map);
        }
        return sql.query(SELECT + " WHERE b.patient_id = ? ORDER BY b.created_at DESC, b.id DESC", BillDao::map,
                patientId);
    }

    public Optional<Bill> findById(int id) {
        return sql.queryOne(SELECT + " WHERE b.id = ?", BillDao::map, id);
    }

    /** Creates an empty bill; the charges are filled in once the charge rows are attached. */
    public int insert(int patientId, LocalDateTime createdAt) {
        return sql.insert("INSERT INTO bills (patient_id, created_at) VALUES (?, ?)", patientId, createdAt);
    }

    public void setCharges(int billId, BigDecimal room, BigDecimal medicine, BigDecimal lab) {
        sql.update("UPDATE bills SET room_charges = ?, medicine_charges = ?, lab_charges = ? WHERE id = ?",
                room, medicine, lab, billId);
    }

    public boolean markPaid(int billId, LocalDateTime paidAt) {
        return sql.update("UPDATE bills SET status = 'PAID', paid_at = ? WHERE id = ? AND status = 'UNPAID'",
                paidAt, billId) == 1;
    }

    public int countUnpaid(Integer patientId) {
        if (patientId == null) {
            return sql.queryInt("SELECT COUNT(*) FROM bills WHERE status = 'UNPAID'");
        }
        return sql.queryInt("SELECT COUNT(*) FROM bills WHERE status = 'UNPAID' AND patient_id = ?", patientId);
    }

    public BigDecimal sumUnpaid(Integer patientId) {
        String sqlText = "SELECT COALESCE(SUM(total), 0) FROM bills WHERE status = 'UNPAID'"
                + (patientId == null ? "" : " AND patient_id = ?");
        Object[] params = patientId == null ? new Object[0] : new Object[]{patientId};
        return sql.queryOne(sqlText, rs -> rs.getBigDecimal(1), params).orElse(BigDecimal.ZERO);
    }

    static Bill map(ResultSet rs) throws SQLException {
        return new Bill(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getBigDecimal("room_charges"),
                rs.getBigDecimal("medicine_charges"),
                rs.getBigDecimal("lab_charges"),
                rs.getBigDecimal("total"),
                BillStatus.valueOf(rs.getString("status")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("paid_at", LocalDateTime.class));
    }
}
