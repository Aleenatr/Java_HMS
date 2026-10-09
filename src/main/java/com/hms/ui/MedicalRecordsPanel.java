package com.hms.ui;

import com.hms.model.LabTest;
import com.hms.model.MedicalRecord;
import com.hms.model.Patient;
import com.hms.service.Services;
import com.hms.service.Validator;
import com.hms.util.Formats;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.hms.security.Permission.WRITE_MEDICAL_RECORDS;

/**
 * A patient's history: visit notes and lab tests. Doctors pick any patient and add notes;
 * patients see only their own history.
 */
public final class MedicalRecordsPanel extends ModulePanel {

    private final JComboBox<Patient> patientPicker = new JComboBox<>();
    private final DataTable<MedicalRecord> visits = new DataTable<>(
            Column.of("Visit date", MedicalRecord::visitDate, 90),
            Column.of("Doctor", MedicalRecord::doctorName, 150),
            Column.of("Diagnosis", MedicalRecord::diagnosis, 260),
            Column.of("Prescription", MedicalRecord::prescription, 300));
    private final DataTable<LabTest> tests = new DataTable<>(
            Column.of("Ordered", LabTest::orderedAt, 125),
            Column.of("Test", LabTest::testName, 200),
            Column.of("Ordered by", LabTest::doctorName, 150),
            Column.of("Status", LabTest::status, 90),
            Column.of("Result", LabTest::result, 320));
    private final JTextArea details = new JTextArea(5, 40);
    private boolean reloadingPatients;

    public MedicalRecordsPanel(Services services) {
        super(services, services.session().isPatient() ? "My records" : "Medical records",
                services.session().isPatient() ? "My medical records" : "Medical records",
                services.session().isPatient()
                        ? "Your visit notes, prescriptions and lab results"
                        : "Choose a patient to see their history; add notes after each visit");
        if (!services.session().isPatient()) {
            patientPicker.setPreferredSize(new Dimension(280, patientPicker.getPreferredSize().height));
            patientPicker.addActionListener(e -> {
                if (!reloadingPatients) {
                    attempt(this::showHistory);
                }
            });
            addFilter(Ui.muted("Patient"));
            addFilter(patientPicker);
        }
        if (services.session().can(WRITE_MEDICAL_RECORDS)) {
            primaryAction("Add visit note", this::addRecord);
        }

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        visits.onSelectionChange(this::showDetails);

        JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JSplitPane visitTab = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visits, detailsScroll);
        visitTab.setResizeWeight(0.7);
        visitTab.setBorder(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Visits", visitTab);
        tabs.addTab("Lab tests", tests);
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(tabs, BorderLayout.CENTER);
        setContent(content);
    }

    @Override
    public void refresh() {
        if (services.session().isPatient()) {
            showHistory();
            return;
        }
        Patient previous = (Patient) patientPicker.getSelectedItem();
        List<Patient> patients = services.patients().list(null);
        reloadingPatients = true;
        try {
            patientPicker.removeAllItems();
            patients.forEach(patientPicker::addItem);
            patients.stream().filter(p -> previous != null && p.id() == previous.id()).findFirst()
                    .ifPresent(patientPicker::setSelectedItem);
        } finally {
            reloadingPatients = false;
        }
        showHistory();
    }

    private Integer currentPatientId() {
        if (services.session().isPatient()) {
            return services.session().patientId();
        }
        Patient chosen = (Patient) patientPicker.getSelectedItem();
        return chosen == null ? null : chosen.id();
    }

    private void showHistory() {
        Integer patientId = currentPatientId();
        if (patientId == null) {
            visits.setRows(List.of());
            tests.setRows(List.of());
            return;
        }
        visits.setRows(services.records().forPatient(patientId));
        tests.setRows(services.lab().forPatient(patientId));
        visits.selectFirstRow();
        showDetails();
    }

    private void showDetails() {
        details.setText(visits.selected().map(r -> "Visit on " + Formats.date(r.visitDate()) + " with "
                + r.doctorName() + "\n\nDiagnosis: " + r.diagnosis()
                + "\nPrescription: " + Objects.requireNonNullElse(r.prescription(), "-")
                + "\nNotes: " + Objects.requireNonNullElse(r.notes(), "-")).orElse(""));
        details.setCaretPosition(0);
    }

    private void addRecord() {
        Integer patientId = currentPatientId();
        if (patientId == null) {
            Dialogs.error(this, "Please choose a patient first.");
            return;
        }
        Patient patient = (Patient) patientPicker.getSelectedItem();
        FormDialog form = new FormDialog(this, "Visit note for " + patient.name());
        JTextField date = form.date("Visit date", LocalDate.now());
        JTextArea diagnosis = form.area("Diagnosis", "", 2);
        JTextArea prescription = form.area("Prescription", "", 3);
        JTextArea notes = form.area("Notes", "", 3);
        form.note("Signed as " + services.session().user().displayName() + ".");
        boolean saved = form.show("Save note", () -> services.records().add(
                patientId,
                Validator.parseDate("Visit date", date.getText()),
                diagnosis.getText(),
                prescription.getText(),
                notes.getText()));
        if (saved) {
            showHistory();
        }
    }
}
