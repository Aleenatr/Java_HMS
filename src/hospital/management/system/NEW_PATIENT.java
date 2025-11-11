package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.Date;

public class NEW_PATIENT extends JFrame implements ActionListener {

    JComboBox<String> comboBox;
    JTextField textFieldNumber, textName, textFieldDisease, textFieldDeposite;
    JRadioButton r1, r2;
    Choice c1;
    JLabel date;
    JButton b1, b2;

    public NEW_PATIENT() {

        // Load background
        ImageIcon background = new ImageIcon(ClassLoader.getSystemResource("icon/bg.jpeg"));
        Image bgImage = background.getImage().getScaledInstance(850, 550, Image.SCALE_SMOOTH);

        // Background panel with transparent overlay
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(255, 255, 255, 180)); // light overlay for contrast
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);
        add(panel);

        // Title
        JLabel labelName = new JLabel("NEW PATIENT FORM");
        labelName.setBounds(290, 20, 400, 40);
        labelName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        labelName.setForeground(new Color(0, 51, 102));
        panel.add(labelName);

        // ID
        JLabel labelID = new JLabel("ID Type:");
        labelID.setBounds(100, 90, 120, 25);
        labelID.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelID);

        comboBox = new JComboBox<>(new String[]{"Aadhar Card", "Voter ID", "Driving ID"});
        comboBox.setBounds(230, 90, 200, 25);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(comboBox);

        // Number
        JLabel labelNumber = new JLabel("Number:");
        labelNumber.setBounds(100, 130, 120, 25);
        labelNumber.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelNumber);

        textFieldNumber = new JTextField();
        textFieldNumber.setBounds(230, 130, 200, 25);
        textFieldNumber.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(textFieldNumber);

        // Name
        JLabel labelName1 = new JLabel("Name:");
        labelName1.setBounds(100, 170, 120, 25);
        labelName1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelName1);

        textName = new JTextField();
        textName.setBounds(230, 170, 200, 25);
        textName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(textName);

        // Gender
        JLabel labelgender = new JLabel("Gender:");
        labelgender.setBounds(100, 210, 120, 25);
        labelgender.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelgender);

        r1 = new JRadioButton("Male");
        r2 = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(r1);
        genderGroup.add(r2);

        r1.setBounds(230, 210, 80, 25);
        r2.setBounds(320, 210, 100, 25);
        r1.setBackground(new Color(255, 255, 255, 180));
        r2.setBackground(new Color(255, 255, 255, 180));
        r1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        r2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(r1);
        panel.add(r2);

        // Disease
        JLabel labelDisease = new JLabel("Disease:");
        labelDisease.setBounds(100, 250, 120, 25);
        labelDisease.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelDisease);

        textFieldDisease = new JTextField();
        textFieldDisease.setBounds(230, 250, 200, 25);
        textFieldDisease.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(textFieldDisease);

        // Room
        JLabel labelRoom = new JLabel("Room:");
        labelRoom.setBounds(100, 290, 120, 25);
        labelRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelRoom);

        c1 = new Choice();
        try {
            conn c = new conn();
            ResultSet resultSet = c.statement.executeQuery("SELECT * FROM Room WHERE Availability = 'Availabil'");
            while (resultSet.next()) {
                c1.add(resultSet.getString("room_no"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        c1.setBounds(230, 290, 200, 25);
        panel.add(c1);

        // Time
        JLabel labelDate = new JLabel("Time:");
        labelDate.setBounds(100, 330, 120, 25);
        labelDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelDate);

        Date date1 = new Date();
        date = new JLabel(date1.toString());
        date.setBounds(230, 330, 300, 25);
        date.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(date);

        // Deposit
        JLabel labelDeposite = new JLabel("Deposit:");
        labelDeposite.setBounds(100, 370, 120, 25);
        labelDeposite.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(labelDeposite);

        textFieldDeposite = new JTextField();
        textFieldDeposite.setBounds(230, 370, 200, 25);
        textFieldDeposite.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(textFieldDeposite);

        // Buttons
        b1 = new JButton("✅ ADD PATIENT");
        b1.setBounds(200, 430, 160, 40);
        b1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b1.setBackground(new Color(40, 167, 69));
        b1.setForeground(Color.WHITE);
        b1.setFocusPainted(false);
        b1.addActionListener(this);
        panel.add(b1);

        b2 = new JButton("← BACK");
        b2.setBounds(380, 430, 120, 40);
        b2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b2.setBackground(new Color(108, 117, 125));
        b2.setForeground(Color.WHITE);
        b2.setFocusPainted(false);
        b2.addActionListener(this);
        panel.add(b2);

        // Frame setup
        setTitle("New Patient Registration");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b1) {
            String gender = r1.isSelected() ? "Male" : r2.isSelected() ? "Female" : null;

            if (gender == null) {
                JOptionPane.showMessageDialog(this, "Please select gender!");
                return;
            }

            String id = (String) comboBox.getSelectedItem();
            String number = textFieldNumber.getText();
            String name = textName.getText();
            String disease = textFieldDisease.getText();
            String room = c1.getSelectedItem();
            String time = date.getText();
            String deposit = textFieldDeposite.getText();

            try {
                conn c = new conn();
                String q = "INSERT INTO Patient_Info VALUES('" + id + "','" + number + "','" + name + "','" + gender + "','" + disease + "','" + room + "','" + time + "','" + deposit + "')";
                String q1 = "UPDATE Room SET Availability = 'Occupied' WHERE room_no = '" + room + "'";
                c.statement.executeUpdate(q);
                c.statement.executeUpdate(q1);
                JOptionPane.showMessageDialog(null, "Added Successfully");
                setVisible(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == b2) {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new NEW_PATIENT();
    }
}
