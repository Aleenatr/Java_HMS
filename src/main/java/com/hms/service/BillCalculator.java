package com.hms.service;

import com.hms.model.Admission;
import com.hms.model.Dispense;
import com.hms.model.LabTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * The billing arithmetic, kept free of database code so it can be unit tested.
 * Money is BigDecimal, never double: 0.1 + 0.2 is exactly 0.3 here.
 */
public final class BillCalculator {

    private BillCalculator() {
    }

    /**
     * Days charged for a stay: the number of midnights between admission and discharge,
     * with a minimum of one day (a same-day discharge is still charged one day).
     */
    public static long chargeableDays(LocalDateTime admittedAt, LocalDateTime dischargedAt) {
        if (dischargedAt.isBefore(admittedAt)) {
            throw new IllegalArgumentException("Discharge is before admission");
        }
        long midnights = ChronoUnit.DAYS.between(admittedAt.toLocalDate(), dischargedAt.toLocalDate());
        return Math.max(1, midnights);
    }

    public static BigDecimal roomCharge(Admission stay) {
        long days = chargeableDays(stay.admittedAt(), stay.dischargedAt());
        return stay.dailyRate().multiply(BigDecimal.valueOf(days));
    }

    public static BigDecimal roomCharges(List<Admission> stays) {
        return stays.stream().map(BillCalculator::roomCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal medicineCharges(List<Dispense> dispenses) {
        return dispenses.stream().map(Dispense::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal labCharges(List<LabTest> tests) {
        return tests.stream().map(LabTest::price).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
