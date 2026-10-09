package com.hms.service;

import com.hms.util.Formats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Input checks shared by every module. Each method returns the cleaned value
 * (trimmed text, parsed number) or throws a ValidationException that says what to fix.
 */
public final class Validator {

    private static final Pattern NAME = Pattern.compile("[\\p{L} .'-]+");
    private static final Pattern MOBILE = Pattern.compile("[6-9]\\d{9}");
    private static final Pattern LANDLINE = Pattern.compile("[0-9+\\- ]{6,20}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private Validator() {
    }

    public static String requireText(String field, String value, int maxLength) {
        String text = value == null ? "" : value.strip();
        if (text.isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
        if (text.length() > maxLength) {
            throw new ValidationException(field + " must be at most " + maxLength + " characters.");
        }
        return text;
    }

    /** Returns null for blank input, so empty optional fields are stored as NULL. */
    public static String optionalText(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(field, value, maxLength);
    }

    public static String requireName(String field, String value) {
        String name = requireText(field, value, 100);
        if (!NAME.matcher(name).matches()) {
            throw new ValidationException(field + " can only contain letters, spaces, dots, hyphens and apostrophes.");
        }
        return name;
    }

    /** Indian mobile number: 10 digits starting with 6-9. Spaces, dashes and a +91 prefix are allowed. */
    public static String requireMobile(String field, String value) {
        String digits = requireText(field, value, 20).replaceAll("[\\s-]", "");
        if (digits.startsWith("+91")) {
            digits = digits.substring(3);
        }
        if (!MOBILE.matcher(digits).matches()) {
            throw new ValidationException(field + " must be a 10-digit mobile number starting with 6, 7, 8 or 9.");
        }
        return digits;
    }

    public static String requireLandline(String field, String value) {
        String phone = requireText(field, value, 20);
        if (!LANDLINE.matcher(phone).matches()) {
            throw new ValidationException(field + " must be 6 to 20 digits (spaces, + and - allowed).");
        }
        return phone;
    }

    public static String optionalEmail(String value) {
        String email = optionalText("Email", value, 100);
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw new ValidationException("Email must look like name@example.com.");
        }
        return email;
    }

    public static <T> T requireSelected(String field, T value) {
        if (value == null) {
            throw new ValidationException("Please choose a " + field.toLowerCase() + ".");
        }
        return value;
    }

    /** Parses a date typed as DD-MM-YYYY. */
    public static LocalDate parseDate(String field, String text) {
        String value = requireText(field, text, 10);
        try {
            return Formats.parseDate(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field + " must be a real date in DD-MM-YYYY format, e.g. 25-12-1990.");
        }
    }

    public static LocalDate requireBirthDate(LocalDate date) {
        requireSelected("Date of birth", date);
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            throw new ValidationException("Date of birth cannot be in the future.");
        }
        if (date.isBefore(today.minusYears(130))) {
            throw new ValidationException("Date of birth is more than 130 years ago. Please check it.");
        }
        return date;
    }

    public static LocalDate requireNotFuture(String field, LocalDate date) {
        requireSelected(field, date);
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(field + " cannot be in the future.");
        }
        return date;
    }

    /** Money: zero or more, at most 2 decimal places, small enough for DECIMAL(10,2). */
    public static BigDecimal parseAmount(String field, String text) {
        String value = requireText(field, text, 15).replace(",", "");
        BigDecimal amount;
        try {
            amount = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a number, e.g. 1500 or 1500.50.");
        }
        if (amount.signum() < 0) {
            throw new ValidationException(field + " cannot be negative.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(field + " can have at most 2 decimal places.");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException(field + " is too large.");
        }
        return amount.setScale(2);
    }

    public static int parseCount(String field, String text, int min, int max) {
        String value = requireText(field, text, 9);
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
        if (number < min || number > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
        return number;
    }

    public static String requireUsername(String value) {
        String username = requireText("Username", value, 50);
        if (!USERNAME.matcher(username).matches()) {
            throw new ValidationException("Username must be 3-50 characters: letters, digits, dot, dash or underscore.");
        }
        return username;
    }

    /** At least 8 characters with at least one letter and one digit. */
    public static void requireStrongPassword(char[] password) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password) {
            hasLetter |= Character.isLetter(c);
            hasDigit |= Character.isDigit(c);
        }
        if (password.length < 8 || !hasLetter || !hasDigit) {
            throw new ValidationException("Password must be at least 8 characters and include a letter and a digit.");
        }
    }
}
