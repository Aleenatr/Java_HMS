package com.hms.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.hms.HmsException;
import com.hms.security.Session;
import com.hms.service.Services;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.hms.security.Permission.MANAGE_ADMISSIONS;
import static com.hms.security.Permission.MANAGE_PHARMACY;
import static com.hms.security.Permission.MANAGE_ROOMS;
import static com.hms.security.Permission.MANAGE_USERS;
import static com.hms.security.Permission.VIEW_BILLS;
import static com.hms.security.Permission.VIEW_LAB_TESTS;
import static com.hms.security.Permission.VIEW_MEDICAL_RECORDS;
import static com.hms.security.Permission.VIEW_PATIENTS;

/**
 * The main window's content: header, sidebar and the screen area.
 * The sidebar only lists the screens the user's role may open.
 */
public final class MainView extends JPanel {

    private final Services services;
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final Map<String, ModulePanel> modules = new LinkedHashMap<>();
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();

    public MainView(Services services, Runnable onLogout) {
        super(new BorderLayout());
        this.services = services;
        add(header(onLogout), BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.SIDEBAR);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));
        nav.setPreferredSize(new Dimension(200, 0));
        ButtonGroup group = new ButtonGroup();
        for (ModulePanel module : modulesFor(services)) {
            modules.put(module.navLabel(), module);
            screens.add(module, module.navLabel());
            JToggleButton button = navButton(module.navLabel());
            group.add(button);
            navButtons.put(module.navLabel(), button);
            nav.add(button);
            nav.add(Box.createVerticalStrut(4));
        }
        add(nav, BorderLayout.WEST);
        add(screens, BorderLayout.CENTER);
        open(modules.keySet().iterator().next());
    }

    /** Which screens each role gets. The services check permissions again on every call. */
    static List<ModulePanel> modulesFor(Services services) {
        Session session = services.session();
        List<ModulePanel> list = new ArrayList<>();
        list.add(new DashboardPanel(services));
        if (session.can(VIEW_PATIENTS)) {
            list.add(new PatientsPanel(services));
        }
        list.add(new DoctorsPanel(services));
        list.add(new AppointmentsPanel(services));
        if (session.can(MANAGE_ADMISSIONS) || session.isDoctor()) {
            list.add(new AdmissionsPanel(services));
        }
        if (session.can(MANAGE_ROOMS)) {
            list.add(new RoomsPanel(services));
        }
        if (session.can(VIEW_MEDICAL_RECORDS)) {
            list.add(new MedicalRecordsPanel(services));
        }
        // Patients already see their lab results inside "My records".
        if (session.can(VIEW_LAB_TESTS) && !session.isPatient()) {
            list.add(new LabTestsPanel(services));
        }
        if (session.can(MANAGE_PHARMACY)) {
            list.add(new PharmacyPanel(services));
        }
        if (session.can(VIEW_BILLS)) {
            list.add(new BillingPanel(services));
        }
        if (session.can(MANAGE_USERS)) {
            list.add(new UsersPanel(services));
        }
        return list;
    }

    /** Shows a screen and reloads its data, so it is always up to date. */
    public void open(String navLabel) {
        ModulePanel module = modules.get(navLabel);
        navButtons.get(navLabel).setSelected(true);
        cards.show(screens, navLabel);
        try {
            module.refresh();
        } catch (HmsException e) {
            Dialogs.error(this, e.getMessage());
        }
    }

    public List<String> screenNames() {
        return List.copyOf(modules.keySet());
    }

    private JToggleButton navButton(String label) {
        JToggleButton button = new JToggleButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0; margin: 8,14,8,14; font: +1;"
                        + "background: #F8FAFC; hoverBackground: #E2E8F0; pressedBackground: #CBD5E1;"
                        + "selectedBackground: #CCFBF1; selectedForeground: #115E59");
        button.addActionListener(e -> open(label));
        return button;
    }

    private JPanel header(Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 16));

        JLabel brand = new JLabel("Hospital Management System");
        brand.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        brand.setForeground(Color.WHITE);
        header.add(brand, BorderLayout.WEST);

        Session session = services.session();
        JLabel who = new JLabel(session.user().displayName() + "  ·  " + session.role());
        who.setForeground(new Color(0xCCFBF1));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(who);
        right.add(headerButton("Change password", this::changePassword));
        right.add(headerButton("Log out", onLogout));
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private static JButton headerButton(String text, Runnable onClick) {
        JButton button = Ui.button(text, onClick);
        button.putClientProperty(FlatClientProperties.STYLE,
                "background: #115E59; foreground: #FFFFFF; hoverBackground: #134E4A; borderWidth: 0; focusWidth: 0");
        return button;
    }

    private void changePassword() {
        FormDialog form = new FormDialog(this, "Change password");
        JPasswordField current = form.password("Current password");
        JPasswordField next = form.password("New password");
        JPasswordField confirm = form.password("Confirm new password");
        form.note("At least 8 characters, with a letter and a digit.");
        if (form.show("Change password", () -> services.auth().changePassword(services.session(),
                current.getPassword(), next.getPassword(), confirm.getPassword()))) {
            Dialogs.info(this, "Your password has been changed.");
        }
    }
}
