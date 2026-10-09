package com.hms.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DbConfigTest {

    @TempDir
    Path folder;

    @Test
    void defaultsWhenNothingIsConfigured() {
        DbConfig config = DbConfig.load(folder.resolve("missing.properties"), Map.of());
        assertEquals(DbConfig.DEFAULT_URL, config.url());
        assertEquals("root", config.user());
        assertEquals("", config.password());
    }

    @Test
    void readsThePropertiesFile() throws IOException {
        Path file = folder.resolve("db.properties");
        Files.writeString(file, "db.url=jdbc:mysql://db:3306/hms\ndb.user=hms\ndb.password=s3cret\n");
        DbConfig config = DbConfig.load(file, Map.of());
        assertEquals("jdbc:mysql://db:3306/hms", config.url());
        assertEquals("hms", config.user());
        assertEquals("s3cret", config.password());
    }

    @Test
    void environmentVariablesWin() throws IOException {
        Path file = folder.resolve("db.properties");
        Files.writeString(file, "db.user=hms\ndb.password=fromFile\n");
        DbConfig config = DbConfig.load(file, Map.of("HMS_DB_USER", "ci", "HMS_DB_PASSWORD", ""));
        assertEquals("ci", config.user());
        assertEquals("", config.password(), "an empty password set on purpose is respected");
    }

    @Test
    void toStringNeverShowsThePassword() {
        DbConfig config = new DbConfig("jdbc:mysql://x/hms", "root", "s3cret");
        assertFalse(config.toString().contains("s3cret"));
    }
}
