package com.hms.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/** How money, dates and times are shown and typed everywhere in the app (Indian formats). */
public final class Formats {

    public static final String DATE_PATTERN = "dd-MM-uuuu";
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern(DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final Locale INDIA = Locale.of("en", "IN");

    private Formats() {
    }

    /** 123456.5 becomes "₹123,456.50". */
    public static String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(INDIA);
        return format.format(amount == null ? BigDecimal.ZERO : amount);
    }

    public static String date(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    public static String dateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_TIME.format(dateTime);
    }

    public static String time(LocalTime time) {
        return time == null ? "" : TIME.format(time);
    }

    /** Parses "25-12-1990". STRICT rejects impossible dates such as 31-02-2024. */
    public static LocalDate parseDate(String text) throws DateTimeParseException {
        return LocalDate.parse(text.strip(), DATE);
    }
}
