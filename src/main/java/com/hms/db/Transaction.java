package com.hms.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Runs every statement on one connection with auto-commit switched off,
 * so they are saved together or not at all. Created only by {@link Database#transaction}.
 */
public final class Transaction implements SqlExecutor {

    private final Connection connection;

    Transaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try {
            return Jdbc.query(connection, sql, mapper, params);
        } catch (SQLException e) {
            throw new DataAccessException("Query failed", e);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        try {
            return Jdbc.update(connection, sql, params);
        } catch (SQLException e) {
            throw new DataAccessException("Update failed", e);
        }
    }

    @Override
    public int insert(String sql, Object... params) {
        try {
            return Jdbc.insert(connection, sql, params);
        } catch (SQLException e) {
            throw new DataAccessException("Insert failed", e);
        }
    }
}
