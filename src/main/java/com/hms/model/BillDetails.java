package com.hms.model;

import java.util.List;

/** A bill together with every charge row it covers, used to print an itemised bill. */
public record BillDetails(Bill bill, Patient patient, List<Admission> admissions, List<Dispense> dispenses,
                          List<LabTest> labTests) {
}
