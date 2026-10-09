package com.hms.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    // A low iteration count keeps the tests fast; the algorithm is the same.
    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    void correctPasswordVerifies() {
        String stored = hasher.hash("Secret123".toCharArray());
        assertTrue(hasher.verify("Secret123".toCharArray(), stored));
    }

    @Test
    void wrongPasswordFails() {
        String stored = hasher.hash("Secret123".toCharArray());
        assertFalse(hasher.verify("Secret124".toCharArray(), stored));
        assertFalse(hasher.verify("".toCharArray(), stored));
    }

    @Test
    void samePasswordGivesDifferentHashesBecauseOfTheSalt() {
        assertNotEquals(hasher.hash("Secret123".toCharArray()), hasher.hash("Secret123".toCharArray()));
    }

    @Test
    void passwordIsNeverStoredInPlainText() {
        String stored = hasher.hash("Secret123".toCharArray());
        assertFalse(stored.contains("Secret123"));
        assertTrue(stored.startsWith("pbkdf2$1000$"));
    }

    @Test
    void hashMadeWithOtherIterationCountStillVerifies() {
        String stored = new PasswordHasher(2_000).hash("Secret123".toCharArray());
        assertTrue(hasher.verify("Secret123".toCharArray(), stored));
    }

    @Test
    void tamperedOrMalformedHashesFail() {
        String stored = hasher.hash("Secret123".toCharArray());
        String tampered = stored.substring(0, stored.length() - 4) + "AAA=";
        assertFalse(hasher.verify("Secret123".toCharArray(), tampered));
        assertFalse(hasher.verify("Secret123".toCharArray(), null));
        assertFalse(hasher.verify("Secret123".toCharArray(), ""));
        assertFalse(hasher.verify("Secret123".toCharArray(), "Secret123"));
        assertFalse(hasher.verify("Secret123".toCharArray(), "md5$1$abc$def"));
        assertFalse(hasher.verify("Secret123".toCharArray(), "pbkdf2$x$abc$def"));
        assertFalse(hasher.verify("Secret123".toCharArray(), "pbkdf2$1000$not base64!$def"));
        assertFalse(hasher.verify("Secret123".toCharArray(), "pbkdf2$1000$$"));
    }
}
