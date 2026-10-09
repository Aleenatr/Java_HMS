package com.hms.model;

import com.hms.util.Formats;

import java.math.BigDecimal;

public record Room(int id, String roomNumber, RoomType type, BigDecimal dailyRate, RoomStatus status) {

    @Override
    public String toString() {
        return "Room " + roomNumber + "  ·  " + type + "  ·  " + Formats.money(dailyRate) + "/day";
    }
}
