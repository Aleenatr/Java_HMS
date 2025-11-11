package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MedicalRecords extends JFrame implements ActionListener {
    
    JComboBox<String> patientChoice;
    JTextArea recordsArea;
    JButton viewButton, addButton, refreshButton, backButton;
    
    public MedicalRecords() {
        setTitle("Medical Records");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 123));
        headerPanel.setBounds(0, 0, 1000, 80);
        headerPanel.setLayout(null);
        add(headerPanel);
        
        JLabel headerLabel = new JLabel("📋 Medical Records");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(headerLabel);
        
        // Selection Panel
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(null);
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBounds(50, 110, 900, 80);
        selectionPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        add(selectionPanel);
        
        JLabel patientLabel = new JLabel("Select Patient:");
        patientLabel.setBounds(30, 25, 120, 30);
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectionPanel.add(patientLabel);
        
        patientChoice = new JComboBox<>();
        patientChoice.setBounds(160, 25, 400, 30);
        patientChoice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectionPanel.add(patientChoice);
        
        viewButton = new JButton("👁️ View Records");
        viewButton.setBounds(580, 25, 150, 35);
        viewButton.setBackground(new Color(0, 123, 255));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewButton.setFocusPainted(false);
        viewButton.addActionListener(this);
        selectionPanel.add(viewButton);
        
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setBounds(750, 25, 120, 35);
        refreshButton.setBackground(new Color(40, 167, 69));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(this);
        selectionPanel.add(refreshButton);
        
        // Records Display Panel
        JPanel recordsPanel = new JPanel();
        recordsPanel.setLayout(new BorderLayout());
        recordsPanel.setBounds(50, 210, 900, 410);
        recordsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            "Patient Medical History", 0, 0, new Font("Segoe UI", Font.BOLD, 16)));
        add(recordsPanel);
        
        recordsArea = new JTextArea();
        recordsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recordsArea.setEditable(false);
        recordsArea.setLineWrap(true);
        recordsArea.setWrapStyleWord(true);
        recordsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(recordsArea);
        recordsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel
        addButton = new JButton("➕ ADD NEW RECORD");
        addButton.setBounds(350, 635, 180, 40);
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setFocusPainted(false);
        addButton.addActionListener(this);
        add(addButton);
        
        backButton = new JButton("← BACK");
        backButton.setBounds(550, 635, 120, 40);
        backButton.setBackground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.addActionListener(this);
        add(backButton);
        
        loadPatients();
        setVisible(true);
    }
    
    private void loadPatients() {
        patientChoice.removeAllItems();
        patientChoice.addItem("-- Select Patient --");
        try {
            conn c = new conn();
            ResultSet rs = c.statement.executeQuery(
                "SELECT Number, Name FROM Patient_Info ORDER BY Name");
            while (rs.next()) {
                patientChoice.addItem(rs.getString("Number") + " - " + rs.getString("Name"));
            }
            if (patientChoice.getItemCount() == 1) {
                JOptionPane.showMessageDialog(this, 
                    "No patients found in database!\nPlease add patients first.", 
                    "No Data", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading patients: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == viewButton) {
            viewRecords();
        } else if (e.getSource() == addButton) {
            addNewRecord();
        } else if (e.getSource() == refreshButton) {
            viewRecords();
        } else if (e.getSource() == backButton) {
            dispose();
        }
    }
    
    private void viewRecords() {
        if (patientChoice.getSelectedItem() == null || 
            patientChoice.getSelectedItem().toString().startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please select a patient!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selected = (String) patientChoice.getSelectedItem();
        String patientNumber = selected.split(" - ")[0];
        
        try {
            conn c = new conn();
            
            // Get patient basic info
            ResultSet patientRs = c.statement.executeQuery(
                "SELECT * FROM Patient_Info WHERE Number = '" + patientNumber + "'");
            
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════════════════\n");
            sb.append("                       PATIENT MEDICAL RECORDS\n");
            sb.append("═══════════════════════════════════════════════════════════════════════\n\n");
            
            if (patientRs.next()) {
                sb.append("PATIENT INFORMATION\n");
                sb.append("─────────────────────────────────────────────────────────────────────\n");
                sb.append(String.format("Patient ID     : %s\n", patientRs.getString("Number")));
                sb.append(String.format("Name           : %s\n", patientRs.getString("Name")));
                sb.append(String.format("Gender         : %s\n", patientRs.getString("Gender")));
                sb.append(String.format("Current Disease: %s\n", patientRs.getString("Disease")));
                sb.append(String.format("Room Number    : %s\n", patientRs.getString("Room_Number")));
                sb.append(String.format("Admission Date : %s\n", patientRs.getString("Time")));
                sb.append("\n");
            }
            
            // Get medical records
            ResultSet recordsRs = c.statement.executeQuery(
                "SELECT * FROM MedicalRecords WHERE patient_number = '" + patientNumber + "' " +
                "ORDER BY visit_date DESC");
            
            sb.append("MEDICAL HISTORY\n");
            sb.append("─────────────────────────────────────────────────────────────────────\n");
            
            int recordCount = 0;
            while (recordsRs.next()) {
                recordCount++;
                sb.append(String.format("\n🏥 VISIT #%d\n", recordCount));
                sb.append(String.format("   Date        : %s\n", recordsRs.getString("visit_date")));
                sb.append(String.format("   Doctor      : %s\n", recordsRs.getString("doctor_name")));
                sb.append(String.format("   Diagnosis   : %s\n", recordsRs.getString("diagnosis")));
                sb.append(String.format("   Prescription: %s\n", recordsRs.getString("prescription")));
                String notes = recordsRs.getString("notes");
                if (notes != null && !notes.isEmpty()) {
                    sb.append(String.format("   Notes       : %s\n", notes));
                }
                sb.append("\n");
            }
            
            if (recordCount == 0) {
                sb.append("\nNo medical records found for this patient.\n");
            }
            
            sb.append("\n═══════════════════════════════════════════════════════════════════════\n");
            sb.append("                          END OF RECORDS\n");
            sb.append("═══════════════════════════════════════════════════════════════════════\n");
            
            recordsArea.setText(sb.toString());
            recordsArea.setCaretPosition(0);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading records: " + ex.getMessage());
        }
    }
    
    private void addNewRecord() {
        if (patientChoice.getSelectedItem() == null || 
            patientChoice.getSelectedItem().toString().startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please select a patient first!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selected = (String) patientChoice.getSelectedItem();
        String patientNumber = selected.split(" - ")[0];
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField dateField = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format(new java.util.Date()));
        JTextField doctorField = new JTextField();
        JTextArea diagnosisArea = new JTextArea(3, 20);
        JTextArea prescriptionArea = new JTextArea(3, 20);
        JTextArea notesArea = new JTextArea(3, 20);
        
        panel.add(new JLabel("Visit Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Doctor Name:"));
        panel.add(doctorField);
        panel.add(new JLabel("Diagnosis:"));
        panel.add(new JScrollPane(diagnosisArea));
        panel.add(new JLabel("Prescription:"));
        panel.add(new JScrollPane(prescriptionArea));
        panel.add(new JLabel("Additional Notes:"));
        panel.add(new JScrollPane(notesArea));
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Medical Record",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String date = dateField.getText().trim();
            String doctor = doctorField.getText().trim();
            String diagnosis = diagnosisArea.getText().trim();
            String prescription = prescriptionArea.getText().trim();
            String notes = notesArea.getText().trim();
            
            if (date.isEmpty() || doctor.isEmpty() || diagnosis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields!");
                return;
            }
            
            try {
                conn c = new conn();
                String query = "INSERT INTO MedicalRecords (patient_number, visit_date, diagnosis, " +
                              "prescription, doctor_name, notes) VALUES ('" + patientNumber + "', '" + 
                              date + "', '" + diagnosis.replace("'", "''") + "', '" + 
                              prescription.replace("'", "''") + "', '" + doctor.replace("'", "''") + 
                              "', '" + notes.replace("'", "''") + "')";
                
                c.statement.executeUpdate(query);
                
                JOptionPane.showMessageDialog(this, "✅ Medical record added successfully!");
                viewRecords();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding record: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MedicalRecords());
    }
}
