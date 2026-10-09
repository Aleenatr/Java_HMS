package com.hms.model;

public enum AppointmentStatus {
    SCHEDULED("Scheduled"), COMPLETED("Completed"), CANCELLED("Cancelled");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
