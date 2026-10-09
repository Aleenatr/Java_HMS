package com.hms.model;

public record Department(int id, String name, String phone) {

    @Override
    public String toString() {
        return name;
    }
}
