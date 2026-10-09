package com.hms.model;

import java.time.LocalDate;
import java.time.Period;

/** A patient. id is 0 for a patient that has not been saved yet. */
public record Patient(int id, String name, Gender gender, LocalDate dateOfBirth, String phone, String email,
                      String address, Integer userId) {

    /** Age is calculated, not stored, so it is always correct. */
    public int age() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return name + "  ·  #" + id;
    }
}
