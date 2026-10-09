package com.hms.security;

/**
 * Things a user may be allowed to do. Code checks permissions, not roles,
 * so adding a new role later (say, Nurse) only means giving it a set of these.
 */
public enum Permission {
    VIEW_PATIENTS("view patients"),
    MANAGE_PATIENTS("add, edit or delete patients"),
    MANAGE_DOCTORS("add, edit or delete doctors"),
    MANAGE_ROOMS("manage rooms"),
    MANAGE_ADMISSIONS("admit or discharge patients"),
    BOOK_APPOINTMENTS("book or cancel appointments"),
    COMPLETE_APPOINTMENTS("mark appointments as completed"),
    WRITE_MEDICAL_RECORDS("write medical records"),
    VIEW_MEDICAL_RECORDS("view medical records"),
    ORDER_LAB_TESTS("order lab tests"),
    RECORD_LAB_RESULTS("record lab results"),
    VIEW_LAB_TESTS("view lab tests"),
    MANAGE_PHARMACY("manage the pharmacy"),
    MANAGE_BILLING("generate bills or record payments"),
    VIEW_BILLS("view bills"),
    MANAGE_USERS("manage user accounts");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
