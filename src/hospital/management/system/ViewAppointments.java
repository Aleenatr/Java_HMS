package hospital.management.system;

import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ViewAppointments extends JFrame {
    
    JTable table;
    JTextField searchField;
    JComboBox<String> filterBox;
    JButton refreshButton, searchButton, cancelButton, backButton;
    
    public ViewAppointments() {
        setTitle("View Appointments");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 123));
        headerPanel.setBounds(0, 0, 1200, 80);
        headerPanel.setLayout(null);
        add(headerPanel);
        
        JLabel headerLabel = new JLabel("📋 Appointment Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(30, 20, 500, 40);
        headerPanel.add(headerLabel);
        
        // Search Panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(null);
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBounds(50, 100, 1100, 80);
        searchPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        add(searchPanel);
        
        JLabel searchLabel = new JLabel("Search by:");
        searchLabel.setBounds(20, 25, 100, 30);
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(searchLabel);
        
        filterBox = new JComboBox<>(new String[]{
            "Patient Name", "Phone", "Department", "Doctor", "Status"
        });
        filterBox.setBounds(120, 25, 150, 30);
        filterBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(filterBox);
        
        searchField = new JTextField();
        searchField.setBounds(290, 25, 300, 30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchField);
        
        searchButton = new JButton("🔍 Search");
        searchButton.setBounds(610, 22, 130, 36);
        searchButton.setBackground(new Color(0, 123, 123));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setFocusPainted(false);
        searchPanel.add(searchButton);
        
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setBounds(755, 22, 130, 36);
        refreshButton.setBackground(new Color(40, 167, 69));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        searchPanel.add(refreshButton);
        
        cancelButton = new JButton("❌ Cancel Appointment");
        cancelButton.setBounds(900, 22, 190, 36);
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setFocusPainted(false);
        searchPanel.add(cancelButton);
        
        // Table Panel
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBounds(50, 200, 1100, 400);
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 123), 2));
        add(tablePanel);
        
        table = new JTable();
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 123, 123));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Back Button
        backButton = new JButton("← BACK");
        backButton.setBounds(520, 620, 150, 40);
        backButton.setBackground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(backButton);
        
        // Action Listeners
        searchButton.addActionListener(e -> searchAppointments());
        refreshButton.addActionListener(e -> loadAllAppointments());
        cancelButton.addActionListener(e -> cancelAppointment());
        backButton.addActionListener(e -> dispose());
        
        // Load initial data
        loadAllAppointments();
        
        setVisible(true);
    }
    
    private void loadAllAppointments() {
        try {
            conn c = new conn();
            String query = "SELECT id AS 'ID', patient_name AS 'Patient', phone AS 'Phone', " +
                          "department AS 'Department', doctor AS 'Doctor', " +
                          "appointment_date AS 'Date', time_slot AS 'Time', " +
                          "status AS 'Status', booking_date AS 'Booked On' " +
                          "FROM Appointments ORDER BY appointment_date DESC, time_slot";
            ResultSet rs = c.statement.executeQuery(query);
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage());
        }
    }
    
    private void searchAppointments() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadAllAppointments();
            return;
        }
        
        String filter = (String) filterBox.getSelectedItem();
        String column = "";
        
        switch (filter) {
            case "Patient Name": column = "patient_name"; break;
            case "Phone": column = "phone"; break;
            case "Department": column = "department"; break;
            case "Doctor": column = "doctor"; break;
            case "Status": column = "status"; break;
        }
        
        try {
            conn c = new conn();
            String query = "SELECT id AS 'ID', patient_name AS 'Patient', phone AS 'Phone', " +
                          "department AS 'Department', doctor AS 'Doctor', " +
                          "appointment_date AS 'Date', time_slot AS 'Time', " +
                          "status AS 'Status', booking_date AS 'Booked On' " +
                          "FROM Appointments WHERE " + column + " LIKE ? " +
                          "ORDER BY appointment_date DESC";
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, "%" + searchText + "%");
            ResultSet rs = pst.executeQuery();
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }
    
    private void cancelAppointment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel!");
            return;
        }
        
        int appointmentId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        String patientName = table.getValueAt(selectedRow, 1).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel appointment for " + patientName + "?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                conn c = new conn();
                String query = "UPDATE Appointments SET status = 'Cancelled' WHERE id = ?";
                PreparedStatement pst = c.connection.prepareStatement(query);
                pst.setInt(1, appointmentId);
                pst.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully!");
                loadAllAppointments();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViewAppointments());
    }
}
