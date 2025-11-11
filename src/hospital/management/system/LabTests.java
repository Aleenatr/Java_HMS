package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LabTests extends JFrame implements ActionListener {
    
    JComboBox<String> patientChoice, testTypeBox;
    JTextField doctorNameField, resultField;
    JTextArea reportArea, testListArea;
    JButton requestButton, updateButton, viewButton, backButton;
    
    public LabTests() {
        setTitle("Laboratory Tests Management");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 123));
        headerPanel.setBounds(0, 0, 1100, 80);
        headerPanel.setLayout(null);
        add(headerPanel);
        
        JLabel headerLabel = new JLabel("🔬 Laboratory Tests");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(headerLabel);
        
        // Left Panel - Request Test
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBounds(30, 110, 500, 580);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            "Request New Test", 0, 0, new Font("Segoe UI", Font.BOLD, 16)));
        add(leftPanel);
        
        int yPos = 40;
        
        JLabel patientLabel = new JLabel("Select Patient:");
        patientLabel.setBounds(30, yPos, 150, 25);
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(patientLabel);
        
        patientChoice = new JComboBox<>();
        patientChoice.setBounds(30, yPos + 30, 430, 30);
        patientChoice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(patientChoice);
        
        yPos += 80;
        JLabel testLabel = new JLabel("Test Type:");
        testLabel.setBounds(30, yPos, 150, 25);
        testLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(testLabel);
        
        testTypeBox = new JComboBox<>(new String[]{
            "Select Test Type",
            "Blood Test - Complete Blood Count (CBC)",
            "Blood Test - Blood Sugar",
            "Blood Test - Lipid Profile",
            "Blood Test - Liver Function Test (LFT)",
            "Blood Test - Kidney Function Test (KFT)",
            "Urine Test - Routine",
            "Urine Test - Culture",
            "X-Ray - Chest",
            "X-Ray - Bone",
            "CT Scan",
            "MRI Scan",
            "Ultrasound",
            "ECG",
            "Echo Cardiogram",
            "COVID-19 RT-PCR",
            "Thyroid Profile"
        });
        testTypeBox.setBounds(30, yPos + 30, 430, 30);
        testTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(testTypeBox);
        
        yPos += 80;
        JLabel doctorLabel = new JLabel("Requested By (Doctor):");
        doctorLabel.setBounds(30, yPos, 200, 25);
        doctorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(doctorLabel);
        
        doctorNameField = new JTextField();
        doctorNameField.setBounds(30, yPos + 30, 430, 30);
        doctorNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(doctorNameField);
        
        yPos += 80;
        JLabel reportLabel = new JLabel("Additional Notes:");
        reportLabel.setBounds(30, yPos, 200, 25);
        reportLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(reportLabel);
        
        reportArea = new JTextArea();
        reportArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBounds(30, yPos + 30, 430, 100);
        leftPanel.add(reportScroll);
        
        yPos += 150;
        requestButton = new JButton("✅ REQUEST TEST");
        requestButton.setBounds(70, yPos, 180, 45);
        requestButton.setBackground(new Color(40, 167, 69));
        requestButton.setForeground(Color.WHITE);
        requestButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        requestButton.setFocusPainted(false);
        requestButton.addActionListener(this);
        leftPanel.add(requestButton);
        
        updateButton = new JButton("📝 UPDATE RESULT");
        updateButton.setBounds(270, yPos, 180, 45);
        updateButton.setBackground(new Color(0, 123, 123));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        updateButton.setFocusPainted(false);
        updateButton.addActionListener(this);
        leftPanel.add(updateButton);
        
        // Right Panel - Pending Tests
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBounds(560, 110, 510, 580);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            "Pending Tests", 0, 0, new Font("Segoe UI", Font.BOLD, 16)));
        add(rightPanel);
        
        testListArea = new JTextArea();
        testListArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        testListArea.setEditable(false);
        testListArea.setBackground(new Color(245, 245, 245));
        JScrollPane testScroll = new JScrollPane(testListArea);
        testScroll.setBounds(20, 40, 470, 450);
        rightPanel.add(testScroll);
        
        viewButton = new JButton("🔄 REFRESH");
        viewButton.setBounds(185, 510, 140, 40);
        viewButton.setBackground(new Color(0, 123, 123));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewButton.setFocusPainted(false);
        viewButton.addActionListener(this);
        rightPanel.add(viewButton);
        
        // Back Button
        backButton = new JButton("← BACK");
        backButton.setBounds(475, 710, 150, 35);
        backButton.setBackground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.addActionListener(this);
        add(backButton);
        
        loadPatients();
        loadPendingTests();
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
    
    private void loadPendingTests() {
        try {
            conn c = new conn();
            ResultSet rs = c.statement.executeQuery(
                "SELECT * FROM LabTests WHERE status = 'Pending' ORDER BY request_date DESC");
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s %-15s %-30s %-12s\n", 
                "ID", "Patient", "Test Type", "Requested"));
            sb.append("=".repeat(70)).append("\n");
            
            while (rs.next()) {
                String testType = rs.getString("test_type");
                if (testType.length() > 28) testType = testType.substring(0, 28) + "..";
                
                sb.append(String.format("%-5d %-15s %-30s %-12s\n",
                    rs.getInt("id"),
                    rs.getString("patient_number"),
                    testType,
                    rs.getString("request_date")));
            }
            
            testListArea.setText(sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == requestButton) {
            requestTest();
        } else if (e.getSource() == updateButton) {
            updateTestResult();
        } else if (e.getSource() == viewButton) {
            loadPendingTests();
        } else if (e.getSource() == backButton) {
            dispose();
        }
    }
    
    private void requestTest() {
        if (patientChoice.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient!");
            return;
        }
        
        String patientInfo = (String) patientChoice.getSelectedItem();
        String patientNumber = patientInfo.split(" - ")[0];
        String testType = (String) testTypeBox.getSelectedItem();
        String doctor = doctorNameField.getText().trim();
        String notes = reportArea.getText().trim();
        
        if (testType.equals("Select Test Type") || doctor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!");
            return;
        }
        
        try {
            conn c = new conn();
            String query = "INSERT INTO LabTests (patient_number, test_type, requested_by, " +
                          "notes, request_date, status) VALUES (?, ?, ?, ?, NOW(), 'Pending')";
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, patientNumber);
            pst.setString(2, testType);
            pst.setString(3, doctor);
            pst.setString(4, notes);
            
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "✅ Test requested successfully!");
            clearFields();
            loadPendingTests();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void updateTestResult() {
        String testId = JOptionPane.showInputDialog(this, "Enter Test ID to update:");
        if (testId == null || testId.trim().isEmpty()) return;
        
        String result = JOptionPane.showInputDialog(this, "Enter Test Result:");
        if (result == null || result.trim().isEmpty()) return;
        
        try {
            conn c = new conn();
            String query = "UPDATE LabTests SET result = ?, status = 'Completed', " +
                          "completion_date = NOW() WHERE id = ?";
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, result);
            pst.setInt(2, Integer.parseInt(testId));
            
            int updated = pst.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "✅ Test result updated successfully!");
                loadPendingTests();
            } else {
                JOptionPane.showMessageDialog(this, "Test ID not found!");
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void clearFields() {
        testTypeBox.setSelectedIndex(0);
        doctorNameField.setText("");
        reportArea.setText("");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LabTests());
    }
}
