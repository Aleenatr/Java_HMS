package com.hms.ui;

import com.hms.model.Admission;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.Room;
import com.hms.service.BillCalculator;
import com.hms.service.Services;
import com.hms.util.Times;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import java.time.LocalDateTime;
import java.util.List;

import static com.hms.security.Permission.MANAGE_ADMISSIONS;

/** Admit and discharge. Doctors see the patients admitted under their care. */
public final class AdmissionsPanel extends ModulePanel {

    private final JCheckBox currentOnly = new JCheckBox("Currently admitted only", true);
    private final DataTable<Admission> table = new DataTable<>(
            Column.of("#", Admission::id, 45),
            Column.of("Patient", Admission::patientName, 150),
            Column.of("Room", Admission::roomNumber, 65),
            Column.of("Doctor", Admission::doctorName, 150),
            Column.of("Reason", Admission::reason, 260),
            Column.of("Admitted", Admission::admittedAt, 125),
            Column.of("Discharged", Admission::dischargedAt, 125),
            Column.of("Days", AdmissionsPanel::days, 50),
            Column.of("Bill #", Admission::billId, 60));

    public AdmissionsPanel(Services services) {
        super(services, services.session().isDoctor() ? "My inpatients" : "Admissions",
                services.session().isDoctor() ? "My inpatients" : "Admissions",
                services.session().isDoctor()
                        ? "Patients admitted under your care"
                        : "Admit patients to rooms; discharge creates the final bill");
        currentOnly.setOpaque(false);
        currentOnly.addActionListener(e -> attempt(this::refresh));
        addFilter(currentOnly);
        if (services.session().can(MANAGE_ADMISSIONS)) {
            action("Discharge & bill", this::discharge);
            primaryAction("Admit patient", this::admit);
        }
        setContent(table);
    }

    /** Days charged so far (or in total, once discharged). */
    private static long days(Admission stay) {
        LocalDateTime end = stay.isOpen() ? Times.now() : stay.dischargedAt();
        // Guards against the PC's clock and MySQL's clock being slightly out of step.
        if (end.isBefore(stay.admittedAt())) {
            end = stay.admittedAt();
        }
        return BillCalculator.chargeableDays(stay.admittedAt(), end);
    }

    @Override
    public void refresh() {
        table.setRows(services.admissions().list(currentOnly.isSelected()));
    }

    private void admit() {
        List<Room> rooms = services.rooms().available();
        if (rooms.isEmpty()) {
            Dialogs.error(this, "No rooms are available right now.");
            return;
        }
        FormDialog form = new FormDialog(this, "Admit patient");
        JComboBox<Patient> patient = form.combo("Patient", services.patients().list(null), null);
        JComboBox<Room> room = form.combo("Room", rooms, null);
        JComboBox<Doctor> doctor = form.combo("Attending doctor", services.doctors().list(), null);
        JTextArea reason = form.area("Reason", "", 3);
        form.note("The room's current daily rate is locked in for this stay.");
        boolean saved = form.show("Admit", () -> services.admissions().admit(
                FormDialog.selected(patient, "Patient").id(),
                FormDialog.selected(room, "Room").id(),
                FormDialog.selected(doctor, "Attending doctor").id(),
                reason.getText()));
        if (saved) {
            refresh();
        }
    }

    private void discharge() {
        selection(table, "an admission").ifPresent(stay -> {
            if (!stay.isOpen()) {
                Dialogs.error(this, stay.patientName() + " has already been discharged.");
                return;
            }
            if (!Dialogs.confirm(this, "Discharge " + stay.patientName() + " from room " + stay.roomNumber()
                    + "?\nThe final bill (room, medicines and lab tests) will be created.")) {
                return;
            }
            Bill bill = services.admissions().discharge(stay.id());
            refresh();
            Dialogs.showDocument(this, "Bill #" + bill.id(), services.billing().printableBill(bill.id()),
                    "bill-" + bill.id() + ".txt");
        });
    }
}
