package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppointmentBooking extends JFrame implements ActionListener {
    
    JTextField patientNameField, phoneField, emailField, ageField, dateField;
    JComboBox<String> departmentBox, doctorBox, timeSlotBox;
    JRadioButton maleRadio, femaleRadio;
    JTextArea symptomsArea;
    JButton bookButton, cancelButton;
    
    public AppointmentBooking() {
        setTitle("Appointment Booking System");
        setSize(900, 780);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 123));
        headerPanel.setBounds(0, 0, 900, 80);
        headerPanel.setLayout(null);
        add(headerPanel);
        
        JLabel headerLabel = new JLabel("📅 Book Appointment");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(headerLabel);
        
        // Main Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBounds(50, 120, 800, 580);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        add(formPanel);
        
        int yPos = 20;
        int labelX = 30, fieldX = 250, labelWidth = 200, fieldWidth = 220;
        
        // Patient Name
        JLabel nameLabel = new JLabel("Patient Name *");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(nameLabel);
        
        patientNameField = new JTextField();
        patientNameField.setBounds(fieldX, yPos, fieldWidth, 30);
        patientNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(patientNameField);
        
        // Phone
        yPos += 50;
        JLabel phoneLabel = new JLabel("Phone Number *");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(phoneLabel);
        
        phoneField = new JTextField();
        phoneField.setBounds(fieldX, yPos, fieldWidth, 30);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(phoneField);
        
        // Email
        yPos += 50;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(emailLabel);
        
        emailField = new JTextField();
        emailField.setBounds(fieldX, yPos, fieldWidth, 30);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(emailField);
        
        // Age
        yPos += 50;
        JLabel ageLabel = new JLabel("Age *");
        ageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ageLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(ageLabel);
        
        ageField = new JTextField();
        ageField.setBounds(fieldX, yPos, 100, 30);
        ageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(ageField);
        
        // Gender
        JLabel genderLabel = new JLabel("Gender *");
        genderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        genderLabel.setBounds(400, yPos, 100, 25);
        formPanel.add(genderLabel);
        
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        
        maleRadio.setBounds(500, yPos, 80, 30);
        femaleRadio.setBounds(590, yPos, 100, 30);
        maleRadio.setBackground(Color.WHITE);
        femaleRadio.setBackground(Color.WHITE);
        maleRadio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        femaleRadio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(maleRadio);
        formPanel.add(femaleRadio);
        
        // Department
        yPos += 50;
        JLabel deptLabel = new JLabel("Department *");
        deptLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deptLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(deptLabel);
        
        departmentBox = new JComboBox<>(new String[]{
            "Select Department",
            "Cardiology", "Neurology", "Orthopedics", 
            "Pediatrics", "Surgery", "Emergency",
            "Radiology", "Laboratory", "Dermatology",
            "ENT", "Ophthalmology", "Dental"
        });
        departmentBox.setBounds(fieldX, yPos, fieldWidth, 30);
        departmentBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        departmentBox.addActionListener(this);
        formPanel.add(departmentBox);
        
        // Doctor
        yPos += 50;
        JLabel doctorLabel = new JLabel("Select Doctor *");
        doctorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        doctorLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(doctorLabel);
        
        doctorBox = new JComboBox<>();
        doctorBox.setBounds(fieldX, yPos, fieldWidth, 30);
        doctorBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(doctorBox);
        
        // Appointment Date
        yPos += 50;
        JLabel dateLabel = new JLabel("Appointment Date * (YYYY-MM-DD)");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(dateLabel);
        
        dateField = new JTextField();
        dateField.setBounds(fieldX, yPos, fieldWidth, 30);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateField.setToolTipText("Format: YYYY-MM-DD (e.g., 2025-11-15)");
        formPanel.add(dateField);
        
        // Time Slot
        yPos += 50;
        JLabel timeLabel = new JLabel("Time Slot *");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timeLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(timeLabel);
        
        timeSlotBox = new JComboBox<>(new String[]{
            "Select Time", "09:00 AM", "10:00 AM", "11:00 AM", 
            "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
        });
        timeSlotBox.setBounds(fieldX, yPos, fieldWidth, 30);
        timeSlotBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(timeSlotBox);
        
        // Symptoms
        yPos += 50;
        JLabel symptomsLabel = new JLabel("Symptoms / Reason *");
        symptomsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        symptomsLabel.setBounds(labelX, yPos, labelWidth, 25);
        formPanel.add(symptomsLabel);
        
        symptomsArea = new JTextArea();
        symptomsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        symptomsArea.setLineWrap(true);
        symptomsArea.setWrapStyleWord(true);
        JScrollPane symptomsScroll = new JScrollPane(symptomsArea);
        symptomsScroll.setBounds(fieldX, yPos, 480, 70);
        formPanel.add(symptomsScroll);
        
        // Buttons
        yPos += 90;
        bookButton = new JButton("✅ BOOK APPOINTMENT");
        bookButton.setBounds(200, yPos, 220, 45);
        bookButton.setBackground(new Color(40, 167, 69));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookButton.setFocusPainted(false);
        bookButton.addActionListener(this);
        formPanel.add(bookButton);
        
        cancelButton = new JButton("❌ CANCEL");
        cancelButton.setBounds(440, yPos, 150, 45);
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(this);
        formPanel.add(cancelButton);
        
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == departmentBox) {
            loadDoctors();
        } else if (e.getSource() == bookButton) {
            bookAppointment();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }
    
    private void loadDoctors() {
        String dept = (String) departmentBox.getSelectedItem();
        doctorBox.removeAllItems();
        
        if (dept.equals("Select Department")) {
            return;
        }
        
        doctorBox.addItem("Dr. Amit Sharma");
        doctorBox.addItem("Dr. Priya Patel");
        doctorBox.addItem("Dr. Rajesh Kumar");
        doctorBox.addItem("Dr. Sneha Joshi");
    }
    
    private void bookAppointment() {
        String name = patientNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String age = ageField.getText().trim();
        String gender = maleRadio.isSelected() ? "Male" : femaleRadio.isSelected() ? "Female" : "";
        String dept = (String) departmentBox.getSelectedItem();
        String doctor = (String) doctorBox.getSelectedItem();
        String appointmentDate = dateField.getText().trim();
        String timeSlot = (String) timeSlotBox.getSelectedItem();
        String symptoms = symptomsArea.getText().trim();
        
        // Validation
        if (name.isEmpty() || phone.isEmpty() || age.isEmpty() || gender.isEmpty() ||
            dept.equals("Select Department") || doctor == null || appointmentDate.isEmpty() ||
            timeSlot.equals("Select Time") || symptoms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate date format
        if (!appointmentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Please enter date in YYYY-MM-DD format!",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            conn c = new conn();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String bookingDate = sdf.format(new Date());
            
            String query = "INSERT INTO Appointments (patient_name, phone, email, age, gender, " +
                          "department, doctor, appointment_date, time_slot, symptoms, booking_date, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Confirmed')";
            
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, phone);
            pst.setString(3, email);
            pst.setString(4, age);
            pst.setString(5, gender);
            pst.setString(6, dept);
            pst.setString(7, doctor);
            pst.setString(8, appointmentDate);
            pst.setString(9, timeSlot);
            pst.setString(10, symptoms);
            pst.setString(11, bookingDate);
            
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, 
                "✅ Appointment Booked Successfully!\n\n" +
                "Patient: " + name + "\n" +
                "Doctor: " + doctor + "\n" +
                "Department: " + dept + "\n" +
                "Date: " + appointmentDate + "\n" +
                "Time: " + timeSlot,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            clearFields();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error booking appointment: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFields() {
        patientNameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        ageField.setText("");
        maleRadio.setSelected(false);
        femaleRadio.setSelected(false);
        departmentBox.setSelectedIndex(0);
        doctorBox.removeAllItems();
        dateField.setText("");
        timeSlotBox.setSelectedIndex(0);
        symptomsArea.setText("");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentBooking());
    }
}
