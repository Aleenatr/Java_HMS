package com.hms.ui;

import com.hms.util.Formats;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A table that shows a list of Java objects (for example List&lt;Patient&gt;), one per row.
 * Columns say how to read each value. Click a heading to sort; dates, money and statuses
 * are formatted automatically.
 */
public final class DataTable<T> extends JScrollPane {

    private final List<Column<T>> columns;
    private final Model model = new Model();
    private final JTable table;
    private List<T> rows = new ArrayList<>();

    @SafeVarargs
    public DataTable(Column<T>... columns) {
        List<Column<T>> list = new ArrayList<>();
        for (Column<T> column : columns) {
            list.add(column);
        }
        this.columns = List.copyOf(list);
        // The JTable asks the model for its columns straight away, so it is created after them.
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new Renderer());
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<Model> sorter = new TableRowSorter<>(model);
        for (int i = 0; i < this.columns.size(); i++) {
            sorter.setComparator(i, VALUE_ORDER);
            table.getColumnModel().getColumn(i).setPreferredWidth(this.columns.get(i).width());
        }
        table.setRowSorter(sorter);
        setViewportView(table);
        setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
    }

    public void setRows(List<T> newRows) {
        rows = new ArrayList<>(newRows);
        model.fireTableDataChanged();
    }

    public List<T> rows() {
        return List.copyOf(rows);
    }

    /** The selected row's object. The sorter may have reordered rows, so the index is converted first. */
    public Optional<T> selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return Optional.empty();
        }
        return Optional.of(rows.get(table.convertRowIndexToModel(viewRow)));
    }

    public void selectFirstRow() {
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    public void onDoubleClick(Runnable action) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    action.run();
                }
            }
        });
    }

    public void onSelectionChange(Runnable action) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                action.run();
            }
        });
    }

    public JTable table() {
        return table;
    }

    /** Sorts numbers and dates by value (not as text) and puts empty cells last. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Comparator<Object> VALUE_ORDER = (a, b) -> {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }
        if (a instanceof Comparable ca && a.getClass() == b.getClass()) {
            return ca.compareTo(b);
        }
        return a.toString().compareToIgnoreCase(b.toString());
    };

    private final class Model extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column).header();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return columns.get(column).value().apply(rows.get(row));
        }
    }

    /**
     * How a cell value is shown. Swing treats any text that starts with "&lt;html&gt;" as HTML
     * (it would even load images from the internet), so such text gets a leading space and is
     * shown as typed. This matters because patients type the reason for their appointment.
     */
    static Object displayValue(Object value) {
        if (value instanceof BigDecimal amount) {
            return Formats.money(amount);
        }
        if (value instanceof LocalDate date) {
            return Formats.date(date);
        }
        if (value instanceof LocalDateTime dateTime) {
            return Formats.dateTime(dateTime);
        }
        if (value instanceof LocalTime time) {
            return Formats.time(time);
        }
        if (value instanceof Boolean flag) {
            return flag ? "Yes" : "No";
        }
        if (value instanceof String text && text.regionMatches(true, 0, "<html>", 0, 6)) {
            return " " + text;
        }
        return value;
    }

    /** Formats each value by its type and colours status values. */
    private static final class Renderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            Object shown = displayValue(value);
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, shown, selected, focused,
                    row, column);
            label.setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            Color status = Theme.statusColour(value);
            label.setFont(status != null ? label.getFont().deriveFont(Font.BOLD) : table.getFont());
            if (!selected) {
                label.setForeground(status != null ? status : table.getForeground());
            }
            label.setToolTipText(shown instanceof String text && text.length() > 40 ? text : null);
            return label;
        }
    }
}
