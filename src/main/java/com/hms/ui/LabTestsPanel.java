package com.hms.ui;

import com.hms.model.LabTest;
import com.hms.model.LabTestStatus;
import com.hms.model.LabTestType;
import com.hms.model.Patient;
import com.hms.service.Services;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;

import static com.hms.security.Permission.ORDER_LAB_TESTS;
import static com.hms.security.Permission.RECORD_LAB_RESULTS;

/** Lab tests: doctors order, the lab desk records results, patients read their own. */
public final class LabTestsPanel extends ModulePanel {

    private final JCheckBox pendingOnly = new JCheckBox("Waiting for results only", false);
    private final DataTable<LabTest> table = new DataTable<>(
            Column.of("#", LabTest::id, 45),
            Column.of("Ordered", LabTest::orderedAt, 125),
            Column.of("Patient", LabTest::patientName, 150),
            Column.of("Test", LabTest::testName, 200),
            Column.of("Ordered by", LabTest::doctorName, 150),
            Column.of("Price", LabTest::price, 90),
            Column.of("Status", LabTest::status, 90),
            Column.of("Result", LabTest::result, 260));

    public LabTestsPanel(Services services) {
        super(services, services.session().isPatient() ? "My lab tests" : "Lab tests",
                services.session().isPatient() ? "My lab tests" : "Lab tests",
                services.session().isDoctor() ? "Tests you have ordered and their results"
                        : services.session().isPatient() ? "Your tests and results"
                        : "Every test ordered in the hospital; record results as they come in");
        pendingOnly.setOpaque(false);
        pendingOnly.addActionListener(e -> attempt(this::refresh));
        addFilter(pendingOnly);
        if (services.session().can(RECORD_LAB_RESULTS)) {
            action("Record result", this::recordResult);
        }
        if (services.session().can(ORDER_LAB_TESTS)) {
            primaryAction("Order test", this::order);
        }
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.lab().list(pendingOnly.isSelected()));
    }

    private void order() {
        FormDialog form = new FormDialog(this, "Order lab test");
        JComboBox<Patient> patient = form.combo("Patient", services.patients().list(null), null);
        JComboBox<LabTestType> test = form.combo("Test", services.lab().testTypes(), null);
        form.note("The test is charged on the patient's next bill at today's price.");
        if (form.show("Order", () -> services.lab().order(
                FormDialog.selected(patient, "Patient").id(),
                FormDialog.selected(test, "Test").id()))) {
            refresh();
        }
    }

    private void recordResult() {
        selection(table, "a test").ifPresent(test -> {
            if (test.status() == LabTestStatus.COMPLETED) {
                Dialogs.error(this, "This test already has a result.");
                return;
            }
            FormDialog form = new FormDialog(this, "Result: " + test.testName());
            form.note("Patient: " + test.patientName() + "  ·  ordered by " + test.doctorName());
            JTextArea result = form.area("Result", "", 4);
            if (form.show("Save result", () -> services.lab().recordResult(test.id(), result.getText()))) {
                refresh();
            }
        });
    }
}
