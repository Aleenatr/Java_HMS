package hospital.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.ResultSet;

public class Login extends JFrame implements ActionListener {
    JTextField textField;
    JPasswordField jpasswordField;
    JButton b1, b2;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == b1) {
            try {
                conn c = new conn();
                String user = textField.getText();
                String pass = new String(jpasswordField.getPassword());

                String q = "SELECT * FROM login WHERE ID='" + user + "' AND PW='" + pass + "'";
                ResultSet resultSet = c.statement.executeQuery(q);
                if (resultSet.next()) {
                    new Reception();
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Username or Password");
                }
            } catch (Exception E) {
                E.printStackTrace();
            }
        } else {
            System.exit(0);
        }
    }

    public Login() {
        setTitle("Hospital Management Login");
        setSize(750, 300);
        setLocation(400, 270);
        setLayout(null);
        getContentPane().setBackground(new Color(109, 164, 170));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel nameLabel = new JLabel("Username");
        nameLabel.setBounds(40, 20, 100, 30);
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        add(nameLabel);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(40, 70, 100, 30);
        passwordLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        add(passwordLabel);

        textField = new JTextField();
        textField.setBounds(150, 20, 200, 30);
        add(textField);

        jpasswordField = new JPasswordField();
        jpasswordField.setBounds(150, 70, 200, 30);
        add(jpasswordField);

        b1 = new JButton("Login");
        b1.setBounds(40, 140, 120, 30);
        b1.addActionListener(this);
        add(b1);

        b2 = new JButton("Cancel");
        b2.setBounds(180, 140, 120, 30);
        b2.addActionListener(this);
        add(b2);

        // ✅ Proper Unsplash image loading
        try {
            URL url = new URL("https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=800");
            ImageIcon imageIcon = new ImageIcon(url);
            Image img = imageIcon.getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            imageLabel.setBounds(350, -30, 400, 300);
            add(imageLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}
