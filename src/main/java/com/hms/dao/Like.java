package com.hms.dao;

/** Builds LIKE patterns. % and _ typed by the user are escaped so they match literally. */
final class Like {

    private Like() {
    }

    static String contains(String text) {
        String escaped = text.strip()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
