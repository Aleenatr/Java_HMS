package com.hms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A test ordered for a patient. price is copied from the price list when ordered. */
public record LabTest(int id, int patientId, String patientName, int doctorId, String doctorName,
                      String testName, BigDecimal price, LabTestStatus status, String result,
                      LocalDateTime orderedAt, LocalDateTime completedAt, Integer billId) {
}
