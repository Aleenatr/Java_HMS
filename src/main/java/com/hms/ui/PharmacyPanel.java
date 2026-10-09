package com.hms.ui;

import com.hms.model.Dispense;
import com.hms.model.Medicine;
import com.hms.model.Patient;
import com.hms.service.Services;
import com.hms.util.Formats;
import com.hms.service.Validator;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/** Medicine inventory and dispensing. Dispensed medicines go on the patient's next bill. */
public final class PharmacyPanel extends ModulePanel {

    private final JTextField search = Ui.searchField("Search by name or category", this::refresh);
    private final DataTable<Medicine> inventory = new DataTable<>(
            Column.of("#", Medicine::id, 45),
            Column.of("Medicine", Medicine::name, 190),
            Column.of("Category", Medicine::category, 110),
            Column.of("Manufacturer", Medicine::manufacturer, 130),
            Column.of("Unit price", Medicine::unitPrice, 90),
            Column.of("In stock", Medicine::stock, 75),
            Column.of("Reorder at", Medicine::reorderLevel, 80),
            Column.of("Stock level", Medicine::stockLevel, 110));
    private final DataTable<Dispense> dispensed = new DataTable<>(
            Column.of("When", Dispense::dispensedAt, 125),
            Column.of("Patient", Dispense::patientName, 160),
            Column.of("Medicine", Dispense::medicineName, 190),
            Column.of("Qty", Dispense::quantity, 50),
            Column.of("Unit price", Dispense::unitPrice, 90),
            Column.of("Amount", Dispense::amount, 100),
            Column.of("Bill #", Dispense::billId, 60));

    public PharmacyPanel(Services services) {
        super(services, "Pharmacy", "Pharmacy", "Stock, reorder alerts and dispensing to patients");
        addFilter(search);
        addFilter(Ui.button("Search", () -> attempt(this::refresh)));
        dangerAction("Delete", this::delete);
        action("Edit", this::edit);
        action("Restock", this::restock);
        action("Add medicine", this::add);
        primaryAction("Dispense", this::dispense);
        inventory.onDoubleClick(() -> attempt(this::edit));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Inventory", inventory);
        tabs.addTab("Dispensed", dispensed);
        setContent(tabs);
    }

    @Override
    public void refresh() {
        inventory.setRows(services.pharmacy().list(search.getText()));
        dispensed.setRows(services.pharmacy().recentDispenses());
    }

    private void add() {
        FormDialog form = new FormDialog(this, "Add medicine");
        JTextField name = form.text("Name", "");
        JTextField category = form.text("Category", "");
        JTextField manufacturer = form.text("Manufacturer (optional)", "");
        JTextField price = form.text("Unit price (₹)", "");
        JTextField stock = form.text("Opening stock", "0");
        JTextField reorder = form.text("Reorder level", "20");
        if (form.show("Add medicine", () -> services.pharmacy().add(new Medicine(0,
                name.getText(), category.getText(), manufacturer.getText(),
                Validator.parseAmount("Unit price", price.getText()),
                Validator.parseCount("Opening stock", stock.getText(), 0, 100_000),
                Validator.parseCount("Reorder level", reorder.getText(), 0, 100_000))))) {
            refresh();
        }
    }

    private void edit() {
        selection(inventory, "a medicine").ifPresent(m -> {
            FormDialog form = new FormDialog(this, "Edit " + m.name());
            JTextField name = form.text("Name", m.name());
            JTextField category = form.text("Category", m.category());
            JTextField manufacturer = form.text("Manufacturer (optional)", m.manufacturer());
            JTextField price = form.text("Unit price (₹)", m.unitPrice().toPlainString());
            JTextField reorder = form.text("Reorder level", String.valueOf(m.reorderLevel()));
            form.note("Stock changes only through Restock and Dispense, so every unit is accounted for.");
            if (form.show("Save changes", () -> services.pharmacy().update(new Medicine(m.id(),
                    name.getText(), category.getText(), manufacturer.getText(),
                    Validator.parseAmount("Unit price", price.getText()),
                    m.stock(),
                    Validator.parseCount("Reorder level", reorder.getText(), 0, 100_000))))) {
                refresh();
            }
        });
    }

    private void restock() {
        selection(inventory, "a medicine").ifPresent(m -> {
            FormDialog form = new FormDialog(this, "Restock " + m.name());
            form.note("Currently " + m.stock() + " in stock.");
            JTextField quantity = form.text("Quantity received", "");
            if (form.show("Add to stock", () -> services.pharmacy().restock(m.id(),
                    Validator.parseCount("Quantity", quantity.getText(), 1, 100_000)))) {
                refresh();
            }
        });
    }

    private void dispense() {
        selection(inventory, "a medicine").ifPresent(m -> {
            FormDialog form = new FormDialog(this, "Dispense " + m.name());
            form.note(m.stock() + " in stock at " + Formats.money(m.unitPrice()) + " each.");
            JComboBox<Patient> patient = form.combo("Patient", services.patients().list(null), null);
            JTextField quantity = form.text("Quantity", "");
            if (form.show("Dispense", () -> services.pharmacy().dispense(
                    FormDialog.selected(patient, "Patient").id(), m.id(),
                    Validator.parseCount("Quantity", quantity.getText(), 1, 100_000)))) {
                refresh();
            }
        });
    }

    private void delete() {
        selection(inventory, "a medicine").ifPresent(m -> {
            if (Dialogs.confirm(this, "Delete " + m.name() + " from the inventory?")) {
                services.pharmacy().delete(m.id());
                refresh();
            }
        });
    }
}
