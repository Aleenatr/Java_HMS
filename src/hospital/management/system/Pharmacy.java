package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Pharmacy extends JFrame implements ActionListener {
    
    JTextField medicineNameField, quantityField, priceField, manufacturerField, searchField;
    JComboBox<String> categoryBox;
    JTextArea inventoryArea;
    JButton addButton, searchButton, refreshButton, backButton;
    
    public Pharmacy() {
        setTitle("Pharmacy Management");
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
        
        JLabel headerLabel = new JLabel("💊 Pharmacy Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(30, 20, 400, 40);
        headerPanel.add(headerLabel);
        
        // Left Panel - Add Medicine
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBounds(30, 110, 450, 520);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            "Add New Medicine", 0, 0, new Font("Segoe UI", Font.BOLD, 16)));
        add(leftPanel);
        
        int yPos = 40;
        
        JLabel nameLabel = new JLabel("Medicine Name:");
        nameLabel.setBounds(30, yPos, 150, 25);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(nameLabel);
        
        medicineNameField = new JTextField();
        medicineNameField.setBounds(30, yPos + 30, 380, 30);
        medicineNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(medicineNameField);
        
        yPos += 80;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(30, yPos, 150, 25);
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(categoryLabel);
        
        categoryBox = new JComboBox<>(new String[]{
            "Select Category", "Antibiotics", "Pain Relievers", "Antiseptics",
            "Vitamins", "Cardiac", "Diabetic", "Respiratory", "Digestive", "Other"
        });
        categoryBox.setBounds(30, yPos + 30, 380, 30);
        categoryBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(categoryBox);
        
        yPos += 80;
        JLabel manufacturerLabel = new JLabel("Manufacturer:");
        manufacturerLabel.setBounds(30, yPos, 150, 25);
        manufacturerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(manufacturerLabel);
        
        manufacturerField = new JTextField();
        manufacturerField.setBounds(30, yPos + 30, 380, 30);
        manufacturerField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(manufacturerField);
        
        yPos += 80;
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(30, yPos, 150, 25);
        quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(quantityLabel);
        
        quantityField = new JTextField();
        quantityField.setBounds(30, yPos + 30, 180, 30);
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(quantityField);
        
        JLabel priceLabel = new JLabel("Price (₹):");
        priceLabel.setBounds(230, yPos, 150, 25);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftPanel.add(priceLabel);
        
        priceField = new JTextField();
        priceField.setBounds(230, yPos + 30, 180, 30);
        priceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leftPanel.add(priceField);
        
        yPos += 80;
        addButton = new JButton("✅ ADD MEDICINE");
        addButton.setBounds(100, yPos, 240, 45);
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setFocusPainted(false);
        addButton.addActionListener(this);
        leftPanel.add(addButton);
        
        // Right Panel - Inventory
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBounds(510, 110, 460, 520);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            "Medicine Inventory", 0, 0, new Font("Segoe UI", Font.BOLD, 16)));
        add(rightPanel);
        
        JLabel searchLabel = new JLabel("Search Medicine:");
        searchLabel.setBounds(30, 40, 150, 25);
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rightPanel.add(searchLabel);
        
        searchField = new JTextField();
        searchField.setBounds(30, 70, 250, 30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rightPanel.add(searchField);
        
        searchButton = new JButton("🔍 Search");
        searchButton.setBounds(290, 70, 120, 30);
        searchButton.setBackground(new Color(0, 123, 123));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(this);
        rightPanel.add(searchButton);
        
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setBounds(290, 110, 120, 30);
        refreshButton.setBackground(new Color(40, 167, 69));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(this);
        rightPanel.add(refreshButton);
        
        inventoryArea = new JTextArea();
        inventoryArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        inventoryArea.setEditable(false);
        inventoryArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(inventoryArea);
        scrollPane.setBounds(30, 150, 400, 330);
        rightPanel.add(scrollPane);
        
        // Back Button
        backButton = new JButton("← BACK");
        backButton.setBounds(420, 650, 150, 40);
        backButton.setBackground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.addActionListener(this);
        add(backButton);
        
        loadInventory();
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addMedicine();
        } else if (e.getSource() == searchButton) {
            searchMedicine();
        } else if (e.getSource() == refreshButton) {
            loadInventory();
        } else if (e.getSource() == backButton) {
            dispose();
        }
    }
    
    private void addMedicine() {
        String name = medicineNameField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String manufacturer = manufacturerField.getText().trim();
        String quantity = quantityField.getText().trim();
        String price = priceField.getText().trim();
        
        if (name.isEmpty() || category.equals("Select Category") || 
            manufacturer.isEmpty() || quantity.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        try {
            conn c = new conn();
            String query = "INSERT INTO Pharmacy (medicine_name, category, manufacturer, " +
                          "quantity, price, added_date) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setString(3, manufacturer);
            pst.setInt(4, Integer.parseInt(quantity));
            pst.setDouble(5, Double.parseDouble(price));
            
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "✅ Medicine added successfully!");
            clearFields();
            loadInventory();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void loadInventory() {
        try {
            conn c = new conn();
            ResultSet rs = c.statement.executeQuery(
                "SELECT * FROM Pharmacy ORDER BY medicine_name");
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s %-25s %-15s %-8s %s\n", 
                "ID", "Medicine", "Category", "Qty", "Price"));
            sb.append("=".repeat(70)).append("\n");
            
            while (rs.next()) {
                sb.append(String.format("%-5d %-25s %-15s %-8d ₹%.2f\n",
                    rs.getInt("id"),
                    rs.getString("medicine_name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")));
            }
            
            inventoryArea.setText(sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void searchMedicine() {
        String search = searchField.getText().trim();
        if (search.isEmpty()) {
            loadInventory();
            return;
        }
        
        try {
            conn c = new conn();
            String query = "SELECT * FROM Pharmacy WHERE medicine_name LIKE ? OR category LIKE ?";
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setString(1, "%" + search + "%");
            pst.setString(2, "%" + search + "%");
            ResultSet rs = pst.executeQuery();
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s %-25s %-15s %-8s %s\n", 
                "ID", "Medicine", "Category", "Qty", "Price"));
            sb.append("=".repeat(70)).append("\n");
            
            while (rs.next()) {
                sb.append(String.format("%-5d %-25s %-15s %-8d ₹%.2f\n",
                    rs.getInt("id"),
                    rs.getString("medicine_name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")));
            }
            
            inventoryArea.setText(sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void clearFields() {
        medicineNameField.setText("");
        categoryBox.setSelectedIndex(0);
        manufacturerField.setText("");
        quantityField.setText("");
        priceField.setText("");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Pharmacy());
    }
}
