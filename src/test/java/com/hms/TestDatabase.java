package com.hms;

import com.hms.config.DbConfig;
import com.hms.db.Database;
import com.hms.db.SchemaInitializer;
import com.hms.model.User;
import com.hms.security.Role;
import com.hms.security.Session;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

/**
 * Gives database tests a fresh MySQL database with the schema and demo data.
 *
 * <p>Set HMS_TEST_DB_URL (and HMS_TEST_DB_USER / HMS_TEST_DB_PASSWORD) to run them, e.g.
 * {@code jdbc:mysql://localhost:3306/hms_test?createDatabaseIfNotExist=true}.
 * Without it, database tests are skipped and only the unit tests run.
 * As a safety net, tables are only wiped if the database name contains "test".
 */
public final class TestDatabase {

    private TestDatabase() {
    }

    public static boolean isConfigured() {
        String url = System.getenv("HMS_TEST_DB_URL");
        return url != null && !url.isBlank();
    }

    public static Database fresh() {
        Assumptions.assumeTrue(isConfigured(),
                "Set HMS_TEST_DB_URL to run database tests");
        Database db = new Database(new DbConfig(
                System.getenv("HMS_TEST_DB_URL"),
                envOr("HMS_TEST_DB_USER", "root"),
                envOr("HMS_TEST_DB_PASSWORD", "")));
        dropAllTables(db);
        new SchemaInitializer(db).initialize();
        return db;
    }

    private static void dropAllTables(Database db) {
        String name = db.queryOne("SELECT DATABASE()", rs -> rs.getString(1)).orElse("");
        if (!name.toLowerCase().contains("test")) {
            throw new IllegalStateException("Refusing to wipe database '" + name
                    + "': the test database's name must contain 'test'.");
        }
        List<String> tables = db.query(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                rs -> rs.getString(1));
        // One connection, so the FOREIGN_KEY_CHECKS setting applies to every DROP.
        db.transaction(tx -> {
            tx.update("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : tables) {
                tx.update("DROP TABLE IF EXISTS `" + table + "`");
            }
            tx.update("SET FOREIGN_KEY_CHECKS = 1");
            return null;
        });
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    // Sessions matching the demo data (see src/main/resources/db/demo-data.sql).

    public static Session admin() {
        return new Session(new User(1, "admin", Role.ADMIN, true, "admin", null), null, null);
    }

    /** Dr. Arjun Mehta, doctor #1 (Cardiology). */
    public static Session drMehta() {
        return new Session(new User(2, "dr.mehta", Role.DOCTOR, true, "Dr. Arjun Mehta", null), 1, null);
    }

    /** Dr. Kavita Rao, doctor #2 (Pediatrics). */
    public static Session drRao() {
        return new Session(new User(3, "dr.rao", Role.DOCTOR, true, "Dr. Kavita Rao", null), 2, null);
    }

    /** Priya Sharma, patient #1, an outpatient with unbilled medicines and lab tests. */
    public static Session priya() {
        return new Session(new User(4, "priya", Role.PATIENT, true, "Priya Sharma", null), null, 1);
    }

    /** Rahul Verma, patient #2, currently admitted in room 301. */
    public static Session rahul() {
        return new Session(new User(5, "rahul", Role.PATIENT, true, "Rahul Verma", null), null, 2);
    }
}
