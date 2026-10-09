package com.hms.model;

/**
 * A doctor. departmentName is filled from a JOIN for display; saving only uses departmentId.
 * userId is the doctor's login account, or null if they don't have one yet.
 */
public record Doctor(int id, String name, String specialization, int departmentId, String departmentName,
                     String phone, String email, Integer userId) {

    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }
}
