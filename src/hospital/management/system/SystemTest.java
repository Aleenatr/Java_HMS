package hospital.management.system;

import javax.swing.*;
import java.awt.*;

/**
 * System Diagnostics and Test Utility
 * Java 21 LTS Compatible
 * 
 * This utility checks if all components are properly configured
 */
public class SystemTest extends JFrame {

    private JTextArea outputArea;
    private StringBuilder log;

    public SystemTest() {
        setTitle("Hospital Management System - Diagnostics");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("System Diagnostics & Test", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(0, 123, 123));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(700, 50));
        add(header, BorderLayout.NORTH);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setBackground(new Color(30, 30, 30));
        outputArea.setForeground(new Color(0, 255, 0));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        
        JButton testButton = new JButton("Run All Tests");
        testButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        testButton.setBackground(new Color(0, 123, 123));
        testButton.setForeground(Color.WHITE);
        testButton.setFocusPainted(false);
        testButton.addActionListener(e -> runTests());
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(testButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        log = new StringBuilder();
        setVisible(true);
    }

    private void runTests() {
        outputArea.setText("");
        log = new StringBuilder();
        
        printHeader("HOSPITAL MANAGEMENT SYSTEM - DIAGNOSTICS");
        printLine("Test Started: " + new java.util.Date());
        printSeparator();

        // Test 1: Java Version
        testJavaVersion();
        
        // Test 2: Required Files
        testRequiredFiles();
        
        // Test 3: Database Connection
        testDatabaseConnection();
        
        // Test 4: Database Tables
        testDatabaseTables();
        
        printSeparator();
        printLine("Test Completed: " + new java.util.Date());
        
        outputArea.setText(log.toString());
        outputArea.setCaretPosition(0);
    }

    private void testJavaVersion() {
        printTest("Java Version Check");
        try {
            String version = System.getProperty("java.version");
            String vendor = System.getProperty("java.vendor");
            
            printInfo("Java Version: " + version);
            printInfo("Java Vendor: " + vendor);
            
            if (version.startsWith("21")) {
                printSuccess("✅ Java 21 detected - Correct version!");
            } else {
                printWarning("⚠️ Java " + version + " detected. Recommended: Java 21 LTS");
            }
        } catch (Exception e) {
            printError("❌ Failed to detect Java version: " + e.getMessage());
        }
        printSeparator();
    }

    private void testRequiredFiles() {
        printTest("Required Files Check");
        
        String[] requiredFiles = {
            "mysql-connector-java-8.0.28.jar",
            "ResultSet2xml.jar",
            "database_setup.sql"
        };
        
        for (String file : requiredFiles) {
            java.io.File f = new java.io.File(file);
            if (f.exists()) {
                printSuccess("✅ Found: " + file);
            } else {
                printError("❌ Missing: " + file);
            }
        }
        printSeparator();
    }

    private void testDatabaseConnection() {
        printTest("Database Connection Check");
        try {
            conn c = new conn();
            if (c.connection != null && !c.connection.isClosed()) {
                printSuccess("✅ Database connection successful!");
                printInfo("Database URL: jdbc:mysql://localhost:3306/hospital_management_system");
                printInfo("Connection Status: Active");
                c.close();
            } else {
                printError("❌ Database connection is null or closed");
            }
        } catch (Exception e) {
            printError("❌ Database connection failed!");
            printError("   Error: " + e.getMessage());
            printWarning("   Please check:");
            printWarning("   1. MySQL server is running");
            printWarning("   2. Database 'hospital_management_system' exists");
            printWarning("   3. Username and password in conn.java are correct");
        }
        printSeparator();
    }

    private void testDatabaseTables() {
        printTest("Database Tables Check");
        
        String[] requiredTables = {
            "login", "Patient_Info", "Room", 
            "EMP_INFO", "department", "Ambulance"
        };
        
        try {
            conn c = new conn();
            java.sql.DatabaseMetaData meta = c.connection.getMetaData();
            
            for (String table : requiredTables) {
                java.sql.ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"});
                if (rs.next()) {
                    printSuccess("✅ Table found: " + table);
                    
                    // Count records
                    try {
                        java.sql.ResultSet countRs = c.statement.executeQuery("SELECT COUNT(*) FROM " + table);
                        if (countRs.next()) {
                            int count = countRs.getInt(1);
                            printInfo("   Records: " + count);
                        }
                    } catch (Exception e) {
                        printWarning("   Could not count records");
                    }
                } else {
                    printError("❌ Table missing: " + table);
                }
            }
            c.close();
        } catch (Exception e) {
            printError("❌ Failed to check tables: " + e.getMessage());
        }
        printSeparator();
    }

    private void printHeader(String text) {
        log.append("╔══════════════════════════════════════════════════════════════╗\n");
        log.append("║  ").append(centerText(text, 58)).append("  ║\n");
        log.append("╚══════════════════════════════════════════════════════════════╝\n\n");
    }

    private void printTest(String test) {
        log.append("\n[TEST] ").append(test).append("\n");
    }

    private void printSuccess(String message) {
        log.append("  ").append(message).append("\n");
    }

    private void printError(String message) {
        log.append("  ").append(message).append("\n");
    }

    private void printWarning(String message) {
        log.append("  ").append(message).append("\n");
    }

    private void printInfo(String message) {
        log.append("    ").append(message).append("\n");
    }

    private void printLine(String text) {
        log.append(text).append("\n");
    }

    private void printSeparator() {
        log.append("──────────────────────────────────────────────────────────────\n");
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SystemTest();
        });
    }
}
