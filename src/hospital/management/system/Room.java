package hospital.management.system;

import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Room extends JFrame {

    JTable table;
    JTextField roomField, priceField;
    JComboBox<String> availabilityBox, bedTypeBox;

    Room() {
        setUndecorated(true);
        setSize(900, 600);
        setLocation(300, 230);
        setLayout(new BorderLayout());

        // Root layered pane for proper stacking
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 600));
        add(layeredPane, BorderLayout.CENTER);

        // ===== Background Layer =====
        ImageIcon bgIcon = new ImageIcon(ClassLoader.getSystemResource("icon/bg.jpeg"));
        Image bgImg = bgIcon.getImage().getScaledInstance(900, 600, Image.SCALE_SMOOTH);
        JLabel background = new JLabel(new ImageIcon(bgImg));
        background.setBounds(0, 0, 900, 600);
        layeredPane.add(background, Integer.valueOf(0));

        // ===== Semi-transparent Overlay (optional dimming) =====
        JPanel overlay = new JPanel();
        overlay.setBounds(0, 0, 900, 600);
        overlay.setBackground(new Color(0, 0, 0, 60)); // subtle dark overlay
        overlay.setLayout(null);
        layeredPane.add(overlay, Integer.valueOf(1));

        // ===== Content Panel =====
        JPanel content = new JPanel(null);
        content.setOpaque(false);
        content.setBounds(0, 0, 900, 600);
        layeredPane.add(content, Integer.valueOf(2));

        // Header title
        JLabel title = new JLabel("Room Management", JLabel.CENTER);
        title.setBounds(0, 20, 900, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        content.add(title);

        // Rounded table container
        JPanel tablePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBounds(80, 80, 740, 280);
        tablePanel.setOpaque(false);
        content.add(tablePanel);

        // Table setup
        table = new JTable();
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 150, 136));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        loadRoomData();

        // Input section (like a frosted card)
        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(80, 390, 740, 100);
        formPanel.setBackground(new Color(255, 255, 255, 210));
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136), 1, true));
        content.add(formPanel);

        // Labels & Inputs
        JLabel label1 = new JLabel("Room No:");
        label1.setBounds(40, 20, 80, 25);
        label1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(label1);

        roomField = new JTextField();
        roomField.setBounds(40, 50, 100, 28);
        roomField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(roomField);

        JLabel label2 = new JLabel("Availability:");
        label2.setBounds(170, 20, 100, 25);
        label2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(label2);

        availabilityBox = new JComboBox<>(new String[]{"Available", "Occupied"});
        availabilityBox.setBounds(170, 50, 120, 28);
        availabilityBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(availabilityBox);

        JLabel label3 = new JLabel("Price:");
        label3.setBounds(320, 20, 80, 25);
        label3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(label3);

        priceField = new JTextField();
        priceField.setBounds(320, 50, 100, 28);
        priceField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(priceField);

        JLabel label4 = new JLabel("Bed Type:");
        label4.setBounds(460, 20, 100, 25);
        label4.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(label4);

        bedTypeBox = new JComboBox<>(new String[]{"Single", "Double"});
        bedTypeBox.setBounds(460, 50, 120, 28);
        bedTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(bedTypeBox);

        JButton addRoom = new JButton("➕ Add Room");
        addRoom.setBounds(605, 46, 130, 36);
        addRoom.setBackground(new Color(40, 167, 69));
        addRoom.setForeground(Color.WHITE);
        addRoom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addRoom.setFocusPainted(false);
        formPanel.add(addRoom);

        addRoom.addActionListener(e -> addRoom());

        // Back button
        JButton back = new JButton("← Back");
        back.setBounds(400, 520, 120, 38);
        back.setBackground(new Color(108, 117, 125));
        back.setForeground(Color.WHITE);
        back.setFont(new Font("Segoe UI", Font.BOLD, 13));
        back.setFocusPainted(false);
        content.add(back);
        back.addActionListener(e -> setVisible(false));

        setVisible(true);
    }

    void loadRoomData() {
        try {
            conn c = new conn();
            ResultSet rs = c.statement.executeQuery("SELECT * FROM room");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addRoom() {
        String roomNo = roomField.getText();
        String availability = (String) availabilityBox.getSelectedItem();
        String price = priceField.getText();
        String bedType = (String) bedTypeBox.getSelectedItem();

        if (roomNo.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields");
            return;
        }

        try {
            conn c = new conn();
            String q = "INSERT INTO room VALUES('" + roomNo + "', '" + availability + "', '" + price + "', '" + bedType + "')";
            c.statement.executeUpdate(q);
            JOptionPane.showMessageDialog(null, "Room Added Successfully");
            loadRoomData();
            roomField.setText("");
            priceField.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Room();
    }
}
