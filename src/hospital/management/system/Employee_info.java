package hospital.management.system;

import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Employee_info extends JFrame {

    JTable table;
    JTextField nameField, ageField, phoneField, salaryField, gmailField, aadharField;

    Employee_info() {
        // Main panel
        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 1000, 600);
        panel.setBackground(new Color(109, 164, 170));
        panel.setLayout(null);
        add(panel);

        // ===== TABLE =====
        table = new JTable();
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 123, 123));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 150, 960, 320);
        panel.add(scrollPane);

        loadEmployeeData();

        // ===== FORM INPUTS =====
        JLabel labelTitle = new JLabel("Add Employee");
        labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labelTitle.setBounds(400, 10, 200, 30);
        labelTitle.setForeground(Color.WHITE);
        panel.add(labelTitle);

        JLabel label1 = new JLabel("Name:");
        label1.setBounds(30, 60, 60, 25);
        label1.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label1.setForeground(Color.WHITE);
        panel.add(label1);

        nameField = new JTextField();
        nameField.setBounds(90, 60, 120, 25);
        panel.add(nameField);

        JLabel label2 = new JLabel("Age:");
        label2.setBounds(230, 60, 40, 25);
        label2.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label2.setForeground(Color.WHITE);
        panel.add(label2);

        ageField = new JTextField();
        ageField.setBounds(270, 60, 60, 25);
        panel.add(ageField);

        JLabel label3 = new JLabel("Phone:");
        label3.setBounds(350, 60, 60, 25);
        label3.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label3.setForeground(Color.WHITE);
        panel.add(label3);

        phoneField = new JTextField();
        phoneField.setBounds(410, 60, 120, 25);
        panel.add(phoneField);

        JLabel label4 = new JLabel("Salary:");
        label4.setBounds(550, 60, 60, 25);
        label4.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label4.setForeground(Color.WHITE);
        panel.add(label4);

        salaryField = new JTextField();
        salaryField.setBounds(610, 60, 80, 25);
        panel.add(salaryField);

        JLabel label5 = new JLabel("Gmail:");
        label5.setBounds(710, 60, 60, 25);
        label5.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label5.setForeground(Color.WHITE);
        panel.add(label5);

        gmailField = new JTextField();
        gmailField.setBounds(770, 60, 150, 25);
        panel.add(gmailField);

        JLabel label6 = new JLabel("Aadhar:");
        label6.setBounds(30, 100, 60, 25);
        label6.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label6.setForeground(Color.WHITE);
        panel.add(label6);

        aadharField = new JTextField();
        aadharField.setBounds(90, 100, 120, 25);
        panel.add(aadharField);

        // ===== BUTTONS =====
        JButton addButton = new JButton("➕ ADD EMPLOYEE");
        addButton.setBounds(240, 97, 170, 36);
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setFocusPainted(false);
        panel.add(addButton);

        JButton deleteButton = new JButton("🗑️ DELETE EMPLOYEE");
        deleteButton.setBounds(425, 97, 180, 36);
        deleteButton.setBackground(new Color(220, 53, 69)); // red color
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setFocusPainted(false);
        panel.add(deleteButton);

        JButton refreshButton = new JButton("🔄 REFRESH");
        refreshButton.setBounds(620, 97, 130, 36);
        refreshButton.setBackground(new Color(0, 123, 123));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        panel.add(refreshButton);

        JButton backButton = new JButton("← BACK");
        backButton.setBounds(765, 97, 110, 36);
        backButton.setBackground(new Color(108, 117, 125));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.setFocusPainted(false);
        panel.add(backButton);

        // ===== ACTIONS =====
        addButton.addActionListener(e -> addEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());
        refreshButton.addActionListener(e -> loadEmployeeData());
        backButton.addActionListener(e -> setVisible(false));

        // ===== FRAME SETTINGS =====
        setUndecorated(true);
        setSize(1000, 600);
        setLocation(350, 230);
        setLayout(null);
        setVisible(true);
    }

    void loadEmployeeData() {
        try {
            conn c = new conn();
            String q = "SELECT * FROM EMP_INFO";
            ResultSet rs = c.statement.executeQuery(q);
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addEmployee() {
        String name = nameField.getText();
        String age = ageField.getText();
        String phone = phoneField.getText();
        String salary = salaryField.getText();
        String gmail = gmailField.getText();
        String aadhar = aadharField.getText();

        if (name.isEmpty() || age.isEmpty() || phone.isEmpty() || salary.isEmpty() || gmail.isEmpty() || aadhar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields before adding.");
            return;
        }

        try {
            conn c = new conn();
            String q = "INSERT INTO EMP_INFO (Name, Age, Phone_number, Salary, Gmail, Aadhar_number) " +
                    "VALUES('" + name + "', '" + age + "', '" + phone + "', '" + salary + "', '" + gmail + "', '" + aadhar + "')";
            c.statement.executeUpdate(q);
            JOptionPane.showMessageDialog(null, "Employee Added Successfully!");
            loadEmployeeData();

            // Clear fields
            nameField.setText("");
            ageField.setText("");
            phoneField.setText("");
            salaryField.setText("");
            gmailField.setText("");
            aadharField.setText("");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding employee. Check database connection.");
        }
    }

    void deleteEmployee() {
        String aadhar = aadharField.getText();

        if (aadhar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter the Aadhar number of the employee to delete.");
            return;
        }

        try {
            conn c = new conn();
            String q = "DELETE FROM EMP_INFO WHERE Aadhar_number = '" + aadhar + "'";
            int rowsAffected = c.statement.executeUpdate(q);

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Employee Deleted Successfully!");
                loadEmployeeData();
                aadharField.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "No employee found with this Aadhar number.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting employee. Check database connection.");
        }
    }

    public static void main(String[] args) {
        new Employee_info();
    }
}
