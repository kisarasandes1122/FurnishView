import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private Color primaryColor = new Color(64, 123, 255);
    private Color backgroundColor = new Color(245, 247, 250);
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 28);

    public RegistrationFrame() {
        // Set frame properties
        setTitle("Furniture Designer - Registration");
        setSize(450, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Set background color for the frame
        JPanel contentPane = new JPanel();
        contentPane.setBackground(backgroundColor);
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        setContentPane(contentPane);

        // Create a panel for the logo
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("FurnishView", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(primaryColor);

        logoPanel.add(titleLabel, BorderLayout.CENTER);
        JLabel subtitleLabel = new JLabel("Create a new account", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        logoPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));

        // Username field with left-aligned label
        JPanel usernamePanel = createFormField("Username", usernameField = new JTextField());
        usernameField.setPreferredSize(new Dimension(300, 40));
        usernameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        // Password field with left-aligned label
        JPanel passwordPanel = createFormField("Password", passwordField = new JPasswordField());
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        // Confirm Password field
        JPanel confirmPasswordPanel = createFormField("Confirm Password", confirmPasswordField = new JPasswordField());
        confirmPasswordField.setPreferredSize(new Dimension(300, 40));
        confirmPasswordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        // Register button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = new JButton("Create Account");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(primaryColor);
        registerButton.setForeground(Color.BLACK);
        registerButton.setBorder(new EmptyBorder(10, 0, 10, 0));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));

        buttonPanel.add(registerButton);

        // Add hover effect to button
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(41, 98, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(primaryColor);
            }
        });

        registerButton.addActionListener(e -> handleRegistration());

        JButton backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backToLoginButton.setForeground(primaryColor);
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLoginButton.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(backToLoginButton);

        // Add components to form with spacing
        formPanel.add(usernamePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(confirmPasswordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(buttonPanel);

        // Add all panels to the content pane
        contentPane.add(logoPanel, BorderLayout.NORTH);
        contentPane.add(formPanel, BorderLayout.CENTER);
    }

    private JPanel createFormField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(mainFont);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(mainFont);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);

        return panel;
    }

    private void handleRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required!",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match!",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if username already exists
        if (UserManager.getUser(username) != null) {
            JOptionPane.showMessageDialog(this,
                    "Username already exists!",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create new user (always as CUSTOMER)
        User newUser = new User(username, password, User.UserType.CUSTOMER);
        if (UserManager.addUser(newUser)) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now log in.",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            // Navigate to login screen
            this.dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error creating account. Please try again.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            RegistrationFrame frame = new RegistrationFrame();
            frame.setVisible(true);
        });
    }
}