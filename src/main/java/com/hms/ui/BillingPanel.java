package com.hms.ui;

import com.hms.model.Bill;
import com.hms.model.BillStatus;
import com.hms.model.Patient;
import com.hms.service.Services;
import com.hms.util.Formats;

import javax.swing.JComboBox;

import static com.hms.security.Permission.MANAGE_BILLING;

/** Bills built from three modules: room stays, pharmacy and lab tests. */
public final class BillingPanel extends ModulePanel {

    private final DataTable<Bill> table = new DataTable<>(
            Column.of("Bill #", Bill::id, 55),
            Column.of("Date", Bill::createdAt, 125),
            Column.of("Patient", Bill::patientName, 160),
            Column.of("Room", Bill::roomCharges, 100),
            Column.of("Medicines", Bill::medicineCharges, 100),
            Column.of("Lab", Bill::labCharges, 100),
            Column.of("Total", Bill::total, 110),
            Column.of("Status", Bill::status, 80),
            Column.of("Paid on", Bill::paidAt, 125));

    public BillingPanel(Services services) {
        super(services, services.session().isPatient() ? "My bills" : "Billing",
                services.session().isPatient() ? "My bills" : "Billing",
                "Each bill adds up room stays, medicines and lab tests");
        action("View / print", this::view);
        if (services.session().can(MANAGE_BILLING)) {
            action("Mark as paid", this::markPaid);
            primaryAction("Generate bill", this::generate);
        }
        table.onDoubleClick(() -> attempt(this::view));
        setContent(table);
    }

    @Override
    public void refresh() {
        table.setRows(services.billing().list());
    }

    private void generate() {
        FormDialog form = new FormDialog(this, "Generate bill");
        JComboBox<Patient> patient = form.combo("Patient", services.patients().list(null), null);
        form.note("Collects every charge not billed yet: finished room stays, medicines and lab tests. "
                + "Admitted patients are billed automatically when discharged.");
        Bill[] created = new Bill[1];
        if (form.show("Generate", () -> created[0] = services.billing().generate(
                FormDialog.selected(patient, "Patient").id()))) {
            refresh();
            show(created[0]);
        }
    }

    private void markPaid() {
        selection(table, "a bill").ifPresent(bill -> {
            if (bill.status() == BillStatus.PAID) {
                Dialogs.error(this, "Bill #" + bill.id() + " is already paid.");
                return;
            }
            if (Dialogs.confirm(this, "Record full payment of " + Formats.money(bill.total())
                    + " for bill #" + bill.id() + "?")) {
                services.billing().markPaid(bill.id());
                refresh();
            }
        });
    }

    private void view() {
        selection(table, "a bill").ifPresent(this::show);
    }

    private void show(Bill bill) {
        Dialogs.showDocument(this, "Bill #" + bill.id(), services.billing().printableBill(bill.id()),
                "bill-" + bill.id() + ".txt");
    }
}
