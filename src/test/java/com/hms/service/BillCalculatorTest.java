package com.hms.service;

import com.hms.model.Admission;
import com.hms.model.Dispense;
import com.hms.model.LabTest;
import com.hms.model.LabTestStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillCalculatorTest {

    private static final LocalDateTime MONDAY_10AM = LocalDateTime.of(2026, 10, 5, 10, 0);

    @Test
    void sameDayDischargeIsChargedOneDay() {
        assertEquals(1, BillCalculator.chargeableDays(MONDAY_10AM, MONDAY_10AM.plusHours(5)));
    }

    @Test
    void daysAreCountedByMidnightsPassed() {
        assertEquals(3, BillCalculator.chargeableDays(MONDAY_10AM, MONDAY_10AM.plusDays(3)));
        // Admitted 11 pm, discharged 1 am: one midnight, one day.
        LocalDateTime lateNight = LocalDateTime.of(2026, 10, 5, 23, 0);
        assertEquals(1, BillCalculator.chargeableDays(lateNight, lateNight.plusHours(2)));
    }

    @Test
    void dischargeBeforeAdmissionIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> BillCalculator.chargeableDays(MONDAY_10AM, MONDAY_10AM.minusMinutes(1)));
    }

    @Test
    void roomChargeIsRateTimesDays() {
        Admission stay = new Admission(1, 1, "P", 1, "301", 1, "D", "r", new BigDecimal("4500.00"),
                MONDAY_10AM, MONDAY_10AM.plusDays(4), null);
        assertEquals(new BigDecimal("18000.00"), BillCalculator.roomCharge(stay));
    }

    @Test
    void medicineAndLabChargesAreExactSums() {
        List<Dispense> medicines = List.of(
                new Dispense(1, 1, "P", 1, "A", 3, new BigDecimal("0.10"), MONDAY_10AM, null),
                new Dispense(2, 1, "P", 2, "B", 28, new BigDecimal("2.50"), MONDAY_10AM, null));
        // With double, 3 x 0.10 would be 0.30000000000000004. BigDecimal keeps it exact.
        assertEquals(new BigDecimal("70.30"), BillCalculator.medicineCharges(medicines));

        List<LabTest> tests = List.of(
                new LabTest(1, 1, "P", 1, "D", "ECG", new BigDecimal("300.00"), LabTestStatus.ORDERED, null,
                        MONDAY_10AM, null, null),
                new LabTest(2, 1, "P", 1, "D", "CBC", new BigDecimal("350.00"), LabTestStatus.COMPLETED, "ok",
                        MONDAY_10AM, MONDAY_10AM, null));
        assertEquals(new BigDecimal("650.00"), BillCalculator.labCharges(tests));
    }

    @Test
    void noChargesAddUpToZero() {
        assertEquals(BigDecimal.ZERO, BillCalculator.roomCharges(List.of()));
        assertEquals(BigDecimal.ZERO, BillCalculator.medicineCharges(List.of()));
        assertEquals(BigDecimal.ZERO, BillCalculator.labCharges(List.of()));
    }
}
