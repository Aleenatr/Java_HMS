package com.hms.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Appointment(int id, int patientId, String patientName, int doctorId, String doctorName,
                          LocalDate date, LocalTime slot, String reason, AppointmentStatus status) {
}
