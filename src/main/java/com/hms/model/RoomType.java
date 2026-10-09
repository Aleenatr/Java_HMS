package com.hms.model;

public enum RoomType {
    GENERAL("General ward"), SEMI_PRIVATE("Semi-private"), PRIVATE("Private"), ICU("ICU");

    private final String label;

    RoomType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
