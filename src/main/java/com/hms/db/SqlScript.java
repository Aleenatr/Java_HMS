package com.hms.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits a .sql file into single statements at each ';'.
 * Semicolons inside quotes or comments are not treated as the end of a statement.
 */
public final class SqlScript {

    private SqlScript() {
    }

    public static String readResource(String path) {
        try (InputStream in = SqlScript.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path, e);
        }
    }

    public static List<String> split(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int i = 0;
        while (i < script.length()) {
            char c = script.charAt(i);
            if (c == '-' && script.startsWith("--", i)) {
                i = skipUntil(script, i, "\n");
            } else if (c == '/' && script.startsWith("/*", i)) {
                i = skipUntil(script, i + 2, "*/");
            } else if (c == '\'' || c == '"' || c == '`') {
                int end = endOfQuoted(script, i);
                current.append(script, i, end);
                i = end;
            } else if (c == ';') {
                addIfNotBlank(statements, current);
                current.setLength(0);
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        addIfNotBlank(statements, current);
        return statements;
    }

    /** Returns the index just after {@code marker}, or the end of the script. */
    private static int skipUntil(String script, int from, String marker) {
        int at = script.indexOf(marker, from);
        return at < 0 ? script.length() : at + marker.length();
    }

    /** Index just after the closing quote. Handles doubled quotes ('') and backslash escapes (\'). */
    private static int endOfQuoted(String script, int start) {
        char quote = script.charAt(start);
        int i = start + 1;
        while (i < script.length()) {
            char c = script.charAt(i);
            if (c == '\\' && quote != '`') {
                i += 2;
            } else if (c == quote) {
                if (i + 1 < script.length() && script.charAt(i + 1) == quote) {
                    i += 2;
                } else {
                    return i + 1;
                }
            } else {
                i++;
            }
        }
        return script.length();
    }

    private static void addIfNotBlank(List<String> statements, StringBuilder sql) {
        String statement = sql.toString().strip();
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }
}
