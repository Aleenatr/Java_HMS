package com.hms.model;

import java.time.LocalDate;

/** One visit's clinical notes, written by a doctor. */
public record MedicalRecord(int id, int patientId, String patientName, int doctorId, String doctorName,
                            LocalDate visitDate, String diagnosis, String prescription, String notes) {
}
