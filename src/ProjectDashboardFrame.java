import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Dashboard screen that appears after login and before the main design interface.
 * Shows previously saved designs and options to create new ones.
 */
public class ProjectDashboardFrame extends JFrame {

    // UI Components
    private JPanel contentPane;
    private JPanel recentProjectsPanel;
    private JPanel actionsPanel;
    private JPanel userInfoPanel;
    private JTable projectsTable;
    private DefaultTableModel tableModel;
    private JButton createNewButton;
    private JButton openSelectedButton;
    private JButton deleteSelectedButton;
    private JButton duplicateButton;
    private JButton logoutButton;
    private JLabel welcomeLabel;
    private JLabel statsLabel;

    // State
    private String currentUser;
    private boolean isAdmin = false;
    private List<ProjectManager.ProjectMetadata> projects;

    // Updated color palette with natural, desaturated tones
    private Color textColor = new Color(68, 68, 68);      // Dark gray instead of black
    private Color backgroundColor = new Color(245, 245, 245); // Soft warm white
    private Color accentColor = new Color(213, 204, 189); // Light taupe/beige
    private Color subtleGray = new Color(240, 240, 238);  // Very light gray for dividers
    private Color headerBgColor = new Color(245, 244, 240); // Barely-there background for header

    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 16);
    private Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

    public ProjectDashboardFrame(String username) {
        currentUser = username;

        // Check if user is admin (if UserManager exists)
        try {
            isAdmin = UserManager.isAdmin(username);
        } catch (Exception e) {
            // If UserManager isn't available, just continue
            System.err.println("Warning: Could not check admin status: " + e.getMessage());
        }

        // Set frame properties
        setTitle("FurnishView - Project Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // Initialize the UI
        initializeUI();

        // Load saved projects data for the current user
        loadProjects();
    }

    private void initializeUI() {
        // Main content pane with border layout
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(backgroundColor);
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        // Header panel with user info
        userInfoPanel = createUserInfoPanel();
        contentPane.add(userInfoPanel, BorderLayout.NORTH);

        // Main center panel for projects table
        recentProjectsPanel = createRecentProjectsPanel();
        contentPane.add(recentProjectsPanel, BorderLayout.CENTER);

        // Side panel with actions
        actionsPanel = createActionsPanel();
        contentPane.add(actionsPanel, BorderLayout.EAST);
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, subtleGray),
                BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));

        // Left: Welcome message with admin indicator if applicable
        String welcomeMessage = isAdmin ?
                "Welcome, " + currentUser + " (Administrator)!" :
                "Welcome, " + currentUser + "!";

        welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(titleFont);
        welcomeLabel.setForeground(textColor);  // Changed from primaryColor to textColor
        panel.add(welcomeLabel, BorderLayout.WEST);

        // Right: User actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(backgroundColor);

        // Custom flat dark logout button
        logoutButton = createCustomButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());

        rightPanel.add(logoutButton);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createRecentProjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

        // Projects table header
        JLabel headerLabel = new JLabel("Your Projects");
        headerLabel.setFont(headerFont);
        headerLabel.setForeground(textColor);
        panel.add(headerLabel, BorderLayout.NORTH);

        // Table model and setup
        String[] columns = {"Project Name", "Room Type", "Furniture Items", "Last Modified", "Owner"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        projectsTable = new JTable(tableModel);
        projectsTable.setRowHeight(25);
        projectsTable.setFont(mainFont);
        projectsTable.setForeground(textColor);
        projectsTable.setBackground(backgroundColor);
        projectsTable.setGridColor(subtleGray);
        projectsTable.setShowGrid(false);  // Remove grid lines
        projectsTable.setIntercellSpacing(new Dimension(10, 5));  // Use spacing instead of grid
        projectsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        projectsTable.getTableHeader().setForeground(textColor);
        projectsTable.getTableHeader().setBackground(headerBgColor);
        projectsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, subtleGray));
        projectsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectsTable.setAutoCreateRowSorter(true);

        // Enable double-click to open
        projectsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedProject();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(projectsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(subtleGray, 1));
        scrollPane.getViewport().setBackground(backgroundColor);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Statistics label
        statsLabel = new JLabel("Total Projects: 0");
        statsLabel.setFont(mainFont);
        statsLabel.setForeground(textColor);
        panel.add(statsLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, subtleGray),
                BorderFactory.createEmptyBorder(10, 15, 0, 0)
        ));

        // Panel title
        JLabel actionsLabel = new JLabel("Actions");
        actionsLabel.setFont(headerFont);
        actionsLabel.setForeground(textColor);
        actionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(actionsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Create new project button - with dark styling
        createNewButton = createCustomButton("Create New Project");
        createNewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        createNewButton.addActionListener(e -> createNewProject());
        panel.add(createNewButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Secondary actions with the same dark style
        openSelectedButton = createCustomButton("Open Selected");
        openSelectedButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        openSelectedButton.addActionListener(e -> openSelectedProject());
        panel.add(openSelectedButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Duplicate selected button
        duplicateButton = createCustomButton("Duplicate Selected");
        duplicateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        duplicateButton.addActionListener(e -> duplicateSelectedProject());
        panel.add(duplicateButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Delete selected button
        deleteSelectedButton = createCustomButton("Delete Selected");
        deleteSelectedButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteSelectedButton.addActionListener(e -> deleteSelectedProject());
        panel.add(deleteSelectedButton);

        // Add admin-specific buttons if user is admin
        if (isAdmin) {
            panel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Add a separator for admin actions
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            separator.setForeground(subtleGray);
            panel.add(separator);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            JLabel adminLabel = new JLabel("Admin Actions");
            adminLabel.setFont(headerFont);
            adminLabel.setForeground(textColor);
            adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(adminLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Create template button
            JButton createTemplateButton = createCustomButton("Create Template");
            createTemplateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            createTemplateButton.addActionListener(e -> createTemplate());
            panel.add(createTemplateButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Manage inventory button
            JButton manageInventoryButton = createCustomButton("Manage Inventory");
            manageInventoryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            manageInventoryButton.addActionListener(e -> manageInventory());
            panel.add(manageInventoryButton);
        }

        return panel;
    }

    // Custom button creation method with flat dark style
    private JButton createCustomButton(String text) {
        boolean isLogoutButton = text.equals("Logout");
        final Color buttonBgColor = isLogoutButton ? new Color(220, 53, 69) : new Color(102, 102, 102); // Red for logout, beige for others
        final Color buttonTextColor = Color.WHITE ;

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Set button background color based on button type
                g2d.setColor(buttonBgColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                Font buttonTextFont = new Font("Inter", Font.BOLD, 14);
                g2d.setFont(buttonTextFont);

                // Draw text centered
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(buttonTextColor); // Text color based on button type
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

        Font buttonFont = new Font("Inter", Font.BOLD, 14);

        // Button styling
        button.setFont(buttonFont);
        button.setForeground(buttonTextColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set consistent size
        button.setMaximumSize(new Dimension(200, 45));
        button.setPreferredSize(new Dimension(180, 45));

        return button;
    }

    private void loadProjects() {
        // Clear existing data
        tableModel.setRowCount(0);
        projects = new ArrayList<>();

        // Get projects for the current user
        List<ProjectManager.ProjectMetadata> userProjects = ProjectManager.getProjectsForUser(currentUser);
        projects.addAll(userProjects);

        // Add to table model
        for (ProjectManager.ProjectMetadata project : projects) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            Object[] rowData = {
                    project.projectName,
                    project.roomType,
                    project.itemCount,
                    sdf.format(project.lastModifiedDate),
                    project.createdBy
            };
            tableModel.addRow(rowData);
        }

        // Update statistics
        statsLabel.setText("Total Projects: " + projects.size());

        // Disable buttons if no projects
        boolean hasProjects = !projects.isEmpty();
        openSelectedButton.setEnabled(hasProjects);
        deleteSelectedButton.setEnabled(hasProjects);
        duplicateButton.setEnabled(hasProjects);
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }

    private void createNewProject() {
        // Create a blank design model
        DesignModel newModel = new DesignModel();

        // Set the creator to the current user
        newModel.setCreatedBy(currentUser);

        // Open the main app with the new model
        this.dispose();
        MainAppFrame mainApp = new MainAppFrame(newModel, null, null, currentUser);
        mainApp.setVisible(true);
    }

    private void openSelectedProject() {
        int selectedRow = projectsTable.getSelectedRow();
        if (selectedRow != -1) {
            // Convert to model index if table is sorted
            int modelRow = projectsTable.convertRowIndexToModel(selectedRow);

            if (modelRow >= 0 && modelRow < projects.size()) {
                ProjectManager.ProjectMetadata selected = projects.get(modelRow);

                // Check if the current user is the owner of the project or is an admin
                String owner = selected.createdBy;
                if (!currentUser.equals(owner) && !isAdmin) {
                    JOptionPane.showMessageDialog(this,
                            "You do not have permission to open this project.",
                            "Permission Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    File file = new File(selected.filename);
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        DesignModel loadedModel = (DesignModel) ois.readObject();
                        if (loadedModel != null) {
                            // Open the main app with the loaded model
                            this.dispose();
                            MainAppFrame mainApp = new MainAppFrame(loadedModel, file, selected.projectName, currentUser);
                            mainApp.setVisible(true);
                            return;
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error loading design file: " + e.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a project to open",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void duplicateSelectedProject() {
        int selectedRow = projectsTable.getSelectedRow();
        if (selectedRow != -1) {
            // Convert to model index if table is sorted
            int modelRow = projectsTable.convertRowIndexToModel(selectedRow);

            if (modelRow >= 0 && modelRow < projects.size()) {
                ProjectManager.ProjectMetadata selected = projects.get(modelRow);

                // Check if the current user is the owner of the project or is an admin
                String owner = selected.createdBy;
                if (!currentUser.equals(owner) && !isAdmin) {
                    JOptionPane.showMessageDialog(this,
                            "You do not have permission to duplicate this project.",
                            "Permission Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // Get new name
                    String newName = JOptionPane.showInputDialog(this,
                            "Enter name for duplicate project:",
                            selected.projectName + " (Copy)");

                    if (newName != null && !newName.trim().isEmpty()) {
                        // Use ProjectManager to duplicate
                        ProjectManager.ProjectMetadata newMetadata =
                                ProjectManager.duplicateProject(selected, newName, currentUser);

                        if (newMetadata != null) {
                            JOptionPane.showMessageDialog(this,
                                    "Project duplicated successfully",
                                    "Duplicate Successful", JOptionPane.INFORMATION_MESSAGE);

                            // Reload projects
                            loadProjects();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to duplicate project",
                                    "Duplication Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error duplicating project: " + e.getMessage(),
                            "Duplication Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a project to duplicate",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedProject() {
        int selectedRow = projectsTable.getSelectedRow();
        if (selectedRow != -1) {
            // Convert to model index if table is sorted
            int modelRow = projectsTable.convertRowIndexToModel(selectedRow);

            if (modelRow >= 0 && modelRow < projects.size()) {
                ProjectManager.ProjectMetadata selected = projects.get(modelRow);

                // Check if the current user is the owner of the project or is an admin
                String owner = selected.createdBy;
                if (!currentUser.equals(owner) && !isAdmin) {
                    JOptionPane.showMessageDialog(this,
                            "You do not have permission to delete this project.",
                            "Permission Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete the project \"" + selected.projectName + "\"?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (ProjectManager.deleteProject(selected)) {
                            JOptionPane.showMessageDialog(this,
                                    "Project deleted successfully",
                                    "Delete Successful", JOptionPane.INFORMATION_MESSAGE);

                            // Reload projects
                            loadProjects();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to delete project file",
                                    "Delete Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this,
                                "Error deleting project: " + e.getMessage(),
                                "Delete Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a project to delete",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Admin-specific methods

    private void createTemplate() {
        // This method will be implemented for Design Templates feature
        JOptionPane.showMessageDialog(this,
                "Template creation functionality will be implemented soon.",
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }

    private void manageInventory() {
        // This method will be implemented for Inventory Management feature
        JOptionPane.showMessageDialog(this,
                "Inventory management functionality will be implemented soon.",
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to set current user (can be used from outside)
    public void setCurrentUser(User user) {
        if (user != null) {
            this.currentUser = user.getUsername();
            this.isAdmin = user.getUserType() == User.UserType.ADMIN;

            // Update welcome label if it exists
            if (welcomeLabel != null) {
                String welcomeMessage = isAdmin ?
                        "Welcome, " + currentUser + " (Administrator)!" :
                        "Welcome, " + currentUser + "!";
                welcomeLabel.setText(welcomeMessage);
                welcomeLabel.setForeground(textColor);  // Ensure text color is consistent
            }

            // Reload projects for this user
            loadProjects();
        }
    }
}