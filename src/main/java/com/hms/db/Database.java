package com.hms.db;

import com.hms.config.DbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

/**
 * Entry point to MySQL. Each plain call opens a connection, runs one statement
 * (auto-commit) and closes the connection again. A desktop app with one user at a
 * time does not need a connection pool; a server version would add one (e.g. HikariCP).
 */
public final class Database implements SqlExecutor {

    private final DbConfig config;

    public Database(DbConfig config) {
        this.config = config;
    }

    public DbConfig config() {
        return config;
    }

    Connection openConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    /** Fails fast with a clear message if MySQL is not reachable or the login is wrong. */
    public void checkConnection() {
        try (Connection connection = openConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("MySQL accepted the login but the connection is not usable");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Cannot connect to MySQL at " + config.url(), e);
        }
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = openConnection()) {
            return Jdbc.query(connection, sql, mapper, params);
        } catch (SQLException e) {
            throw new DataAccessException("Query failed", e);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        try (Connection connection = openConnection()) {
            return Jdbc.update(connection, sql, params);
        } catch (SQLException e) {
            throw new DataAccessException("Update failed", e);
        }
    }

    @Override
    public int insert(String sql, Object... params) {
        try (Connection connection = openConnection()) {
            return Jdbc.insert(connection, sql, params);
        } catch (SQLException e) {
            throw new DataAccessException("Insert failed", e);
        }
    }

    /**
     * Runs {@code work} as one transaction: commit if it returns normally,
     * roll back if it throws. Used wherever several tables must change together,
     * e.g. discharge = close the admission + free the room + create the bill.
     */
    public <T> T transaction(Function<Transaction, T> work) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.apply(new Transaction(connection));
                connection.commit();
                return result;
            } catch (RuntimeException e) {
                rollbackQuietly(connection, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transaction failed", e);
        }
    }

    private static void rollbackQuietly(Connection connection, RuntimeException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}
