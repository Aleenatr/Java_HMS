package com.hms.model;

import com.hms.util.Formats;

import java.math.BigDecimal;

/** An entry in the lab's price list. */
public record LabTestType(int id, String name, BigDecimal price) {

    @Override
    public String toString() {
        return name + "  ·  " + Formats.money(price);
    }
}
