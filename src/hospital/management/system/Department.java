package hospital.management.system;

import net.proteanit.sql.DbUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

public class Department extends JFrame {
    
    JTable table;
    
    Department(){
        setTitle("Hospital Departments Directory");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 123));
        headerPanel.setBounds(0, 0, 900, 90);
        headerPanel.setLayout(null);
        add(headerPanel);

        JLabel headerIcon = new JLabel("🏥");
        headerIcon.setFont(new Font("Segoe UI", Font.BOLD, 48));
        headerIcon.setBounds(30, 15, 80, 60);
        headerPanel.add(headerIcon);

        JLabel headerLabel = new JLabel("HOSPITAL DEPARTMENTS");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(120, 20, 600, 50);
        headerPanel.add(headerLabel);

        JLabel subHeader = new JLabel("Contact Information & Emergency Numbers");
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subHeader.setForeground(new Color(200, 230, 230));
        subHeader.setBounds(120, 55, 500, 25);
        headerPanel.add(subHeader);

        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(null);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBounds(50, 120, 800, 60);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        add(infoPanel);

        JLabel infoIcon = new JLabel("ℹ️");
        infoIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        infoIcon.setBounds(15, 15, 40, 30);
        infoPanel.add(infoIcon);

        JLabel infoText = new JLabel("24/7 Emergency Services Available - All Departments Equipped with Latest Technology");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoText.setForeground(new Color(52, 58, 64));
        infoText.setBounds(60, 15, 700, 30);
        infoPanel.add(infoText);

        // Table Panel
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBounds(50, 200, 800, 360);
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 123), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        add(tablePanel);

        // Create table with better styling
        table = new JTable();
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(40);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Style table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(0, 123, 123));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        header.setReorderingAllowed(false);

        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Load data
        try{
            conn c = new conn();
            String q = "select * from department ORDER BY Department";
            ResultSet resultSet = c.statement.executeQuery(q);
            table.setModel(DbUtils.resultSetToTableModel(resultSet));
            
            // Apply center alignment to all columns
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
            
            // Set column widths
            if (table.getColumnCount() >= 2) {
                table.getColumnModel().getColumn(0).setPreferredWidth(400);
                table.getColumnModel().getColumn(1).setPreferredWidth(300);
            }
            
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading department data: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Back Button
        JButton b1 = new JButton("← BACK");
        b1.setBounds(380, 575, 140, 45);
        b1.setBackground(new Color(108, 117, 125));
        b1.setForeground(Color.WHITE);
        b1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b1.setFocusPainted(false);
        b1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(108, 117, 125), 2),
            BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        add(b1);
        
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Department());
    }
}

