package com.hms.ui;

import com.hms.model.Appointment;
import com.hms.service.DashboardService.Stat;
import com.hms.service.Services;
import com.hms.util.Formats;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;

/** Home screen: a few numbers for the logged-in role and the upcoming appointments. */
public final class DashboardPanel extends ModulePanel {

    private static final Color TILE_FILL = new Color(0xF0FDFA);

    private final JPanel tiles = new JPanel(new GridLayout(0, 4, 14, 14));
    private final DataTable<Appointment> upcoming = new DataTable<>(
            Column.of("Date", Appointment::date, 90),
            Column.of("Time", Appointment::slot, 80),
            Column.of("Patient", Appointment::patientName, 170),
            Column.of("Doctor", Appointment::doctorName, 170),
            Column.of("Reason", Appointment::reason, 300));

    public DashboardPanel(Services services) {
        super(services, "Dashboard", "Welcome, " + services.session().user().displayName(),
                "Signed in as " + services.session().role() + "  ·  " + Formats.date(LocalDate.now()));
        tiles.setOpaque(false);
        JPanel list = new JPanel(new BorderLayout(0, 8));
        list.setOpaque(false);
        list.add(Ui.heading("Upcoming appointments", "h4"), BorderLayout.NORTH);
        list.add(upcoming, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setOpaque(false);
        body.add(tiles, BorderLayout.NORTH);
        body.add(list, BorderLayout.CENTER);
        setContent(body);
    }

    @Override
    public void refresh() {
        tiles.removeAll();
        for (Stat stat : services.dashboard().stats()) {
            tiles.add(tile(stat));
        }
        tiles.revalidate();
        tiles.repaint();
        upcoming.setRows(services.dashboard().upcomingAppointments());
    }

    private static JComponent tile(Stat stat) {
        JPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel value = Ui.heading(stat.value(), "h3");
        value.setForeground(Theme.ACCENT_DARK);
        card.add(Ui.muted(stat.label()), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    /** A panel with a filled, rounded background. */
    private static final class RoundedPanel extends JPanel {
        RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TILE_FILL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(Theme.ACCENT_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
