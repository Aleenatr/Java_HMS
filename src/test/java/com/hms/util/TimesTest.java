package com.hms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimesTest {

    @Test
    void nowHasNoFractionOfASecond() {
        assertEquals(0, Times.now().getNano());
    }
}
