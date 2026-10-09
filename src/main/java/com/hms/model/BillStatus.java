package com.hms.model;

public enum BillStatus {
    UNPAID("Unpaid"), PAID("Paid");

    private final String label;

    BillStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
