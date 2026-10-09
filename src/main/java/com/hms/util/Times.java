package com.hms.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** The app's clock. */
public final class Times {

    private Times() {
    }

    /**
     * The current time in whole seconds. MySQL DATETIME columns keep whole seconds and round
     * any fraction, so 10:00:00.7 would be saved as 10:00:01, a moment in the future. Cutting
     * the fraction off first keeps saved times and in-app times in step.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
