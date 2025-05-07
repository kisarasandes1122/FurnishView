import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import javax.swing.border.*;
import java.io.File;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Color primaryColor = new Color(64, 64, 64); // Dark gray
    private Color backgroundColor = Color.WHITE;
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
    private Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
    private Color buttonColor = new Color(41, 45, 50); // Dark slate color for buttons

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

        // Custom painted flat button with dark background, no border
        JButton loginButton = new JButton("LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark button style with no border (like image provided)
                g2d.setColor(buttonColor); // Dark slate color
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw text centered
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(Color.WHITE); // White text
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }

            // Override these methods to prevent default button styling
            @Override
            protected void paintBorder(Graphics g) {
                // Don't paint any border
            }

            @Override
            public boolean isFocusPainted() {
                return false;
            }

            @Override
            public boolean isContentAreaFilled() {
                return false;
            }
        };

        // Button styling
        loginButton.setFont(buttonFont);
        loginButton.setForeground(Color.WHITE); // Set text color to white
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Set button height
        int buttonHeight = 55; // Increased height
        loginButton.setMaximumSize(new Dimension(fieldWidth, buttonHeight));
        loginButton.setPreferredSize(new Dimension(fieldWidth, buttonHeight));

        // Add action listener for login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Terms and conditions text
        JPanel termsPanel = new JPanel();
        termsPanel.setBackground(backgroundColor);
        termsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        termsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel termsText = new JLabel("<html><div style='text-align: center;'>By Signing up to FurnishView, means you agree to our Privacy<br>Policy and Terms of Service</div></html>");
        termsText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        termsText.setForeground(Color.GRAY);
        termsPanel.add(termsText);

        // Registration option
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerPanel.setBackground(backgroundColor);
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel alreadyMemberLabel = new JLabel("Already a Member?");
        alreadyMemberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alreadyMemberLabel.setForeground(Color.GRAY);

        // Using a JLabel with HTML for the LOGIN text with custom styling
        JLabel loginLink = new JLabel("Register");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginLink.setForeground(new Color(33, 33, 33));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new RegistrationFrame().setVisible(true);
            }
        });

        registerPanel.add(alreadyMemberLabel);
        registerPanel.add(loginLink);

        formPanel.add(loginButton);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(termsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
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

        // Add key listener to password field to handle Enter key press
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        // Add key listener to username field to handle Enter key press
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Debug print to check values
        System.out.println("Attempting login with username: " + username);

        try {
            if (UserManager.authenticateUser(username, password)) {
                User user = UserManager.getUser(username);
                this.dispose();

                // Open the dashboard frame
                SwingUtilities.invokeLater(() -> {
                    ProjectDashboardFrame dashboardFrame = new ProjectDashboardFrame(username);
                    dashboardFrame.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Login error: " + e.getMessage(),
                    "Login Error",
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