package com.hms.ui;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTableTest {

    @Test
    void textThatLooksLikeHtmlIsShownAsTyped() {
        // Swing would render this as HTML and even fetch the image; the leading space prevents it.
        assertEquals(" <html><img src='http://example.com/x.png'>",
                DataTable.displayValue("<html><img src='http://example.com/x.png'>"));
        assertEquals(" <HTML>shout", DataTable.displayValue("<HTML>shout"));
        assertEquals("Chest pain <html>", DataTable.displayValue("Chest pain <html>"));
    }

    @Test
    void valuesAreFormattedByType() {
        assertEquals("₹1,500.00", DataTable.displayValue(new BigDecimal("1500")));
        assertEquals("09-10-2026", DataTable.displayValue(LocalDate.of(2026, 10, 9)));
        assertEquals("Yes", DataTable.displayValue(true));
        assertEquals(42, DataTable.displayValue(42));
    }
}
