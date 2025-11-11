package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Billing extends JFrame implements ActionListener {
    
    JComboBox<String> patientChoice;
    JTextField roomChargeField, medicineField, labTestField, doctorFeeField, otherField, totalField;
    JTextArea detailsArea;
    JButton calculateButton, generateButton, printButton, backButton;
    
    public Billing() {
        setTitle("Patient Billing System");
        setSize(900, 750);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 123));
        headerPanel.setBounds(0, 0, 900, 80);
        headerPanel.setLayout(null);
        add(headerPanel);
        
        JLabel headerLabel = new JLabel("💰 Patient Billing");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(headerLabel);
        
        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBounds(50, 110, 800, 550);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        add(mainPanel);
        
        int yPos = 30;
        
        // Patient Selection
        JLabel patientLabel = new JLabel("Select Patient:");
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        patientLabel.setBounds(50, yPos, 150, 25);
        mainPanel.add(patientLabel);
        
        patientChoice = new JComboBox<>();
        patientChoice.setBounds(220, yPos, 300, 30);
        patientChoice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(patientChoice);
        
        JButton loadButton = new JButton("Load");
        loadButton.setBounds(540, yPos, 100, 30);
        loadButton.setBackground(new Color(0, 123, 123));
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(this);
        mainPanel.add(loadButton);
        
        yPos += 60;
        
        // Patient Details
        JLabel detailsLabel = new JLabel("Patient Details:");
        detailsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailsLabel.setBounds(50, yPos, 150, 25);
        mainPanel.add(detailsLabel);
        
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsArea.setBackground(new Color(245, 245, 245));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBounds(50, yPos + 30, 700, 80);
        mainPanel.add(detailsScroll);
        
        yPos += 130;
        
        // Billing Items
        JLabel itemsLabel = new JLabel("Billing Items:");
        itemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        itemsLabel.setBounds(50, yPos, 150, 25);
        mainPanel.add(itemsLabel);
        
        yPos += 40;
        
        // Room Charges
        JLabel roomLabel = new JLabel("Room Charges:");
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomLabel.setBounds(80, yPos, 150, 25);
        mainPanel.add(roomLabel);
        
        roomChargeField = new JTextField("0");
        roomChargeField.setBounds(250, yPos, 150, 30);
        roomChargeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(roomChargeField);
        
        // Medicine
        yPos += 40;
        JLabel medicineLabel = new JLabel("Medicine:");
        medicineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        medicineLabel.setBounds(80, yPos, 150, 25);
        mainPanel.add(medicineLabel);
        
        medicineField = new JTextField("0");
        medicineField.setBounds(250, yPos, 150, 30);
        medicineField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(medicineField);
        
        // Lab Tests
        yPos += 40;
        JLabel labLabel = new JLabel("Lab Tests:");
        labLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labLabel.setBounds(80, yPos, 150, 25);
        mainPanel.add(labLabel);
        
        labTestField = new JTextField("0");
        labTestField.setBounds(250, yPos, 150, 30);
        labTestField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(labTestField);
        
        // Doctor Fee
        yPos += 40;
        JLabel doctorLabel = new JLabel("Doctor Fee:");
        doctorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        doctorLabel.setBounds(80, yPos, 150, 25);
        mainPanel.add(doctorLabel);
        
        doctorFeeField = new JTextField("0");
        doctorFeeField.setBounds(250, yPos, 150, 30);
        doctorFeeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(doctorFeeField);
        
        // Other Charges
        yPos += 40;
        JLabel otherLabel = new JLabel("Other Charges:");
        otherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        otherLabel.setBounds(80, yPos, 150, 25);
        mainPanel.add(otherLabel);
        
        otherField = new JTextField("0");
        otherField.setBounds(250, yPos, 150, 30);
        otherField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mainPanel.add(otherField);
        
        // Calculate Button
        calculateButton = new JButton("🧮 Calculate Total");
        calculateButton.setBounds(450, yPos - 80, 160, 35);
        calculateButton.setBackground(new Color(0, 123, 123));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        calculateButton.setFocusPainted(false);
        calculateButton.addActionListener(this);
        mainPanel.add(calculateButton);
        
        // Total
        yPos += 50;
        JSeparator separator = new JSeparator();
        separator.setBounds(50, yPos, 700, 2);
        mainPanel.add(separator);
        
        yPos += 15;
        JLabel totalLabel = new JLabel("Total Amount:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setBounds(80, yPos, 150, 30);
        mainPanel.add(totalLabel);
        
        totalField = new JTextField("₹ 0");
        totalField.setEditable(false);
        totalField.setBounds(250, yPos, 150, 35);
        totalField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalField.setForeground(new Color(0, 123, 123));
        mainPanel.add(totalField);
        
        // Buttons
        generateButton = new JButton("✅ Generate Bill");
        generateButton.setBounds(430, yPos, 160, 35);
        generateButton.setBackground(new Color(40, 167, 69));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(this);
        mainPanel.add(generateButton);
        
        printButton = new JButton("🖨️ Print Bill");
        printButton.setBounds(610, yPos, 140, 35);
        printButton.setBackground(new Color(108, 117, 125));
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printButton.setFocusPainted(false);
        printButton.addActionListener(this);
        mainPanel.add(printButton);
        
        // Back Button
        backButton = new JButton("← BACK");
        backButton.setBounds(350, 680, 150, 40);
        backButton.setBackground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.addActionListener(this);
        add(backButton);
        
        loadPatients();
        setVisible(true);
    }
    
    private void loadPatients() {
        try {
            conn c = new conn();
            ResultSet rs = c.statement.executeQuery("SELECT Number, Name FROM Patient_Info");
            while (rs.next()) {
                patientChoice.addItem(rs.getString("Number") + " - " + rs.getString("Name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Load")) {
            loadPatientDetails();
        } else if (e.getSource() == calculateButton) {
            calculateTotal();
        } else if (e.getSource() == generateButton) {
            generateBill();
        } else if (e.getSource() == printButton) {
            printBill();
        } else if (e.getSource() == backButton) {
            dispose();
        }
    }
    
    private void loadPatientDetails() {
        String selected = (String) patientChoice.getSelectedItem();
        if (selected == null) return;
        
        String patientNumber = selected.split(" - ")[0];
        
        try {
            conn c = new conn();
            ResultSet rs = c.statement.executeQuery(
                "SELECT * FROM Patient_Info WHERE Number = '" + patientNumber + "'");
            
            if (rs.next()) {
                String details = "Patient ID: " + rs.getString("Number") + "\n" +
                                "Name: " + rs.getString("Name") + "\n" +
                                "Gender: " + rs.getString("Gender") + "\n" +
                                "Disease: " + rs.getString("Disease") + "\n" +
                                "Room: " + rs.getString("Room_Number") + "\n" +
                                "Admission Date: " + rs.getString("Time");
                detailsArea.setText(details);
                
                // Load room charges
                String roomNo = rs.getString("Room_Number");
                ResultSet roomRs = c.statement.executeQuery(
                    "SELECT Price FROM Room WHERE room_no = '" + roomNo + "'");
                if (roomRs.next()) {
                    roomChargeField.setText(roomRs.getString("Price"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void calculateTotal() {
        try {
            double room = Double.parseDouble(roomChargeField.getText());
            double medicine = Double.parseDouble(medicineField.getText());
            double lab = Double.parseDouble(labTestField.getText());
            double doctor = Double.parseDouble(doctorFeeField.getText());
            double other = Double.parseDouble(otherField.getText());
            
            double total = room + medicine + lab + doctor + other;
            totalField.setText("₹ " + String.format("%.2f", total));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }
    
    private void generateBill() {
        if (patientChoice.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient!");
            return;
        }
        
        calculateTotal();
        
        try {
            String patientNumber = ((String) patientChoice.getSelectedItem()).split(" - ")[0];
            conn c = new conn();
            
            String query = "INSERT INTO Bills (patient_number, room_charge, medicine, " +
                          "lab_test, doctor_fee, other_charges, total_amount, bill_date) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
            
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, patientNumber);
            pst.setDouble(2, Double.parseDouble(roomChargeField.getText()));
            pst.setDouble(3, Double.parseDouble(medicineField.getText()));
            pst.setDouble(4, Double.parseDouble(labTestField.getText()));
            pst.setDouble(5, Double.parseDouble(doctorFeeField.getText()));
            pst.setDouble(6, Double.parseDouble(otherField.getText()));
            pst.setString(7, totalField.getText().replace("₹ ", ""));
            
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "✅ Bill Generated Successfully!");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating bill: " + ex.getMessage());
        }
    }
    
    private void printBill() {
        JOptionPane.showMessageDialog(this, "Print functionality will open system print dialog.\n" +
            "Bill has been prepared and can be printed.");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Billing());
    }
}
