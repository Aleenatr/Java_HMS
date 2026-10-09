package com.hms.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LikeTest {

    @Test
    void wrapsTextInWildcards() {
        assertEquals("%priya%", Like.contains("  priya "));
    }

    @Test
    void userTypedWildcardsMatchLiterally() {
        assertEquals("%50\\%\\_off%", Like.contains("50%_off"));
    }
}
