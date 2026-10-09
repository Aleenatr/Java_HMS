package com.hms.service;

import com.hms.model.Admission;
import com.hms.model.Bill;
import com.hms.model.BillDetails;
import com.hms.model.Dispense;
import com.hms.model.LabTest;
import com.hms.util.Formats;

import java.math.BigDecimal;

/** Lays out an itemised bill as fixed-width text, ready to print or save. */
public final class BillFormatter {

    private static final int WIDTH = 64;
    private static final String DOUBLE_LINE = "=".repeat(WIDTH) + "\n";
    private static final String SINGLE_LINE = "-".repeat(WIDTH) + "\n";

    private BillFormatter() {
    }

    public static String format(BillDetails details) {
        Bill bill = details.bill();
        StringBuilder out = new StringBuilder();
        out.append(DOUBLE_LINE);
        out.append(center("HOSPITAL MANAGEMENT SYSTEM")).append(center("PATIENT BILL"));
        out.append(DOUBLE_LINE);
        out.append(pair("Bill no.", "#" + bill.id(), "Date", Formats.dateTime(bill.createdAt())));
        out.append(pair("Patient", details.patient().name() + " (#" + details.patient().id() + ")",
                "Phone", details.patient().phone()));
        String status = bill.status().toString()
                + (bill.paidAt() == null ? "" : " on " + Formats.dateTime(bill.paidAt()));
        out.append(String.format("%-10s: %s%n", "Status", status));
        out.append(SINGLE_LINE);

        if (!details.admissions().isEmpty()) {
            out.append("ROOM CHARGES\n");
            for (Admission stay : details.admissions()) {
                long days = BillCalculator.chargeableDays(stay.admittedAt(), stay.dischargedAt());
                out.append(line("Room " + stay.roomNumber() + " (" + days + (days == 1 ? " day)" : " days)"),
                        days + " x " + Formats.money(stay.dailyRate()), BillCalculator.roomCharge(stay)));
                out.append("    ").append(Formats.date(stay.admittedAt().toLocalDate())).append(" to ")
                        .append(Formats.date(stay.dischargedAt().toLocalDate())).append('\n');
            }
        }
        if (!details.dispenses().isEmpty()) {
            out.append("PHARMACY\n");
            for (Dispense item : details.dispenses()) {
                out.append(line(item.medicineName(), item.quantity() + " x " + Formats.money(item.unitPrice()),
                        item.amount()));
            }
        }
        if (!details.labTests().isEmpty()) {
            out.append("LAB TESTS\n");
            for (LabTest test : details.labTests()) {
                out.append(line(test.testName(), "", test.price()));
            }
        }
        out.append(SINGLE_LINE);
        out.append(total("Room charges", bill.roomCharges()));
        out.append(total("Medicine charges", bill.medicineCharges()));
        out.append(total("Lab charges", bill.labCharges()));
        out.append(SINGLE_LINE);
        out.append(total("TOTAL", bill.total()));
        out.append(DOUBLE_LINE);
        return out.toString();
    }

    private static String center(String text) {
        int padding = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(padding) + text + "\n";
    }

    private static String pair(String label1, String value1, String label2, String value2) {
        return String.format("%-10s: %-26s %-6s: %s%n", label1, value1, label2, value2);
    }

    // Columns: 2 + 30 + 1 + 16 + 1 + 14 = 64 characters, so every amount ends in the same place.
    private static String line(String label, String detail, BigDecimal amount) {
        return String.format("  %-30.30s %16s %14s%n", label, detail, Formats.money(amount));
    }

    private static String total(String label, BigDecimal amount) {
        return String.format("%-49s %14s%n", label, Formats.money(amount));
    }
}
