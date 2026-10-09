package com.hms.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Turns the current row of a ResultSet into a Java object. DAOs pass these as lambdas. */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
