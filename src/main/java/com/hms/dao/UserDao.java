package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.User;
import com.hms.security.Role;
import com.hms.util.Times;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class UserDao {

    /** A user together with the stored password hash. Only AuthService asks for this. */
    public record Credentials(User user, String passwordHash) {
    }

    // The joins pick up the doctor's or patient's name linked to each login.
    private static final String SELECT = """
            SELECT u.id, u.username, u.role, u.active, u.created_at, u.password_hash,
                   COALESCE(d.name, p.name, u.username) AS display_name
            FROM users u
            LEFT JOIN doctors d ON d.user_id = u.id
            LEFT JOIN patients p ON p.user_id = u.id
            """;

    private final SqlExecutor sql;

    public UserDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<User> findAll() {
        return sql.query(SELECT + " ORDER BY u.role, display_name", UserDao::map);
    }

    public Optional<User> findById(int id) {
        return sql.queryOne(SELECT + " WHERE u.id = ?", UserDao::map, id);
    }

    public Optional<Credentials> findCredentials(String username) {
        return sql.queryOne(SELECT + " WHERE u.username = ?",
                rs -> new Credentials(map(rs), rs.getString("password_hash")), username);
    }

    public boolean usernameExists(String username) {
        return sql.queryInt("SELECT COUNT(*) FROM users WHERE username = ?", username) > 0;
    }

    public int insert(String username, String passwordHash, Role role) {
        return sql.insert("INSERT INTO users (username, password_hash, role, created_at) VALUES (?, ?, ?, ?)",
                username, passwordHash, role, Times.now());
    }

    public boolean updatePasswordHash(int userId, String passwordHash) {
        return sql.update("UPDATE users SET password_hash = ? WHERE id = ?", passwordHash, userId) == 1;
    }

    public boolean setActive(int userId, boolean active) {
        return sql.update("UPDATE users SET active = ? WHERE id = ?", active, userId) == 1;
    }

    public boolean delete(int userId) {
        return sql.update("DELETE FROM users WHERE id = ?", userId) == 1;
    }

    static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("active"),
                rs.getString("display_name"),
                rs.getObject("created_at", LocalDateTime.class));
    }
}
