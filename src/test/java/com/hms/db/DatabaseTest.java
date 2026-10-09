package com.hms.db;

import com.hms.TestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The JDBC layer: transactions, error translation and the start-up schema check. */
class DatabaseTest {

    private Database db;

    @BeforeEach
    void freshDatabase() {
        db = TestDatabase.fresh();
    }

    @Test
    void transactionCommitsWhenWorkSucceeds() {
        db.transaction(tx -> tx.insert("INSERT INTO departments (name, phone) VALUES (?, ?)", "Dermatology", "011-1"));
        assertEquals(1, db.queryInt("SELECT COUNT(*) FROM departments WHERE name = 'Dermatology'"));
    }

    @Test
    void transactionRollsBackEverythingWhenWorkFails() {
        assertThrows(IllegalStateException.class, () -> db.transaction(tx -> {
            tx.insert("INSERT INTO departments (name, phone) VALUES (?, ?)", "Dermatology", "011-1");
            tx.update("UPDATE rooms SET status = 'MAINTENANCE'");
            throw new IllegalStateException("something went wrong half way");
        }));
        assertEquals(0, db.queryInt("SELECT COUNT(*) FROM departments WHERE name = 'Dermatology'"));
        assertEquals(1, db.queryInt("SELECT COUNT(*) FROM rooms WHERE status = 'MAINTENANCE'"),
                "only the room that was already under maintenance");
    }

    @Test
    void mysqlErrorsAreTranslated() {
        DataAccessException duplicate = assertThrows(DataAccessException.class,
                () -> db.insert("INSERT INTO departments (name, phone) VALUES ('Cardiology', '1')"));
        assertTrue(duplicate.isDuplicateKey());

        DataAccessException referenced = assertThrows(DataAccessException.class,
                () -> db.update("DELETE FROM doctors WHERE id = 1"));
        assertTrue(referenced.isRowStillReferenced());

        DataAccessException missing = assertThrows(DataAccessException.class,
                () -> db.insert("INSERT INTO doctors (name, specialization, department_id, phone) VALUES ('X', 'Y', 999, '9')"));
        assertTrue(missing.isMissingReference());

        DataAccessException check = assertThrows(DataAccessException.class,
                () -> db.update("UPDATE medicines SET stock = -1 WHERE id = 1"));
        assertTrue(check.isCheckViolation());
    }

    @Test
    void parametersAreDataNotSql() {
        String attack = "x' OR '1'='1";
        assertEquals(0, db.queryInt("SELECT COUNT(*) FROM users WHERE username = ?", attack));
        db.insert("INSERT INTO departments (name, phone) VALUES (?, ?)", "Robert'); DROP TABLE users; --", "1");
        assertEquals(5, db.queryInt("SELECT COUNT(*) FROM users"), "the users table is still there");
    }

    @Test
    void initialiserIsSafeToRunAgain() {
        SchemaInitializer initializer = new SchemaInitializer(db);
        assertFalse(initializer.initialize(), "demo data only loads into an empty database");
        assertEquals(13, db.queryInt(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()"));
        assertEquals(5, db.queryInt("SELECT COUNT(*) FROM users"));
    }

    @Test
    void demoDataIsConsistent() {
        // Every occupied room has exactly one open admission, and vice versa.
        assertEquals(0, db.queryInt("""
                SELECT COUNT(*) FROM rooms r
                WHERE (r.status = 'OCCUPIED') <>
                      EXISTS (SELECT 1 FROM admissions a WHERE a.room_id = r.id AND a.discharged_at IS NULL)"""));
        // The paid demo bill equals the charges attached to it.
        assertEquals(0, db.queryInt("""
                SELECT COUNT(*) FROM bills b
                WHERE b.medicine_charges <> (SELECT COALESCE(SUM(quantity * unit_price), 0)
                                             FROM medicine_dispenses WHERE bill_id = b.id)
                   OR b.lab_charges <> (SELECT COALESCE(SUM(price), 0) FROM lab_tests WHERE bill_id = b.id)"""));
    }
}
