package com.hms.dao;

import com.hms.db.SqlExecutor;
import com.hms.model.Medicine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class MedicineDao {

    private static final String SELECT =
            "SELECT id, name, category, manufacturer, unit_price, stock, reorder_level FROM medicines";

    private final SqlExecutor sql;

    public MedicineDao(SqlExecutor sql) {
        this.sql = sql;
    }

    public List<Medicine> findAll() {
        return sql.query(SELECT + " ORDER BY name", MedicineDao::map);
    }

    public List<Medicine> search(String text) {
        String pattern = Like.contains(text);
        return sql.query(SELECT + " WHERE name LIKE ? OR category LIKE ? ORDER BY name", MedicineDao::map,
                pattern, pattern);
    }

    public Optional<Medicine> findById(int id) {
        return sql.queryOne(SELECT + " WHERE id = ?", MedicineDao::map, id);
    }

    public int insert(Medicine m) {
        return sql.insert("""
                INSERT INTO medicines (name, category, manufacturer, unit_price, stock, reorder_level)
                VALUES (?, ?, ?, ?, ?, ?)""",
                m.name(), m.category(), m.manufacturer(), m.unitPrice(), m.stock(), m.reorderLevel());
    }

    /** Stock is not edited here; it only changes through restock() and takeStock(). */
    public boolean update(Medicine m) {
        return sql.update("""
                UPDATE medicines SET name = ?, category = ?, manufacturer = ?, unit_price = ?, reorder_level = ?
                WHERE id = ?""",
                m.name(), m.category(), m.manufacturer(), m.unitPrice(), m.reorderLevel(), m.id()) == 1;
    }

    public boolean restock(int medicineId, int quantity) {
        return sql.update("UPDATE medicines SET stock = stock + ? WHERE id = ?", quantity, medicineId) == 1;
    }

    /**
     * Removes stock in one statement. "stock >= ?" means the row only changes if there is
     * enough, so stock can never go below zero even with two pharmacists dispensing at once.
     */
    public boolean takeStock(int medicineId, int quantity) {
        return sql.update("UPDATE medicines SET stock = stock - ? WHERE id = ? AND stock >= ?",
                quantity, medicineId, quantity) == 1;
    }

    public boolean delete(int id) {
        return sql.update("DELETE FROM medicines WHERE id = ?", id) == 1;
    }

    public int countLowStock() {
        return sql.queryInt("SELECT COUNT(*) FROM medicines WHERE stock <= reorder_level");
    }

    static Medicine map(ResultSet rs) throws SQLException {
        return new Medicine(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("manufacturer"),
                rs.getBigDecimal("unit_price"),
                rs.getInt("stock"),
                rs.getInt("reorder_level"));
    }
}
