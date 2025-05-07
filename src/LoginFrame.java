import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Color primaryColor = new Color(64, 123, 255);
    private Color backgroundColor = new Color(245, 247, 250);
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
    private JButton registerButton;


    public LoginFrame() {
        // Set frame properties
        setTitle("Furniture Designer - Login");
        setSize(450, 500);
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

        // Create logo image (replace with actual icon if available)
        //JLabel logoLabel = new JLabel(new ImageIcon(getClass().getResource("/resources/furniture_logo.png")));
        // If no logo is available, use a text label
        JLabel titleLabel = new JLabel("FurnishView", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(primaryColor);

        logoPanel.add(titleLabel, BorderLayout.CENTER);
        JLabel subtitleLabel = new JLabel("Design your dream space", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        logoPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));

        // Username field with left-aligned label
        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.Y_AXIS));
        usernamePanel.setBackground(backgroundColor);
        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(mainFont);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField("designer");
        usernameField.setFont(mainFont);
        usernameField.setPreferredSize(new Dimension(300, 40));
        usernameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        usernamePanel.add(usernameLabel);
        usernamePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        usernamePanel.add(usernameField);

        // Password field with left-aligned label
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setBackground(backgroundColor);
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(mainFont);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("password");
        passwordField.setFont(mainFont);
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        passwordPanel.add(passwordLabel);
        passwordPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        passwordPanel.add(passwordField);

        // Login button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(primaryColor);
        loginButton.setForeground(Color.BLACK); // Black text as requested
        loginButton.setBorder(new EmptyBorder(10, 0, 10, 0));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));

        buttonPanel.add(loginButton);

        // Add hover effect to button
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(41, 98, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(primaryColor);
            }
        });

        loginButton.addActionListener(e -> handleLogin());

        registerButton = new JButton("Create New Account");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerButton.setForeground(primaryColor);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> {
            this.dispose();
            new RegistrationFrame().setVisible(true);
        });

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(registerButton);

        // Add components to form with spacing
        formPanel.add(usernamePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(buttonPanel);

        // Add all panels to the content pane
        contentPane.add(logoPanel, BorderLayout.NORTH);
        contentPane.add(formPanel, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (UserManager.authenticateUser(username, password)) {
            User user = UserManager.getUser(username);
            this.dispose();

            // Since we're not changing MainAppFrame, we'll just pass the username to ProjectDashboardFrame
            ProjectDashboardFrame dashboardFrame = new ProjectDashboardFrame(username);
            dashboardFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password!",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
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
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}