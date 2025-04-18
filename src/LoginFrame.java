import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Color primaryColor = new Color(64, 123, 255); // A modern blue
    private Color backgroundColor = new Color(245, 247, 250); // Light background
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 28);

    public LoginFrame() {
        // Set frame properties
        setTitle("Furniture Designer - Login");
        setSize(450, 500);
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit app on close
        setResizable(false);

        // Set background color for the frame's content pane
        JPanel contentPane = new JPanel();
        contentPane.setBackground(backgroundColor);
        contentPane.setLayout(new BorderLayout());
        // Add padding around the content
        contentPane.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        setContentPane(contentPane);

        // Create a panel for the logo/title area
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(backgroundColor); // Match background
        logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0)); // Space below title

        // Title Label
        JLabel titleLabel = new JLabel("Furniture Visualizer", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(primaryColor);
        logoPanel.add(titleLabel, BorderLayout.CENTER);

        // Subtitle Label (optional)
        JLabel subtitleLabel = new JLabel("Design your dream space", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120)); // Grey subtitle
        logoPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Create the form panel using BoxLayout for vertical stacking
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0)); // Padding

        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(mainFont);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align label left

        usernameField = new JTextField("designer"); // Default value for convenience
        usernameField.setFont(mainFont);
        usernameField.setPreferredSize(new Dimension(300, 40)); // Set preferred size
        usernameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40)); // Allow stretching horizontally
        // Style the text field border
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true), // Light grey rounded border
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Internal padding
        ));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(mainFont);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("password"); // Default value for convenience
        passwordField.setFont(mainFont);
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        // Add action listener to password field for Enter key
        passwordField.addActionListener(e -> handleLogin());


        // Remember me checkbox (optional)
        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(mainFont);
        rememberMe.setBackground(backgroundColor);
        rememberMe.setFocusPainted(false); // Remove focus border
        rememberMe.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(primaryColor);
        loginButton.setForeground(Color.WHITE); // White text on button
        loginButton.setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding inside button
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 45)); // Allow stretching

        // Add hover effect to button
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(primaryColor.darker()); // Darken on hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(primaryColor); // Restore original color
            }
        });

        // Add action listener for button click
        loginButton.addActionListener(e -> handleLogin());

        // Add components to the form panel with spacing
        formPanel.add(usernameLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Vertical space
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(rememberMe);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(loginButton);

        // Create footer panel (optional: for forgot password link)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(backgroundColor);
        JLabel forgotPasswordLabel = new JLabel("Forgot password?");
        forgotPasswordLabel.setFont(mainFont);
        forgotPasswordLabel.setForeground(primaryColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Add action listener for forgot password if needed
        // forgotPasswordLabel.addMouseListener(...)
        footerPanel.add(forgotPasswordLabel);

        // Add all panels to the content pane
        contentPane.add(logoPanel, BorderLayout.NORTH);
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    // Action handler for login attempt
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Simple hardcoded login check
        if (username.equals("designer") && password.equals("password")) {
            // Close the login frame
            this.dispose();
            // Open the main application frame
            MainAppFrame mainApp = new MainAppFrame();
            mainApp.setVisible(true);
        } else {
            // Show error message
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password!",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Main method for testing LoginFrame independently
    public static void main(String[] args) {
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