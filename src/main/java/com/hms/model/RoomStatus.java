package com.hms.model;

public enum RoomStatus {
    AVAILABLE("Available"), OCCUPIED("Occupied"), MAINTENANCE("Under maintenance");

    private final String label;

    RoomStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
