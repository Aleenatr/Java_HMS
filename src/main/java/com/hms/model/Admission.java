package com.hms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A hospital stay. dailyRate is the room's rate copied at admission time, so a later
 * price change doesn't affect this patient's bill. dischargedAt is null while still admitted.
 */
public record Admission(int id, int patientId, String patientName, int roomId, String roomNumber,
                        int doctorId, String doctorName, String reason, BigDecimal dailyRate,
                        LocalDateTime admittedAt, LocalDateTime dischargedAt, Integer billId) {

    public boolean isOpen() {
        return dischargedAt == null;
    }
}
