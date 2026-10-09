package com.hms.model;

import com.hms.util.Formats;

import java.math.BigDecimal;

public record Medicine(int id, String name, String category, String manufacturer, BigDecimal unitPrice,
                       int stock, int reorderLevel) {

    /** Time to reorder once stock falls to the reorder level. */
    public boolean isLowStock() {
        return stock <= reorderLevel;
    }

    public StockLevel stockLevel() {
        return isLowStock() ? StockLevel.REORDER : StockLevel.IN_STOCK;
    }

    @Override
    public String toString() {
        return name + "  ·  " + Formats.money(unitPrice) + "  ·  " + stock + " in stock";
    }
}
