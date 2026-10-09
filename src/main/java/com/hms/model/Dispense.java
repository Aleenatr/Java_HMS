package com.hms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Medicine handed to a patient. unitPrice is the price at that moment. */
public record Dispense(int id, int patientId, String patientName, int medicineId, String medicineName,
                       int quantity, BigDecimal unitPrice, LocalDateTime dispensedAt, Integer billId) {

    public BigDecimal amount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
