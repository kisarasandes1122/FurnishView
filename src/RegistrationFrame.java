import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private Color primaryColor = new Color(166, 132, 94); // More wood-toned color
    private Color backgroundColor = Color.WHITE;
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font labelFont = new Font("Segoe UI", Font.PLAIN, 12);

    public RegistrationFrame() {
        // Set frame properties
        setTitle("FurnishView - Registration");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create split panel for left and right sides
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        setContentPane(splitPane);

        // Right panel with image
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(backgroundColor);
        
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

        // Left panel with registration form
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(backgroundColor);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Create logo panel
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(backgroundColor);

        // Add logo
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
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        // Set a fixed width for all form elements
        final int FORM_ELEMENT_WIDTH = 300;

        // Username field
        JPanel usernamePanel = createFormField("USERNAME", usernameField = new JTextField());
        usernameField.setPreferredSize(new Dimension(FORM_ELEMENT_WIDTH, 40));
        usernameField.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 40));
        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernamePanel.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 70));

        // Password field with visibility toggle
        JPanel passwordPanel = createPasswordField("PASSWORD", passwordField = new JPasswordField());
        passwordField.setPreferredSize(new Dimension(FORM_ELEMENT_WIDTH, 40));
        passwordField.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 40));
        passwordField.setEchoChar('•');
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordPanel.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 70));

        // Confirm Password field with visibility toggle
        JPanel confirmPasswordPanel = createPasswordField("CONFIRM PASSWORD", confirmPasswordField = new JPasswordField());
        confirmPasswordField.setPreferredSize(new Dimension(FORM_ELEMENT_WIDTH, 40));
        confirmPasswordField.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 40));
        confirmPasswordField.setEchoChar('•');
        confirmPasswordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmPasswordPanel.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 70));

        // Register button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setPreferredSize(new Dimension(FORM_ELEMENT_WIDTH, 45));
        buttonPanel.setMaximumSize(new Dimension(FORM_ELEMENT_WIDTH, 45));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = new JButton("CREATE ACCOUNT");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(Color.WHITE);
        registerButton.setForeground(Color.BLACK);
        registerButton.setBorder(new EmptyBorder(10, 0, 10, 0));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(registerButton, BorderLayout.CENTER);

        // Add hover effect to button
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(Color.DARK_GRAY);            }

        });

        registerButton.addActionListener(e -> handleRegistration());

        // Terms and privacy policy text
        JPanel termsPanel = new JPanel();
        termsPanel.setBackground(backgroundColor);
        termsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel termsText = new JLabel("<html><center>By Signing up to FurnishView, means you agree to our Privacy<br>Policy and Terms of Service</center></html>");
        termsText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        termsText.setForeground(Color.GRAY);
        termsPanel.add(termsText);

        // Already a member text with login link
        JPanel loginLinkPanel = new JPanel();
        loginLinkPanel.setBackground(backgroundColor);
        loginLinkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel alreadyMemberLabel = new JLabel("Already a Member?");
        alreadyMemberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alreadyMemberLabel.setForeground(Color.DARK_GRAY);
        
        JButton loginLink = new JButton("LOG IN");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginLink.setForeground(Color.BLACK);
        loginLink.setBorderPainted(false);
        loginLink.setContentAreaFilled(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setFocusPainted(false);
        
        loginLink.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        
        loginLinkPanel.add(alreadyMemberLabel);
        loginLinkPanel.add(loginLink);

        // Add components to form with spacing
        formPanel.add(usernamePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(confirmPasswordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        formPanel.add(buttonPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(termsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(loginLinkPanel);

        // Add all panels to the left panel
        leftPanel.add(logoPanel, BorderLayout.NORTH);
        leftPanel.add(formPanel, BorderLayout.CENTER);

        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(450);
    }

    private JPanel createFormField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(Color.DARK_GRAY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0)); // Add slight padding to align with field border
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(mainFont);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);

        return panel;
    }
    
    private JPanel createPasswordField(String labelText, JPasswordField field) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(backgroundColor);
        
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(Color.DARK_GRAY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.setFont(mainFont);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        mainPanel.add(label);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(field);
        
        return mainPanel;
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
        SwingUtilities.invokeLater(() -> {
            RegistrationFrame frame = new RegistrationFrame();
            frame.setVisible(true);
        });
    }
}