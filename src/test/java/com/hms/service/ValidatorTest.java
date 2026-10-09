package com.hms.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest {

    @Test
    void mobileNumbersAreCleanedAndChecked() {
        assertEquals("9812345601", Validator.requireMobile("Phone", " 98123-45601 "));
        assertEquals("9812345601", Validator.requireMobile("Phone", "+91 98123 45601"));
        assertThrows(ValidationException.class, () -> Validator.requireMobile("Phone", "12345"));
        assertThrows(ValidationException.class, () -> Validator.requireMobile("Phone", "5812345601"));
        assertThrows(ValidationException.class, () -> Validator.requireMobile("Phone", "98123456O1"));
        assertThrows(ValidationException.class, () -> Validator.requireMobile("Phone", ""));
    }

    @Test
    void textIsTrimmedAndLengthLimited() {
        assertEquals("Fever", Validator.requireText("Reason", "  Fever  ", 10));
        assertThrows(ValidationException.class, () -> Validator.requireText("Reason", "   ", 10));
        assertThrows(ValidationException.class, () -> Validator.requireText("Reason", "x".repeat(11), 10));
        assertNull(Validator.optionalText("Notes", "  ", 10));
    }

    @Test
    void namesAllowLettersAndPunctuationOnly() {
        assertEquals("Dr. Mary D'Souza-Rao", Validator.requireName("Name", "Dr. Mary D'Souza-Rao"));
        assertThrows(ValidationException.class, () -> Validator.requireName("Name", "Robert'); DROP TABLE"));
        assertThrows(ValidationException.class, () -> Validator.requireName("Name", "R2D2"));
    }

    @Test
    void emailIsOptionalButMustLookLikeAnEmail() {
        assertNull(Validator.optionalEmail(""));
        assertEquals("a@b.co", Validator.optionalEmail(" a@b.co "));
        assertThrows(ValidationException.class, () -> Validator.optionalEmail("not-an-email"));
    }

    @Test
    void datesUseDayMonthYearAndRejectImpossibleDates() {
        assertEquals(LocalDate.of(2024, 2, 29), Validator.parseDate("Date", "29-02-2024"));
        assertThrows(ValidationException.class, () -> Validator.parseDate("Date", "31-02-2024"));
        assertThrows(ValidationException.class, () -> Validator.parseDate("Date", "29-02-2023"));
        assertThrows(ValidationException.class, () -> Validator.parseDate("Date", "2024-02-01"));
    }

    @Test
    void birthDateCannotBeInTheFuture() {
        assertThrows(ValidationException.class, () -> Validator.requireBirthDate(LocalDate.now().plusDays(1)));
        assertThrows(ValidationException.class, () -> Validator.requireBirthDate(LocalDate.now().minusYears(131)));
        assertEquals(LocalDate.now(), Validator.requireBirthDate(LocalDate.now()));
    }

    @Test
    void amountsAreExactWithTwoDecimals() {
        assertEquals(new BigDecimal("1500.50"), Validator.parseAmount("Rate", "1500.5"));
        assertEquals(new BigDecimal("1500.00"), Validator.parseAmount("Rate", "1,500"));
        assertThrows(ValidationException.class, () -> Validator.parseAmount("Rate", "-1"));
        assertThrows(ValidationException.class, () -> Validator.parseAmount("Rate", "1.234"));
        assertThrows(ValidationException.class, () -> Validator.parseAmount("Rate", "abc"));
        assertThrows(ValidationException.class, () -> Validator.parseAmount("Rate", "100000000"));
    }

    @Test
    void countsMustBeWholeNumbersInRange() {
        assertEquals(5, Validator.parseCount("Qty", "5", 1, 10));
        assertThrows(ValidationException.class, () -> Validator.parseCount("Qty", "0", 1, 10));
        assertThrows(ValidationException.class, () -> Validator.parseCount("Qty", "2.5", 1, 10));
        assertThrows(ValidationException.class, () -> Validator.parseCount("Qty", "11", 1, 10));
    }

    @Test
    void passwordsNeedEightCharactersWithALetterAndADigit() {
        Validator.requireStrongPassword("abcdefg1".toCharArray());
        assertThrows(ValidationException.class, () -> Validator.requireStrongPassword("abc1".toCharArray()));
        assertThrows(ValidationException.class, () -> Validator.requireStrongPassword("abcdefgh".toCharArray()));
        assertThrows(ValidationException.class, () -> Validator.requireStrongPassword("12345678".toCharArray()));
    }

    @Test
    void usernamesAreSimple() {
        assertEquals("dr.mehta", Validator.requireUsername("dr.mehta"));
        assertThrows(ValidationException.class, () -> Validator.requireUsername("ab"));
        assertThrows(ValidationException.class, () -> Validator.requireUsername("admin' --"));
    }
}
