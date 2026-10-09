package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Doctor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class DoctorDao {

    private static final String SELECT = """
            SELECT d.id, d.name, d.specialization, d.department_id, dep.name AS department_name,
                   d.phone, d.email, d.user_id
            FROM doctors d
            JOIN departments dep ON dep.id = d.department_id
            """;

    private final SqlExecutor sql;

    public DoctorDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Doctor> findAll() {
        return sql.query(SELECT + " ORDER BY d.name", DoctorDao::map);
    }

    public Optional<Doctor> findById(int id) {
        return sql.queryOne(SELECT + " WHERE d.id = ?", DoctorDao::map, id);
    }

    public Optional<Doctor> findByUserId(int userId) {
        return sql.queryOne(SELECT + " WHERE d.user_id = ?", DoctorDao::map, userId);
    }

    public int insert(Doctor d) {
        return sql.insert("""
                INSERT INTO doctors (name, specialization, department_id, phone, email)
                VALUES (?, ?, ?, ?, ?)""",
                d.name(), d.specialization(), d.departmentId(), d.phone(), d.email());
    }

    public boolean update(Doctor d) {
        return sql.update("""
                UPDATE doctors SET name = ?, specialization = ?, department_id = ?, phone = ?, email = ?
                WHERE id = ?""",
                d.name(), d.specialization(), d.departmentId(), d.phone(), d.email(), d.id()) == 1;
    }

    public boolean delete(int id) {
        return sql.update("DELETE FROM doctors WHERE id = ?", id) == 1;
    }

    /** Links a login to the doctor, only if the doctor doesn't have one already. */
    public boolean linkUser(int doctorId, int userId) {
        return sql.update("UPDATE doctors SET user_id = ? WHERE id = ? AND user_id IS NULL", userId, doctorId) == 1;
    }

    public int count() {
        return sql.queryInt("SELECT COUNT(*) FROM doctors");
    }

    static Doctor map(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("specialization"),
                rs.getInt("department_id"),
                rs.getString("department_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getObject("user_id", Integer.class));
    }
}
