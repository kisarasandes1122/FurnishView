import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.io.File;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Color primaryColor = new Color(64, 64, 64); // Changed to dark gray
    private Color backgroundColor = Color.WHITE; // Changed to white
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
    private JButton registerButton;

    public LoginFrame() {
        // Set frame properties
        setTitle("FurnishView - Login");
        setSize(900, 650); // Increased size for two-column layout
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main container with BorderLayout
        JPanel mainContainer = new JPanel(new BorderLayout());
        setContentPane(mainContainer);

        // Create a split layout with login form on left, image on right
        JPanel contentPane = new JPanel(new GridLayout(1, 2));
        mainContainer.add(contentPane, BorderLayout.CENTER);
        
        // Left panel - Login form
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(backgroundColor);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));
        
        // Right panel - Image
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(240, 240, 240));
        
        // Add both panels to content pane
        contentPane.add(leftPanel);
        contentPane.add(rightPanel);
        
        // Create a panel for the logo
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(backgroundColor);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Try to add an icon to the logo - a simple circle with furniture elements
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resources/furnishview_logo.png"));
            // Resize the logo image to make it smaller
            Image img = logoIcon.getImage();
            Image resizedImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logoIcon = new ImageIcon(resizedImg);
            JLabel iconLabel = new JLabel(logoIcon);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoPanel.add(iconLabel);
            logoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        } catch (Exception e) {
            // If logo image not found, create circular icon
            JPanel circlePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw circle outline - reduced size from 60x60 to 40x40
                    g2d.setColor(new Color(100, 80, 60));
                    g2d.drawOval(0, 0, 40, 40);
                    
                    // Draw simple furniture lines - scaled down
                    g2d.drawLine(10, 26, 30, 26); // horizontal line
                    g2d.drawLine(13, 16, 13, 26); // vertical line 1
                    g2d.drawLine(27, 16, 27, 26); // vertical line 2
                    
                    // Draw small lamp - scaled down
                    g2d.drawLine(20, 10, 20, 16);
                    g2d.drawOval(18, 6, 4, 4);
                }
                
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(40, 40); // Reduced from 60x60 to 40x40
                }
            };
            circlePanel.setOpaque(false);
            circlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            logoPanel.add(circlePanel);
            logoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        

        
        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        
        // Create field width for consistency - ensure constant width for ALL form elements
        int fieldWidth = 300;

        // Username field with label
        JPanel usernameLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        usernameLabelPanel.setBackground(backgroundColor);
        usernameLabelPanel.setMaximumSize(new Dimension(fieldWidth, 20));

        JLabel usernameLabel = new JLabel("USERNAME");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usernameLabel.setForeground(Color.DARK_GRAY);
        usernameLabelPanel.add(usernameLabel);

        JPanel usernameFieldPanel = new JPanel();
        usernameFieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        usernameFieldPanel.setBackground(backgroundColor);
        usernameFieldPanel.setPreferredSize(new Dimension(fieldWidth, 40));
        usernameFieldPanel.setMinimumSize(new Dimension(fieldWidth, 40));
        usernameFieldPanel.setMaximumSize(new Dimension(fieldWidth, 40));

        usernameField = new JTextField();
        usernameField.setText("designer");
        usernameField.setFont(mainFont);
        usernameField.setPreferredSize(new Dimension(fieldWidth, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        usernameFieldPanel.add(usernameField);

        // Password field with label
        JPanel passwordLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        passwordLabelPanel.setBackground(backgroundColor);
        passwordLabelPanel.setMaximumSize(new Dimension(fieldWidth, 20));

        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(Color.DARK_GRAY);
        passwordLabelPanel.add(passwordLabel);

        JPanel passwordFieldPanel = new JPanel();
        passwordFieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        passwordFieldPanel.setBackground(backgroundColor);
        passwordFieldPanel.setPreferredSize(new Dimension(fieldWidth, 40));
        passwordFieldPanel.setMinimumSize(new Dimension(fieldWidth, 40));
        passwordFieldPanel.setMaximumSize(new Dimension(fieldWidth, 40));

        passwordField = new JPasswordField();
        passwordField.setText("password");
        passwordField.setFont(mainFont);
        passwordField.setPreferredSize(new Dimension(fieldWidth, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordFieldPanel.add(passwordField);

        // Add components to form panel with proper spacing
        formPanel.add(usernameLabelPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(usernameFieldPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(passwordLabelPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(passwordFieldPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Login button
        JButton loginButton = new JButton("LOG IN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(Color.WHITE); // Changed to white background
        loginButton.setForeground(Color.BLACK); // Changed to black text
        loginButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(12, 0, 12, 0)
        ));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(fieldWidth, 45));

        // Add hover effect to button
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(240, 240, 240)); // Lighter gray on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(Color.WHITE); // Back to white when not hovered
            }
        });

        loginButton.addActionListener(e -> handleLogin());

        // Terms text
        JLabel termsLabel = new JLabel("<html><body style='width: 300px'>By Signing up to FurnishView, means you agree to our Privacy Policy and Terms of Service</body></html>");
        termsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        termsLabel.setForeground(Color.GRAY);
        termsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Registration option
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        registerPanel.setBackground(backgroundColor);
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel alreadyMemberLabel = new JLabel("Are you a new member?");
        alreadyMemberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alreadyMemberLabel.setForeground(Color.GRAY);

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerButton.setForeground(Color.DARK_GRAY);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            this.dispose();
            new RegistrationFrame().setVisible(true);
        });

        registerPanel.add(alreadyMemberLabel);
        registerPanel.add(registerButton);

        formPanel.add(loginButton);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(termsLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        formPanel.add(registerPanel);

        // Add all panels to the left panel
        leftPanel.add(logoPanel);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(formPanel);
        leftPanel.add(Box.createVerticalGlue());
        
        // Add furniture image to right panel
        try {
            ImageIcon furnitureImage = new ImageIcon(getClass().getResource("/resources/furniture_interior.png"));
            // Scale the image to fit panel
            Image scaledImage = furnitureImage.getImage().getScaledInstance(450, 650, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            rightPanel.add(imageLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            // If image not found, create placeholder panel with color matching the screenshot
            rightPanel.setBackground(new Color(240, 230, 210)); // Light wooden color
            JLabel placeholderLabel = new JLabel("Modern Interior Design", JLabel.CENTER);
            placeholderLabel.setFont(new Font("Serif", Font.ITALIC, 24));
            placeholderLabel.setForeground(new Color(120, 100, 80));
            rightPanel.add(placeholderLabel, BorderLayout.CENTER);
        }
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