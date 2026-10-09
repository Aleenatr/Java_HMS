package com.hms.db;

import java.util.List;
import java.util.Optional;

/**
 * Runs SQL with {@code ?} placeholders. Values are always bound as parameters,
 * never pasted into the SQL text, which is what makes SQL injection impossible.
 *
 * <p>Two classes implement this: {@link Database} (each call commits on its own) and
 * {@link Transaction} (calls commit or roll back together). DAOs only see this interface,
 * so the same DAO works inside or outside a transaction.
 */
public interface SqlExecutor {

    <T> List<T> query(String sql, RowMapper<T> mapper, Object... params);

    /** Runs INSERT, UPDATE or DELETE and returns how many rows changed. */
    int update(String sql, Object... params);

    /** Runs an INSERT and returns the AUTO_INCREMENT id MySQL generated. */
    int insert(String sql, Object... params);

    default <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** For COUNT(*) and similar single-number queries. */
    default int queryInt(String sql, Object... params) {
        return queryOne(sql, rs -> rs.getInt(1), params).orElse(0);
    }
}
