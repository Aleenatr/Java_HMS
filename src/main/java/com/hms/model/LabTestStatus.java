package com.hms.model;

public enum LabTestStatus {
    ORDERED("Ordered"), COMPLETED("Completed");

    private final String label;

    LabTestStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
