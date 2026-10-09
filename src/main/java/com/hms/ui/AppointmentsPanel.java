package com.hms.ui;

import com.hms.HmsException;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.service.Services;
import com.hms.service.Validator;
import com.hms.util.Formats;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.hms.security.Permission.BOOK_APPOINTMENTS;
import static com.hms.security.Permission.COMPLETE_APPOINTMENTS;

/**
 * Appointments. Admins book for any patient, patients book for themselves,
 * doctors see their own list and mark visits as completed.
 */
public final class AppointmentsPanel extends ModulePanel {

    private final JCheckBox upcomingOnly = new JCheckBox("Upcoming only", true);
    private final DataTable<Appointment> table = new DataTable<>(
            Column.of("#", Appointment::id, 45),
            Column.of("Date", Appointment::date, 90),
            Column.of("Time", Appointment::slot, 80),
            Column.of("Patient", Appointment::patientName, 160),
            Column.of("Doctor", Appointment::doctorName, 160),
            Column.of("Reason", Appointment::reason, 280),
            Column.of("Status", Appointment::status, 100));

    public AppointmentsPanel(Services services) {
        super(services, services.session().isAdmin() ? "Appointments" : "My appointments",
                services.session().isAdmin() ? "Appointments" : "My appointments",
                services.session().isDoctor()
                        ? "Your consultations; mark each visit completed after seeing the patient"
                        : "Book a free slot with a doctor; cancel if plans change");
        upcomingOnly.setOpaque(false);
        upcomingOnly.addActionListener(e -> attempt(this::refresh));
        addFilter(upcomingOnly);
        if (services.session().can(COMPLETE_APPOINTMENTS)) {
            action("Mark completed", this::complete);
        }
        if (services.session().can(BOOK_APPOINTMENTS)) {
            dangerAction("Cancel appointment", this::cancel);
            primaryAction("Book appointment", this::book);
        }
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.appointments().list(upcomingOnly.isSelected()));
    }

    private void book() {
        FormDialog form = new FormDialog(this, "Book appointment");
        JComboBox<Patient> patient = form.combo("Patient", patientsToBookFor(), null);
        patient.setEnabled(!services.session().isPatient());
        JComboBox<Doctor> doctor = form.combo("Doctor", services.doctors().list(), null);

        JTextField date = form.date("Date", LocalDate.now().plusDays(1));
        JComboBox<LocalTime> slot = form.add("Time", new JComboBox<>());
        slot.setRenderer(new TimeRenderer());
        JLabel slotInfo = new JLabel(" ");
        slotInfo.setForeground(Theme.MUTED_TEXT);
        form.add("", slotInfo);
        JTextArea reason = form.area("Reason for visit", "", 3);

        // The time list always matches the chosen doctor and the date as it is typed.
        Runnable loadSlots = () -> loadFreeSlots(doctor, date, slot, slotInfo);
        doctor.addActionListener(e -> loadSlots.run());
        Ui.onTextChange(date, loadSlots);
        loadSlots.run();

        boolean saved = form.show("Book", () -> {
            try {
                services.appointments().book(
                        FormDialog.selected(patient, "Patient").id(),
                        FormDialog.selected(doctor, "Doctor").id(),
                        Validator.parseDate("Date", date.getText()),
                        FormDialog.selected(slot, "Time slot"),
                        reason.getText());
            } catch (HmsException e) {
                loadSlots.run(); // someone else may have just taken the slot
                throw e;
            }
        });
        if (saved) {
            refresh();
        }
    }

    /** Fills the time list with the doctor's free slots for the typed date, keeping the chosen time if still free. */
    private void loadFreeSlots(JComboBox<Doctor> doctor, JTextField date, JComboBox<LocalTime> slot, JLabel info) {
        Object previous = slot.getSelectedItem();
        slot.setModel(new DefaultComboBoxModel<>());
        try {
            Doctor chosen = FormDialog.selected(doctor, "Doctor");
            LocalDate day = Validator.parseDate("Date", date.getText());
            List<LocalTime> free = services.appointments().freeSlots(chosen.id(), day);
            slot.setModel(new DefaultComboBoxModel<>(free.toArray(new LocalTime[0])));
            if (previous != null && free.contains(previous)) {
                slot.setSelectedItem(previous);
            }
            info.setText(free.isEmpty() ? "No free times that day. Try another date." : free.size() + " free times");
        } catch (HmsException e) {
            info.setText(e.getMessage());
        }
    }

    private List<Patient> patientsToBookFor() {
        if (services.session().isPatient()) {
            return List.of(services.patients().get(services.session().patientId()));
        }
        return services.patients().list(null);
    }

    private void cancel() {
        selection(table, "an appointment").ifPresent(appointment -> {
            if (Dialogs.confirm(this, "Cancel the appointment with " + appointment.doctorName() + " on "
                    + Formats.date(appointment.date()) + " at " + Formats.time(appointment.slot()) + "?")) {
                services.appointments().cancel(appointment.id());
                refresh();
            }
        });
    }

    private void complete() {
        selection(table, "an appointment").ifPresent(appointment -> {
            services.appointments().complete(appointment.id());
            refresh();
        });
    }

    private static final class TimeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                                                      boolean focused) {
            Object text = value instanceof LocalTime time ? Formats.time(time) : value;
            return super.getListCellRendererComponent(list, text, index, selected, focused);
        }
    }
}
