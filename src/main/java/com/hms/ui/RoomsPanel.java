package com.hms.ui;

import com.hms.model.Room;
import com.hms.model.RoomStatus;
import com.hms.model.RoomType;
import com.hms.service.Services;
import com.hms.service.Validator;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.List;

/** Rooms and their daily rates. Occupancy changes only through admissions and discharges. */
public final class RoomsPanel extends ModulePanel {

    private final DataTable<Room> table = new DataTable<>(
            Column.of("Room", Room::roomNumber, 90),
            Column.of("Type", Room::type, 140),
            Column.of("Daily rate", Room::dailyRate, 120),
            Column.of("Status", Room::status, 150));

    public RoomsPanel(Services services) {
        super(services, "Rooms", "Rooms", "Room types, daily rates and availability");
        action("Maintenance on/off", this::toggleMaintenance);
        action("Edit", this::edit);
        primaryAction("Add room", this::add);
        table.onDoubleClick(() -> attempt(this::edit));
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.rooms().list());
    }

    private void add() {
        if (showForm("Add room", null)) {
            refresh();
        }
    }

    private void edit() {
        selection(table, "a room").ifPresent(room -> {
            if (showForm("Edit room " + room.roomNumber(), room)) {
                refresh();
            }
        });
    }

    private void toggleMaintenance() {
        selection(table, "a room").ifPresent(room -> {
            services.rooms().setMaintenance(room.id(), room.status() != RoomStatus.MAINTENANCE);
            refresh();
        });
    }

    private boolean showForm(String title, Room existing) {
        boolean isNew = existing == null;
        FormDialog form = new FormDialog(this, title);
        JTextField number = form.text("Room number", isNew ? "" : existing.roomNumber());
        JComboBox<RoomType> type = form.combo("Type", List.of(RoomType.values()), isNew ? null : existing.type());
        JTextField rate = form.text("Daily rate (₹)", isNew ? "" : existing.dailyRate().toPlainString());
        form.note("A new rate applies to future admissions; current patients keep the rate they were admitted at.");
        return form.show(isNew ? "Add room" : "Save changes", () -> {
            Room room = new Room(
                    isNew ? 0 : existing.id(),
                    number.getText(),
                    (RoomType) type.getSelectedItem(),
                    Validator.parseAmount("Daily rate", rate.getText()),
                    isNew ? RoomStatus.AVAILABLE : existing.status());
            if (isNew) {
                services.rooms().create(room);
            } else {
                services.rooms().update(room);
            }
        });
    }
}
