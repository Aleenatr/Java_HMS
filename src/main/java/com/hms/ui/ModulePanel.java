package com.hms.ui;

import com.hms.HmsException;
import com.hms.service.Services;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Optional;

/**
 * Base class for every screen in the sidebar. It draws the title, the action buttons and
 * the filter row; subclasses add their content and implement refresh().
 *
 * <p>The main window keeps a list of ModulePanels and calls refresh() on whichever one is
 * opened, without knowing which screen it is (polymorphism).
 */
public abstract class ModulePanel extends JPanel {

    protected final Services services;
    private final String navLabel;
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    protected ModulePanel(Services services, String navLabel, String title, String subtitle) {
        super(new BorderLayout(0, 16));
        this.services = services;
        this.navLabel = navLabel;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel titles = new JPanel(new GridLayout(0, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(Ui.heading(title, "h2"));
        JLabel sub = Ui.muted(subtitle);
        titles.add(sub);

        actions.setOpaque(false);
        filters.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout(16, 12));
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        header.add(filters, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
    }

    /** Reloads the screen's data from the database. */
    public abstract void refresh();

    public String navLabel() {
        return navLabel;
    }

    protected void setContent(JComponent content) {
        add(content, BorderLayout.CENTER);
    }

    protected JButton addAction(JButton button) {
        actions.add(button);
        return button;
    }

    protected JButton action(String text, Runnable onClick) {
        return addAction(Ui.button(text, () -> attempt(onClick)));
    }

    protected JButton primaryAction(String text, Runnable onClick) {
        return addAction(Ui.primaryButton(text, () -> attempt(onClick)));
    }

    protected JButton dangerAction(String text, Runnable onClick) {
        return addAction(Ui.dangerButton(text, () -> attempt(onClick)));
    }

    protected void addFilter(JComponent component) {
        filters.add(component);
    }

    /** Runs a user action; an expected error (HmsException) is shown as a message instead of crashing. */
    protected void attempt(Runnable work) {
        try {
            work.run();
        } catch (HmsException e) {
            Dialogs.error(this, e.getMessage());
        }
    }

    /** The selected row, or a "please select" message when nothing is selected. */
    protected <T> Optional<T> selection(DataTable<T> table, String what) {
        Optional<T> selected = table.selected();
        if (selected.isEmpty()) {
            Dialogs.error(this, "Please select " + what + " in the table first.");
        }
        return selected;
    }

    protected void done(String message) {
        Dialogs.info(this, message);
    }
}
