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

        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(mainFont);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField("designer");
        usernameField.setFont(mainFont);
        usernameField.setPreferredSize(new Dimension(300, 40));
        usernameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(mainFont);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("password");
        passwordField.setFont(mainFont);
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Remember me checkbox
        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(mainFont);
        rememberMe.setBackground(backgroundColor);
        rememberMe.setFocusPainted(false);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(primaryColor);
        loginButton.setForeground(Color.BLACK);
        loginButton.setBorder(new EmptyBorder(10, 0, 10, 0));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));

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

        // Add components to form with spacing
        formPanel.add(usernameLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(rememberMe);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(loginButton);

        // Create footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(backgroundColor);
        JLabel forgotPasswordLabel = new JLabel("Forgot password?");
        forgotPasswordLabel.setFont(mainFont);
        forgotPasswordLabel.setForeground(primaryColor);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerPanel.add(forgotPasswordLabel);

        // Add all panels to the content pane
        contentPane.add(logoPanel, BorderLayout.NORTH);
        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.equals("designer") && password.equals("password")) {
            this.dispose();
            MainAppFrame mainApp = new MainAppFrame();
            mainApp.setVisible(true);
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