package com.hms.ui;

import java.util.function.Function;

/**
 * One column of a {@link DataTable}: its heading, how to read the value from a row object,
 * and a preferred width in pixels.
 */
public record Column<T>(String header, Function<T, ?> value, int width) {

    public static <T> Column<T> of(String header, Function<T, ?> value, int width) {
        return new Column<>(header, value, width);
    }
}
