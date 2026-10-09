package com.hms.model;

public enum StockLevel {
    IN_STOCK("In stock"), REORDER("Reorder now");

    private final String label;

    StockLevel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
