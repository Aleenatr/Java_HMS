package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.Date;

public class patient_discharge extends JFrame {
    patient_discharge() {
        setTitle("Patient Discharge - CHECK-OUT");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 248, 255));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(220, 53, 69));
        headerPanel.setBounds(0, 0, 900, 80);
        headerPanel.setLayout(null);
        add(headerPanel);

        JLabel headerLabel = new JLabel("🏥 PATIENT CHECK-OUT");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(50, 20, 500, 40);
        headerPanel.add(headerLabel);

        // Main Panel with modern card design
        JPanel panel = new JPanel();
        panel.setBounds(50, 120, 800, 400);
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69), 3),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        add(panel);

        // Title decoration
        JLabel decorLine = new JLabel("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        decorLine.setFont(new Font("Segoe UI", Font.BOLD, 16));
        decorLine.setForeground(new Color(220, 53, 69));
        decorLine.setBounds(30, 10, 740, 25);
        panel.add(decorLine);

        // Customer ID
        JLabel label2 = new JLabel("Customer Id");
        label2.setBounds(80, 70, 150, 30);
        label2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label2.setForeground(new Color(52, 58, 64));
        panel.add(label2);

        Choice choice = new Choice();
        choice.setBounds(280, 70, 170, 30);
        choice.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        choice.setBackground(new Color(233, 236, 239));
        panel.add(choice);

        try {
            conn c = new conn();
            ResultSet resultSet = c.statement.executeQuery("select * from Patient_Info");
            while (resultSet.next()) {
                choice.add(resultSet.getString("number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Room Number
        JLabel label3 = new JLabel("Room Number");
        label3.setBounds(80, 130, 150, 30);
        label3.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label3.setForeground(new Color(52, 58, 64));
        panel.add(label3);

        JLabel RNo = new JLabel();
        RNo.setBounds(280, 130, 200, 30);
        RNo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        RNo.setForeground(new Color(0, 123, 255));
        panel.add(RNo);

        // In Time
        JLabel label4 = new JLabel("In Time");
        label4.setBounds(80, 190, 150, 30);
        label4.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label4.setForeground(new Color(52, 58, 64));
        panel.add(label4);

        JLabel INTime = new JLabel();
        INTime.setBounds(280, 190, 450, 30);
        INTime.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        INTime.setForeground(new Color(73, 80, 87));
        panel.add(INTime);

        // Out Time
        JLabel label5 = new JLabel("Out Time");
        label5.setBounds(80, 250, 150, 30);
        label5.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label5.setForeground(new Color(52, 58, 64));
        panel.add(label5);

        Date date = new Date();
        JLabel OUTTime = new JLabel("" + date);
        OUTTime.setBounds(280, 250, 450, 30);
        OUTTime.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        OUTTime.setForeground(new Color(73, 80, 87));
        panel.add(OUTTime);

        // Button Panel with better spacing
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(80, 310, 640, 50);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        panel.add(buttonPanel);

        JButton Check = new JButton("🔍 Check");
        Check.setPreferredSize(new Dimension(150, 45));
        Check.setBackground(new Color(0, 123, 255));
        Check.setForeground(Color.WHITE);
        Check.setFont(new Font("Segoe UI", Font.BOLD, 14));
        Check.setFocusPainted(false);
        Check.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        buttonPanel.add(Check);
        
        Check.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                conn c = new conn();
                try {
                    ResultSet resultSet = c.statement.executeQuery("select * from Patient_Info where number = '" + choice.getSelectedItem() + "'");
                    while (resultSet.next()) {
                        RNo.setText(resultSet.getString("Room_Number"));
                        INTime.setText(resultSet.getString("Time"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton discharge = new JButton("✅ Discharge");
        discharge.setPreferredSize(new Dimension(160, 45));
        discharge.setBackground(new Color(40, 167, 69));
        discharge.setForeground(Color.WHITE);
        discharge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        discharge.setFocusPainted(false);
        discharge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 167, 69), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        buttonPanel.add(discharge);
        
        discharge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (RNo.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please check patient details first!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to discharge this patient?", 
                    "Confirm Discharge", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    conn c = new conn();
                    try {
                        c.statement.executeUpdate("delete from Patient_Info where number = '" + choice.getSelectedItem() + "'");
                        c.statement.executeUpdate("update room set Availability = 'Available' where room_no = '" + RNo.getText() + "'");
                        JOptionPane.showMessageDialog(null, 
                            "✅ Patient Discharged Successfully!\nRoom " + RNo.getText() + " is now available.", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        setVisible(false);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, 
                            "Error: " + ex.getMessage(), 
                            "Database Error", 
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });

        JButton Back = new JButton("← Back");
        Back.setPreferredSize(new Dimension(120, 45));
        Back.setBackground(new Color(108, 117, 125));
        Back.setForeground(Color.WHITE);
        Back.setFont(new Font("Segoe UI", Font.BOLD, 14));
        Back.setFocusPainted(false);
        Back.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(108, 117, 125), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        buttonPanel.add(Back);
        
        Back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new patient_discharge();
    }
}
