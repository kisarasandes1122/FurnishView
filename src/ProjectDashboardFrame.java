import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
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
    private List<ProjectInfo> projects;
    private Color primaryColor = new Color(64, 123, 255);
    private Color backgroundColor = new Color(245, 247, 250);
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 16);
    
    // Project metadata class
    private static class ProjectInfo {
        public String filename;
        public String name;
        public Date lastModified;
        public String roomType;
        public int itemCount;
        
        public ProjectInfo(String filename, String name, Date lastModified, String roomType, int itemCount) {
            this.filename = filename;
            this.name = name;
            this.lastModified = lastModified;
            this.roomType = roomType;
            this.itemCount = itemCount;
        }
        
        public Object[] toTableRow() {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            return new Object[]{
                name,
                roomType,
                itemCount,
                sdf.format(lastModified)
            };
        }
    }

    public ProjectDashboardFrame(String username) {
        currentUser = username;
        
        // Set frame properties
        setTitle("FurnishView - Project Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        // Initialize the UI
        initializeUI();
        
        // Load saved projects data
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
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));
        
        // Left: Welcome message
        welcomeLabel = new JLabel("Welcome, " + currentUser + "!");
        welcomeLabel.setFont(titleFont);
        welcomeLabel.setForeground(primaryColor);
        panel.add(welcomeLabel, BorderLayout.WEST);
        
        // Right: User actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(backgroundColor);
        
        logoutButton = new JButton("Logout");
        logoutButton.setFont(mainFont);
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
        panel.add(headerLabel, BorderLayout.NORTH);
        
        // Table model and setup
        String[] columns = {"Project Name", "Room Type", "Furniture Items", "Last Modified"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        projectsTable = new JTable(tableModel);
        projectsTable.setRowHeight(25);
        projectsTable.setFont(mainFont);
        projectsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Statistics label
        statsLabel = new JLabel("Total Projects: 0");
        statsLabel.setFont(mainFont);
        panel.add(statsLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 0, 0)
        ));
        
        // Panel title
        JLabel actionsLabel = new JLabel("Actions");
        actionsLabel.setFont(headerFont);
        actionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(actionsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Create new project button
        createNewButton = createActionButton("Create New Project", e -> createNewProject());
        panel.add(createNewButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Open selected project button
        openSelectedButton = createActionButton("Open Selected", e -> openSelectedProject());
        panel.add(openSelectedButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Duplicate selected button
        duplicateButton = createActionButton("Duplicate Selected", e -> duplicateSelectedProject());
        panel.add(duplicateButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Delete selected button
        deleteSelectedButton = createActionButton("Delete Selected", e -> deleteSelectedProject());
        panel.add(deleteSelectedButton);
        
        return panel;
    }
    
    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(mainFont);
        button.setMaximumSize(new Dimension(200, 35));
        button.setPreferredSize(new Dimension(180, 35));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(listener);
        return button;
    }
    
    private void loadProjects() {
        // Clear existing data
        tableModel.setRowCount(0);
        projects = new ArrayList<>();
        
        // Create designs directory if it doesn't exist
        File designsDir = new File("./designs");
        if (!designsDir.exists()) {
            designsDir.mkdirs();
        }
        
        // Scan directory for .furn files
        File[] files = designsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".furn"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    // Extract basic metadata from file
                    String filename = file.getName();
                    String name = filename.substring(0, filename.lastIndexOf('.'));
                    Date lastModified = new Date(file.lastModified());
                    
                    // Try to extract more metadata from the actual file content
                    String roomType = "Unknown";
                    int itemCount = 0;
                    
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        DesignModel model = (DesignModel) ois.readObject();
                        if (model != null) {
                            if (model.getRoom() != null) {
                                roomType = model.getRoom().getShape().toString();
                            }
                            if (model.getFurnitureList() != null) {
                                itemCount = model.getFurnitureList().size();
                            }
                        }
                    } catch (Exception e) {
                        // If we can't read the file, just use the defaults
                        System.err.println("Error reading design file metadata: " + e.getMessage());
                    }
                    
                    // Create project info and add to list and table
                    ProjectInfo project = new ProjectInfo(file.getAbsolutePath(), name, lastModified, roomType, itemCount);
                    projects.add(project);
                    tableModel.addRow(project.toTableRow());
                } catch (Exception e) {
                    System.err.println("Error processing design file: " + e.getMessage());
                }
            }
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
        
        // Open the main app with the new model
        this.dispose();
        MainAppFrame mainApp = new MainAppFrame();
        mainApp.setVisible(true);
    }
    
    private void openSelectedProject() {
        int selectedRow = projectsTable.getSelectedRow();
        if (selectedRow != -1) {
            // Convert to model index if table is sorted
            int modelRow = projectsTable.convertRowIndexToModel(selectedRow);
            
            if (modelRow >= 0 && modelRow < projects.size()) {
                ProjectInfo selected = projects.get(modelRow);
                try {
                    File file = new File(selected.filename);
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        DesignModel loadedModel = (DesignModel) ois.readObject();
                        if (loadedModel != null) {
                            // Open the main app with the loaded model
                            this.dispose();
                            MainAppFrame mainApp = new MainAppFrame();
                            // Need to set the loaded model - assuming a method exists
                            // mainApp.setDesignModel(loadedModel);
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
                ProjectInfo selected = projects.get(modelRow);
                try {
                    // Load the model
                    File sourceFile = new File(selected.filename);
                    DesignModel model;
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sourceFile))) {
                        model = (DesignModel) ois.readObject();
                    }
                    
                    if (model != null) {
                        // Get new name
                        String newName = JOptionPane.showInputDialog(this,
                                "Enter name for duplicate project:",
                                selected.name + " (Copy)");
                        
                        if (newName != null && !newName.trim().isEmpty()) {
                            // Create new file
                            String newFilename = "./designs/" + newName + ".furn";
                            if (!newFilename.toLowerCase().endsWith(".furn")) {
                                newFilename += ".furn";
                            }
                            
                            File newFile = new File(newFilename);
                            if (newFile.exists()) {
                                int overwrite = JOptionPane.showConfirmDialog(this,
                                        "File already exists. Overwrite?",
                                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                                if (overwrite != JOptionPane.YES_OPTION) {
                                    return;
                                }
                            }
                            
                            // Save the model to the new file
                            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(newFile))) {
                                oos.writeObject(model);
                                JOptionPane.showMessageDialog(this,
                                        "Project duplicated successfully",
                                        "Duplicate Successful", JOptionPane.INFORMATION_MESSAGE);
                                
                                // Reload projects
                                loadProjects();
                            }
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
                ProjectInfo selected = projects.get(modelRow);
                
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete the project \"" + selected.name + "\"?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        File file = new File(selected.filename);
                        if (file.delete()) {
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
}
