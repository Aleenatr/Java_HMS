package hospital.management.system;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Reception extends JFrame {

    public Reception() {
        setTitle("Hospital Management System - Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));

        // Top Header Bar
        JPanel topBar = new JPanel();
        topBar.setLayout(null);
        topBar.setBounds(0, 0, 1920, 80);
        topBar.setBackground(new Color(72, 160, 160));
        add(topBar);

        // Hospital Logo/Title on the left
        JLabel hospitalName = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        hospitalName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        hospitalName.setForeground(Color.WHITE);
        hospitalName.setBounds(50, 20, 700, 40);
        topBar.add(hospitalName);

        // Doctor image on the right
        try {
            ImageIcon doctorIcon = new ImageIcon(ClassLoader.getSystemResource("icon/dr.jpeg"));
            Image doctorImg = doctorIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel doctorLabel = new JLabel(new ImageIcon(doctorImg));
            doctorLabel.setBounds(1750, -20, 150, 120);
            topBar.add(doctorLabel);
        } catch (Exception e) {
            System.out.println("Doctor image not found");
        }

        // Main Content Panel with background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon bg = new ImageIcon(ClassLoader.getSystemResource("icon/bg.jpeg"));
                    Image bgImg = bg.getImage();
                    g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
                    // Add semi-transparent overlay
                    g.setColor(new Color(255, 255, 255, 180));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } catch (Exception e) {
                    setBackground(new Color(240, 248, 255));
                }
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 80, 1920, 1000);
        add(mainPanel);

        // PATIENT MANAGEMENT SECTION
        JLabel patientSection = new JLabel("PATIENT MANAGEMENT");
        patientSection.setBounds(50, 30, 400, 35);
        patientSection.setFont(new Font("Segoe UI", Font.BOLD, 20));
        patientSection.setForeground(new Color(0, 51, 102));
        mainPanel.add(patientSection);
        
        JButton btn1 = new JButton("📝 Add New Patient");
        btn1.setBounds(50, 75, 245, 45);
        btn1.setBackground(new Color(0, 123, 123));
        btn1.setForeground(Color.WHITE);
        btn1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn1.setFocusPainted(false);
        btn1.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        mainPanel.add(btn1);
        btn1.addActionListener(e -> new NEW_PATIENT());

        JButton btn5 = new JButton("� Patient Info");
        btn5.setBounds(315, 75, 245, 45);
        btn5.setBackground(new Color(0, 123, 123));
        btn5.setForeground(Color.WHITE);
        btn5.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn5.setFocusPainted(false);
        btn5.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        mainPanel.add(btn5);
        btn5.addActionListener(e -> new All_Patient_info());
        
        JButton btn7 = new JButton("📝 Update Patient Details");
        btn7.setBounds(580, 75, 245, 45);
        btn7.setBackground(new Color(0, 123, 123));
        btn7.setForeground(Color.WHITE);
        btn7.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn7.setFocusPainted(false);
        btn7.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        mainPanel.add(btn7);
        btn7.addActionListener(e -> new update_patient_details());

        JButton btn6 = new JButton("🚪 Patient Discharge");
        btn6.setBounds(845, 75, 245, 45);
        btn6.setBackground(new Color(220, 53, 69));
        btn6.setForeground(Color.WHITE);
        btn6.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn6.setFocusPainted(false);
        btn6.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
        mainPanel.add(btn6);
        btn6.addActionListener(e -> new patient_discharge());
        
        // APPOINTMENTS & SERVICES
        JLabel servicesSection = new JLabel("APPOINTMENTS & SERVICES");
        servicesSection.setBounds(50, 145, 450, 35);
        servicesSection.setFont(new Font("Segoe UI", Font.BOLD, 20));
        servicesSection.setForeground(new Color(0, 51, 102));
        mainPanel.add(servicesSection);
        
        JButton btnAppointment = new JButton("📅 Book Appointment");
        btnAppointment.setBounds(50, 190, 245, 45);
        btnAppointment.setBackground(new Color(40, 167, 69));
        btnAppointment.setForeground(Color.WHITE);
        btnAppointment.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAppointment.setFocusPainted(false);
        btnAppointment.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 2));
        mainPanel.add(btnAppointment);
        btnAppointment.addActionListener(e -> new AppointmentBooking());
        
        JButton btnViewAppointments = new JButton("�️ View Appointments");
        btnViewAppointments.setBounds(315, 190, 245, 45);
        btnViewAppointments.setBackground(new Color(40, 167, 69));
        btnViewAppointments.setForeground(Color.WHITE);
        btnViewAppointments.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnViewAppointments.setFocusPainted(false);
        btnViewAppointments.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 2));
        mainPanel.add(btnViewAppointments);
        btnViewAppointments.addActionListener(e -> new ViewAppointments());
        
        JButton btnBilling = new JButton("� Patient Billing");
        btnBilling.setBounds(580, 190, 245, 45);
        btnBilling.setBackground(new Color(40, 167, 69));
        btnBilling.setForeground(Color.WHITE);
        btnBilling.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBilling.setFocusPainted(false);
        btnBilling.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 2));
        mainPanel.add(btnBilling);
        btnBilling.addActionListener(e -> new Billing());

        // HOSPITAL FACILITIES
        JLabel facilitiesSection = new JLabel("HOSPITAL FACILITIES");
        facilitiesSection.setBounds(50, 260, 400, 35);
        facilitiesSection.setFont(new Font("Segoe UI", Font.BOLD, 20));
        facilitiesSection.setForeground(new Color(0, 51, 102));
        mainPanel.add(facilitiesSection);
        
        JButton btn2 = new JButton("🛏️ Room Management");
        btn2.setBounds(50, 305, 245, 45);
        btn2.setBackground(new Color(108, 117, 125));
        btn2.setForeground(Color.WHITE);
        btn2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn2.setFocusPainted(false);
        btn2.setBorder(BorderFactory.createLineBorder(new Color(108, 117, 125), 2));
        mainPanel.add(btn2);
        btn2.addActionListener(e -> new Room());

        JButton btn9 = new JButton("🔍 Search Room");
        btn9.setBounds(315, 305, 245, 45);
        btn9.setBackground(new Color(108, 117, 125));
        btn9.setForeground(Color.WHITE);
        btn9.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn9.setFocusPainted(false);
        btn9.setBorder(BorderFactory.createLineBorder(new Color(108, 117, 125), 2));
        mainPanel.add(btn9);
        btn9.addActionListener(e -> new SearchRoom());
        
        JButton btnPharmacy = new JButton("💊 Pharmacy");
        btnPharmacy.setBounds(580, 305, 245, 45);
        btnPharmacy.setBackground(new Color(108, 117, 125));
        btnPharmacy.setForeground(Color.WHITE);
        btnPharmacy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPharmacy.setFocusPainted(false);
        btnPharmacy.setBorder(BorderFactory.createLineBorder(new Color(108, 117, 125), 2));
        mainPanel.add(btnPharmacy);
        btnPharmacy.addActionListener(e -> new Pharmacy());
        
        JButton btnLabTests = new JButton("🔬 Lab Tests");
        btnLabTests.setBounds(845, 305, 245, 45);
        btnLabTests.setBackground(new Color(108, 117, 125));
        btnLabTests.setForeground(Color.WHITE);
        btnLabTests.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLabTests.setFocusPainted(false);
        btnLabTests.setBorder(BorderFactory.createLineBorder(new Color(108, 117, 125), 2));
        mainPanel.add(btnLabTests);
        btnLabTests.addActionListener(e -> new LabTests());

        JButton btn8 = new JButton("🚑 Hospital Ambulance");
        btn8.setBounds(50, 365, 245, 45);
        btn8.setBackground(new Color(108, 117, 125));
        btn8.setForeground(Color.WHITE);
        btn8.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn8.setFocusPainted(false);
        btn8.setBorder(BorderFactory.createLineBorder(new Color(108, 117, 125), 2));
        mainPanel.add(btn8);
        btn8.addActionListener(e -> new Ambulance());
        
        JButton btnMedicalRecords = new JButton("� Medical Records");
        btnMedicalRecords.setBounds(315, 365, 245, 45);
        btnMedicalRecords.setBackground(new Color(108, 117, 125));
        btnMedicalRecords.setForeground(Color.WHITE);
        btnMedicalRecords.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnMedicalRecords.setFocusPainted(false);
        btnMedicalRecords.setBorder(BorderFactory.createLineBorder(new Color(108, 117, 125), 2));
        mainPanel.add(btnMedicalRecords);
        btnMedicalRecords.addActionListener(e -> new MedicalRecords());

        // ADMINISTRATION
        JLabel adminSection = new JLabel("ADMINISTRATION");
        adminSection.setBounds(50, 435, 350, 35);
        adminSection.setFont(new Font("Segoe UI", Font.BOLD, 20));
        adminSection.setForeground(new Color(0, 51, 102));
        mainPanel.add(adminSection);
        
        JButton btn4 = new JButton("👨‍💼 Employee Info");
        btn4.setBounds(50, 480, 245, 45);
        btn4.setBackground(new Color(255, 193, 7));
        btn4.setForeground(Color.WHITE);
        btn4.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn4.setFocusPainted(false);
        btn4.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));
        mainPanel.add(btn4);
        btn4.addActionListener(e -> new Employee_info());

        JButton btn3 = new JButton("🏥 Department");
        btn3.setBounds(315, 480, 245, 45);
        btn3.setBackground(new Color(255, 193, 7));
        btn3.setForeground(Color.WHITE);
        btn3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn3.setFocusPainted(false);
        btn3.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));
        mainPanel.add(btn3);
        btn3.addActionListener(e -> new Department());
        
        JButton btn10 = new JButton("� Logout");
        btn10.setBounds(580, 480, 245, 45);
        btn10.setBackground(new Color(220, 53, 69));
        btn10.setForeground(Color.WHITE);
        btn10.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn10.setFocusPainted(false);
        btn10.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
        mainPanel.add(btn10);
        btn10.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", 
                "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new Login();
            }
        });

        // Bottom status bar with "Brave" branding
        JPanel statusBar = new JPanel();
        statusBar.setLayout(null);
        statusBar.setBounds(0, 950, 1920, 50);
        statusBar.setBackground(new Color(52, 58, 64));
        mainPanel.add(statusBar);
        
        JLabel braveLabel = new JLabel("Brave");
        braveLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        braveLabel.setForeground(Color.WHITE);
        braveLabel.setBounds(880, 12, 100, 25);
        statusBar.add(braveLabel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Reception());
    }
}
