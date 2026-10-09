package com.hms.ui;

import com.hms.model.Gender;
import com.hms.model.Patient;
import com.hms.service.Services;
import com.hms.service.Validator;

import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.util.List;

import static com.hms.security.Permission.MANAGE_PATIENTS;

/** Patient register. Administrators add and edit; doctors can search and view. */
public final class PatientsPanel extends ModulePanel {

    private final JTextField search = Ui.searchField("Search by name or phone", this::refresh);
    private final DataTable<Patient> table = new DataTable<>(
            Column.of("#", Patient::id, 45),
            Column.of("Name", Patient::name, 170),
            Column.of("Gender", Patient::gender, 70),
            Column.of("Age", Patient::age, 50),
            Column.of("Date of birth", Patient::dateOfBirth, 100),
            Column.of("Mobile", Patient::phone, 110),
            Column.of("Email", Patient::email, 200),
            Column.of("Address", Patient::address, 190),
            Column.of("Login", p -> p.userId() != null, 55));

    public PatientsPanel(Services services) {
        super(services, "Patients", "Patients", "Search the patient register"
                + (services.session().can(MANAGE_PATIENTS) ? ", register new patients and update details" : ""));
        addFilter(search);
        addFilter(Ui.button("Search", () -> attempt(this::refresh)));
        if (services.session().can(MANAGE_PATIENTS)) {
            dangerAction("Delete", this::delete);
            action("Edit", this::edit);
            primaryAction("Register patient", this::register);
            table.onDoubleClick(() -> attempt(this::edit));
        }
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.patients().list(search.getText()));
    }

    private void register() {
        if (showForm("Register patient", null)) {
            refresh();
        }
    }

    private void edit() {
        selection(table, "a patient").ifPresent(patient -> {
            if (showForm("Edit patient #" + patient.id(), patient)) {
                refresh();
            }
        });
    }

    private void delete() {
        selection(table, "a patient").ifPresent(patient -> {
            if (Dialogs.confirm(this, "Delete " + patient.name() + "? This cannot be undone.")) {
                services.patients().delete(patient.id());
                refresh();
            }
        });
    }

    private boolean showForm(String title, Patient existing) {
        boolean isNew = existing == null;
        FormDialog form = new FormDialog(this, title);
        JTextField name = form.text("Full name", isNew ? "" : existing.name());
        JComboBox<Gender> gender = form.combo("Gender", List.of(Gender.values()), isNew ? null : existing.gender());
        JTextField birthDate = form.date("Date of birth", isNew ? null : existing.dateOfBirth());
        JTextField phone = form.text("Mobile", isNew ? "" : existing.phone());
        JTextField email = form.text("Email (optional)", isNew ? "" : existing.email());
        JTextArea address = form.area("Address (optional)", isNew ? "" : existing.address(), 2);
        return form.show(isNew ? "Register" : "Save changes", () -> {
            Patient patient = new Patient(
                    isNew ? 0 : existing.id(),
                    name.getText(),
                    (Gender) gender.getSelectedItem(),
                    Validator.parseDate("Date of birth", birthDate.getText()),
                    phone.getText(),
                    email.getText(),
                    address.getText(),
                    isNew ? null : existing.userId());
            if (isNew) {
                services.patients().create(patient);
            } else {
                services.patients().update(patient);
            }
        });
    }
}
