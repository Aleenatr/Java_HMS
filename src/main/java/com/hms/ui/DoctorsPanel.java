package com.hms.ui;

import com.hms.model.Department;
import com.hms.model.Doctor;
import com.hms.service.Services;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.List;

import static com.hms.security.Permission.MANAGE_DOCTORS;

/** Doctor directory. Everyone can look; administrators add, edit and delete. */
public final class DoctorsPanel extends ModulePanel {

    private final DataTable<Doctor> table = new DataTable<>(
            Column.of("#", Doctor::id, 45),
            Column.of("Name", Doctor::name, 170),
            Column.of("Specialization", Doctor::specialization, 160),
            Column.of("Department", Doctor::departmentName, 140),
            Column.of("Mobile", Doctor::phone, 110),
            Column.of("Email", Doctor::email, 230),
            Column.of("Login", d -> d.userId() != null, 55));

    public DoctorsPanel(Services services) {
        super(services, "Doctors", "Doctors", "Doctors and their departments");
        if (services.session().can(MANAGE_DOCTORS)) {
            action("Add department", this::addDepartment);
            dangerAction("Delete", this::delete);
            action("Edit", this::edit);
            primaryAction("Add doctor", this::add);
            table.onDoubleClick(() -> attempt(this::edit));
        }
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.doctors().list());
    }

    private void add() {
        if (showForm("Add doctor", null)) {
            refresh();
        }
    }

    private void edit() {
        selection(table, "a doctor").ifPresent(doctor -> {
            if (showForm("Edit doctor #" + doctor.id(), doctor)) {
                refresh();
            }
        });
    }

    private void delete() {
        selection(table, "a doctor").ifPresent(doctor -> {
            if (Dialogs.confirm(this, "Delete " + doctor.name() + "? This cannot be undone.")) {
                services.doctors().delete(doctor.id());
                refresh();
            }
        });
    }

    private boolean showForm(String title, Doctor existing) {
        boolean isNew = existing == null;
        List<Department> departments = services.doctors().departments();
        Department current = isNew ? null : departments.stream()
                .filter(d -> d.id() == existing.departmentId()).findFirst().orElse(null);

        FormDialog form = new FormDialog(this, title);
        JTextField name = form.text("Full name", isNew ? "Dr. " : existing.name());
        JTextField specialization = form.text("Specialization", isNew ? "" : existing.specialization());
        JComboBox<Department> department = form.combo("Department", departments, current);
        JTextField phone = form.text("Mobile", isNew ? "" : existing.phone());
        JTextField email = form.text("Email (optional)", isNew ? "" : existing.email());
        return form.show(isNew ? "Add doctor" : "Save changes", () -> {
            Department chosen = (Department) department.getSelectedItem();
            Doctor doctor = new Doctor(
                    isNew ? 0 : existing.id(),
                    name.getText(),
                    specialization.getText(),
                    chosen == null ? 0 : chosen.id(),
                    chosen == null ? null : chosen.name(),
                    phone.getText(),
                    email.getText(),
                    isNew ? null : existing.userId());
            if (isNew) {
                services.doctors().create(doctor);
            } else {
                services.doctors().update(doctor);
            }
        });
    }

    private void addDepartment() {
        FormDialog form = new FormDialog(this, "Add department");
        JTextField name = form.text("Department name", "");
        JTextField phone = form.text("Phone / extension", "");
        if (form.show("Add department", () -> services.doctors().addDepartment(name.getText(), phone.getText()))) {
            done("Department added. You can now choose it for doctors.");
        }
    }
}
