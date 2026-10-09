package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Gender;
import com.hms.model.Patient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class PatientDao {

    private static final String SELECT =
            "SELECT id, name, gender, date_of_birth, phone, email, address, user_id FROM patients";

    private final SqlExecutor sql;

    public PatientDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Patient> findAll() {
        return sql.query(SELECT + " ORDER BY name", PatientDao::map);
    }

    /** Matches part of a name or phone number. The search text is a parameter, so it can't change the SQL. */
    public List<Patient> search(String text) {
        String pattern = Like.contains(text);
        return sql.query(SELECT + " WHERE name LIKE ? OR phone LIKE ? ORDER BY name", PatientDao::map,
                pattern, pattern);
    }

    public Optional<Patient> findById(int id) {
        return sql.queryOne(SELECT + " WHERE id = ?", PatientDao::map, id);
    }

    public Optional<Patient> findByUserId(int userId) {
        return sql.queryOne(SELECT + " WHERE user_id = ?", PatientDao::map, userId);
    }

    public int insert(Patient p) {
        return sql.insert("""
                INSERT INTO patients (name, gender, date_of_birth, phone, email, address)
                VALUES (?, ?, ?, ?, ?, ?)""",
                p.name(), p.gender(), p.dateOfBirth(), p.phone(), p.email(), p.address());
    }

    public boolean update(Patient p) {
        return sql.update("""
                UPDATE patients SET name = ?, gender = ?, date_of_birth = ?, phone = ?, email = ?, address = ?
                WHERE id = ?""",
                p.name(), p.gender(), p.dateOfBirth(), p.phone(), p.email(), p.address(), p.id()) == 1;
    }

    public boolean delete(int id) {
        return sql.update("DELETE FROM patients WHERE id = ?", id) == 1;
    }

    /** Links a login to the patient, only if the patient doesn't have one already. */
    public boolean linkUser(int patientId, int userId) {
        return sql.update("UPDATE patients SET user_id = ? WHERE id = ? AND user_id IS NULL", userId, patientId) == 1;
    }

    public int count() {
        return sql.queryInt("SELECT COUNT(*) FROM patients");
    }

    static Patient map(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                Gender.valueOf(rs.getString("gender")),
                rs.getObject("date_of_birth", LocalDate.class),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getObject("user_id", Integer.class));
    }
}
