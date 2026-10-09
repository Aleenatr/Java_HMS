package com.hms.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatsTest {

    @Test
    void moneyIsShownInRupeesWithTwoDecimals() {
        assertEquals("₹123,456.50", Formats.money(new BigDecimal("123456.5")));
        assertEquals("₹0.00", Formats.money(null));
    }

    @Test
    void datesAndTimesUseIndianFormat() {
        assertEquals("09-10-2026", Formats.date(LocalDate.of(2026, 10, 9)));
        assertEquals("02:30 PM", Formats.time(LocalTime.of(14, 30)));
        assertEquals("", Formats.date(null));
    }
}
