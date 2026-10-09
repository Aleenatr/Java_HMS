package com.hms.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.hms.model.AppointmentStatus;
import com.hms.model.BillStatus;
import com.hms.model.LabTestStatus;
import com.hms.model.RoomStatus;
import com.hms.model.StockLevel;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Insets;
import java.util.Map;

/** Colours and the FlatLaf look and feel, set once at start-up. */
public final class Theme {

    public static final Color ACCENT = new Color(0x0F766E);
    public static final Color ACCENT_DARK = new Color(0x115E59);
    public static final Color ACCENT_LIGHT = new Color(0xCCFBF1);
    public static final Color SIDEBAR = new Color(0xF8FAFC);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color MUTED_TEXT = new Color(0x64748B);
    public static final Color DANGER = new Color(0xB91C1C);
    public static final Color CARD = Color.WHITE;

    private static final Color GREEN = new Color(0x15803D);
    private static final Color AMBER = new Color(0xB45309);
    private static final Color BLUE = new Color(0x1D4ED8);
    private static final Color GREY = new Color(0x64748B);

    /** Status values are shown in colour in every table. */
    private static final Map<Enum<?>, Color> STATUS_COLOURS = Map.ofEntries(
            Map.entry(AppointmentStatus.SCHEDULED, BLUE),
            Map.entry(AppointmentStatus.COMPLETED, GREEN),
            Map.entry(AppointmentStatus.CANCELLED, GREY),
            Map.entry(BillStatus.UNPAID, AMBER),
            Map.entry(BillStatus.PAID, GREEN),
            Map.entry(RoomStatus.AVAILABLE, GREEN),
            Map.entry(RoomStatus.OCCUPIED, BLUE),
            Map.entry(RoomStatus.MAINTENANCE, GREY),
            Map.entry(LabTestStatus.ORDERED, AMBER),
            Map.entry(LabTestStatus.COMPLETED, GREEN),
            Map.entry(StockLevel.IN_STOCK, GREEN),
            Map.entry(StockLevel.REORDER, DANGER));

    private Theme() {
    }

    public static void install() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#0F766E"));
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("Table.cellMargins", new Insets(2, 10, 2, 10));
        UIManager.put("TableHeader.height", 32);
        UIManager.put("ScrollPane.smoothScrolling", true);
        UIManager.put("TabbedPane.showTabSeparators", true);
    }

    /** Colour for a status value, or null for "normal text colour". */
    public static Color statusColour(Object value) {
        return value instanceof Enum<?> e ? STATUS_COLOURS.get(e) : null;
    }
}
