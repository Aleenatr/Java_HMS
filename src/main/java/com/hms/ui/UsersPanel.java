package com.hms.ui;

import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.security.Role;
import com.hms.service.Services;
import com.hms.service.ValidationException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.util.Arrays;
import java.util.List;

/** Login accounts. The administrator creates them, assigns the role and can disable them. */
public final class UsersPanel extends ModulePanel {

    private final DataTable<User> table = new DataTable<>(
            Column.of("#", User::id, 45),
            Column.of("Username", User::username, 130),
            Column.of("Name", User::displayName, 180),
            Column.of("Role", User::role, 110),
            Column.of("Active", User::active, 60),
            Column.of("Created", User::createdAt, 125));

    public UsersPanel(Services services) {
        super(services, "Users", "User accounts", "Who can log in, and with which role");
        action("Enable / disable", this::toggleActive);
        action("Reset password", this::resetPassword);
        primaryAction("New login", this::create);
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.users().list());
    }

    /** The profile list depends on the role: doctors or patients that don't have a login yet. */
    private void create() {
        FormDialog form = new FormDialog(this, "New login");
        JComboBox<Role> role = form.combo("Role", List.of(Role.values()), Role.PATIENT);
        JComboBox<Object> profile = form.add("Person", new JComboBox<>());
        JTextField username = form.text("Username", "");
        JPasswordField password = form.password("Password");
        JPasswordField confirm = form.password("Confirm password");
        form.note("At least 8 characters, with a letter and a digit.");

        Runnable loadProfiles = () -> {
            Role chosen = (Role) role.getSelectedItem();
            List<?> people = switch (chosen == null ? Role.ADMIN : chosen) {
                case ADMIN -> List.of();
                case DOCTOR -> services.doctors().list().stream().filter(d -> d.userId() == null).toList();
                case PATIENT -> services.patients().list(null).stream().filter(p -> p.userId() == null).toList();
            };
            profile.setModel(new DefaultComboBoxModel<>(people.toArray()));
            profile.setEnabled(chosen != Role.ADMIN);
        };
        role.addActionListener(e -> loadProfiles.run());
        loadProfiles.run();

        boolean saved = form.show("Create login", () -> {
            char[] secret = matchingPasswords(password, confirm);
            Role chosen = FormDialog.selected(role, "Role");
            switch (chosen) {
                case ADMIN -> services.users().createAdmin(username.getText(), secret);
                case DOCTOR -> services.users().createDoctorLogin(
                        ((Doctor) FormDialog.selected(profile, "Doctor")).id(), username.getText(), secret);
                case PATIENT -> services.users().createPatientLogin(
                        ((Patient) FormDialog.selected(profile, "Patient")).id(), username.getText(), secret);
            }
        });
        if (saved) {
            refresh();
        }
    }

    private void resetPassword() {
        selection(table, "a user").ifPresent(user -> {
            FormDialog form = new FormDialog(this, "Reset password for " + user.username());
            JPasswordField password = form.password("New password");
            JPasswordField confirm = form.password("Confirm password");
            form.note("Give the new password to the user in person. At least 8 characters, with a letter and a digit.");
            if (form.show("Reset password",
                    () -> services.users().resetPassword(user.id(), matchingPasswords(password, confirm)))) {
                done("Password changed for " + user.username() + ".");
            }
        });
    }

    /** Returns the password if both boxes match. The service wipes the returned array after use. */
    private static char[] matchingPasswords(JPasswordField password, JPasswordField confirm) {
        char[] first = password.getPassword();
        char[] second = confirm.getPassword();
        try {
            if (!Arrays.equals(first, second)) {
                Arrays.fill(first, '\0');
                throw new ValidationException("The passwords don't match.");
            }
            return first;
        } finally {
            Arrays.fill(second, '\0');
        }
    }

    private void toggleActive() {
        selection(table, "a user").ifPresent(user -> {
            String verb = user.active() ? "Disable" : "Enable";
            if (Dialogs.confirm(this, verb + " the login " + user.username() + "?")) {
                services.users().setActive(user.id(), !user.active());
                refresh();
            }
        });
    }
}
