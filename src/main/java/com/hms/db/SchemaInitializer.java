package com.hms.db;

/**
 * Prepares the database when the app starts, so there is no manual SQL setup step.
 * The schema uses CREATE TABLE IF NOT EXISTS, so running it every time is safe.
 * Demo data is loaded only into an empty database (no users yet).
 */
public final class SchemaInitializer {

    static final String SCHEMA = "/db/schema.sql";
    static final String DEMO_DATA = "/db/demo-data.sql";

    private final Database db;

    public SchemaInitializer(Database db) {
        this.db = db;
    }

    /** Returns true if demo data was loaded on this run. */
    public boolean initialize() {
        createTables();
        if (db.queryInt("SELECT COUNT(*) FROM users") > 0) {
            return false;
        }
        loadDemoData();
        return true;
    }

    public void createTables() {
        // MySQL commits after every CREATE TABLE anyway, so these run one by one.
        for (String statement : SqlScript.split(SqlScript.readResource(SCHEMA))) {
            db.update(statement);
        }
    }

    private void loadDemoData() {
        // Plain INSERTs can share one transaction: either all demo rows load or none do.
        db.transaction(tx -> {
            for (String statement : SqlScript.split(SqlScript.readResource(DEMO_DATA))) {
                tx.update(statement);
            }
            return null;
        });
    }
}
