package com.hms.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlScriptTest {

    @Test
    void splitsAtSemicolons() {
        assertEquals(List.of("SELECT 1", "SELECT 2"), SqlScript.split("SELECT 1;\nSELECT 2;"));
    }

    @Test
    void lastStatementMayLackASemicolon() {
        assertEquals(List.of("SELECT 1", "SELECT 2"), SqlScript.split("SELECT 1; SELECT 2"));
    }

    @Test
    void semicolonsInsideQuotesAreKept() {
        assertEquals(List.of("INSERT INTO t VALUES ('a;b', 'it''s; fine', 'x\\';y')"),
                SqlScript.split("INSERT INTO t VALUES ('a;b', 'it''s; fine', 'x\\';y');"));
    }

    @Test
    void commentsAreDropped() {
        String script = """
                -- a comment; with a semicolon
                SELECT 1; /* block; comment */ SELECT 2;
                """;
        assertEquals(List.of("SELECT 1", "SELECT 2"), SqlScript.split(script));
    }

    @Test
    void emptyScriptHasNoStatements() {
        assertTrue(SqlScript.split("  \n -- only a comment\n").isEmpty());
    }

    @Test
    void bundledScriptsSplitIntoTheExpectedStatements() {
        List<String> schema = SqlScript.split(SqlScript.readResource(SchemaInitializer.SCHEMA));
        assertEquals(13, schema.size(), "one CREATE TABLE per table");
        assertTrue(schema.stream().allMatch(s -> s.startsWith("CREATE TABLE IF NOT EXISTS")));
        assertTrue(SqlScript.split(SqlScript.readResource(SchemaInitializer.DEMO_DATA)).stream()
                .allMatch(s -> s.startsWith("INSERT INTO")));
    }
}
