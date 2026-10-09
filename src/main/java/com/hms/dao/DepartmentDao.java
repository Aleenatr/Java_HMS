package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Department;

import java.util.List;

public final class DepartmentDao {

    private final SqlExecutor sql;

    public DepartmentDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Department> findAll() {
        return sql.query("SELECT id, name, phone FROM departments ORDER BY name",
                rs -> new Department(rs.getInt("id"), rs.getString("name"), rs.getString("phone")));
    }

    public int insert(String name, String phone) {
        return sql.insert("INSERT INTO departments (name, phone) VALUES (?, ?)", name, phone);
    }
}
