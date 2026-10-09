package com.hms.config;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Where to find MySQL and how to log in to it.
 *
 * <p>Values are read in this order, first match wins:
 * environment variables (HMS_DB_URL, HMS_DB_USER, HMS_DB_PASSWORD),
 * then the file {@code db.properties} in the working folder, then the defaults below.
 * Keeping the password out of the source code means it never ends up on GitHub.
 */
public record DbConfig(String url, String user, String password) {

    public static final String FILE_NAME = "db.properties";
    static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/hms?createDatabaseIfNotExist=true";
    static final String DEFAULT_USER = "root";

    public static DbConfig load() {
        return load(Path.of(FILE_NAME), System.getenv());
    }

    static DbConfig load(Path file, Map<String, String> env) {
        Properties props = new Properties();
        if (Files.isRegularFile(file)) {
            try (Reader in = Files.newBufferedReader(file)) {
                props.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read " + file.toAbsolutePath(), e);
            }
        }
        return new DbConfig(
                firstNonBlank(env.get("HMS_DB_URL"), props.getProperty("db.url"), DEFAULT_URL),
                firstNonBlank(env.get("HMS_DB_USER"), props.getProperty("db.user"), DEFAULT_USER),
                firstNonNull(env.get("HMS_DB_PASSWORD"), props.getProperty("db.password"), ""));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        throw new IllegalStateException("unreachable: the last value is a default");
    }

    // An empty password is a valid setting, so only a missing value falls through.
    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        throw new IllegalStateException("unreachable: the last value is a default");
    }

    /** Records print every field by default; this one must never print the password. */
    @Override
    public String toString() {
        return "DbConfig[url=" + url + ", user=" + user + ", password=****]";
    }
}
