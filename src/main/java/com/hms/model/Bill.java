package com.hms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A bill made of charges from three modules: room stays, pharmacy and lab. */
public record Bill(int id, int patientId, String patientName, BigDecimal roomCharges,
                   BigDecimal medicineCharges, BigDecimal labCharges, BigDecimal total,
                   BillStatus status, LocalDateTime createdAt, LocalDateTime paidAt) {
}
