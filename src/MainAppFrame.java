import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.awt.event.*;
import java.io.*; // For potential Save/Load later
import java.util.Set; // For keyboard state
import java.util.HashSet; // For keyboard state

// Assume other classes like Room, DesignModel, Furniture, DesignRenderer etc. exist

public class MainAppFrame extends JFrame {

    // --- UI Components ---
    private JPanel controlPanel;
    private GLJPanel designCanvas;
    private JComboBox<String> viewModeComboBox;
    private JButton deleteFurnitureButton;
    private JButton wallColorButton, floorColorButton, furnitureColorButton, furnitureTextureButton;
    private JButton updateFurnDimsButton, updateFurnRotationButton; // Declare buttons
    private JButton updateRoomSizeButton; // Declare button
    private JTextField roomWidthField, roomLengthField; // Rectangular
    private JTextField roomRadiusField; // Circular
    private JTextField lOuterWidthField, lOuterLengthField, lInsetWidthField, lInsetLengthField; // L-Shape
    private JTextField tBarWidthField, tBarLengthField, tStemWidthField, tStemLengthField; // T-Shape
    private JTextField sharedHeightField; // Common height field
    private JTextField furnWidthField, furnDepthField, furnHeightField, furnRotationYField;
    private JList<String> furnitureLibraryList;
    private JCheckBoxMenuItem showGridMenuItem;
    private JComboBox<Room.RoomShape> roomShapeComboBox;
    private JPanel roomParameterCardsPanel;
    private CardLayout roomParameterCardLayout;
    private JPanel furnitureDimsPanel; // Panel containing furniture dims/rot fields+buttons
    private JPanel furnitureAppearancePanel; // Panel containing furniture color/texture buttons


    // Constants for CardLayout panels
    private static final String RECT_CARD = "Rectangular";
    private static final String CIRC_CARD = "Circular";
    private static final String L_CARD = "L-Shaped";
    private static final String T_CARD = "T-Shaped";

    // --- Core Components ---
    private FPSAnimator animator;
    private DesignRenderer renderer;
    private DesignModel designModel;
    private PickingHelper pickingHelper; // Add PickingHelper instance

    // private UndoManager undoManager; // Add later
    // private Action undoAction, redoAction; // Add later

    // --- Interaction State ---
    private Point lastMousePoint;           // Last known mouse position for dragging
    private boolean isDraggingCamera = false; // Flag for camera dragging (rotate/pan)
    private boolean isDraggingFurniture = false;// Flag for furniture dragging
    private Furniture draggedFurniture = null; // Reference to the furniture being dragged
    private Vector3f dragOffset = null;       // Offset from furniture origin to click point during drag
    private boolean isMovingWithKeyboard = false; // Flag for keyboard movement sequence
    private Set<Integer> pressedKeys = new HashSet<>(); // Track currently pressed keys for smooth movement

    // --- Furniture Library Data ---
    // (Keep this data as defined in the prompt)
    private static final String[] FURNITURE_TYPES = {
            "Chair", "Sofa", "Dining Table", "Side Table", "Bed", "Bookshelf",
            "Armchair", "Dining Chair", "Office Chair", "Stool", "Bench", "Recliner", "Ottoman",
            "Coffee Table", "Desk", "Console Table", "End Table",
            "Wardrobe", "Dresser", "Filing Cabinet", "TV Stand", "Chest of Drawers",
            "Twin Bed", "Queen Bed", "King Bed", "Bunk Bed", "Murphy Bed",
            "Headboard", "Crib", "Chaise Lounge", "Futon"
    };
    private static final float[][] FURNITURE_DIMS = {
            {0.6f, 0.6f, 0.9f}, {2.0f, 0.9f, 0.8f}, {1.8f, 0.9f, 0.75f}, {0.5f, 0.5f, 0.6f}, {1.6f, 2.1f, 0.5f}, {0.8f, 0.3f, 1.8f},
            {0.9f, 0.9f, 0.9f}, {0.5f, 0.55f, 0.95f},{0.6f, 0.6f, 1.05f}, {0.4f, 0.4f, 0.7f}, {1.4f, 0.45f, 0.45f},{1.0f, 1.0f, 1.0f}, {0.7f, 0.5f, 0.4f},
            {1.2f, 0.6f, 0.45f}, {1.4f, 0.7f, 0.76f}, {1.2f, 0.35f, 0.8f}, {0.5f, 0.5f, 0.6f},
            {1.0f, 0.6f, 1.9f}, {1.5f, 0.5f, 0.8f}, {0.5f, 0.6f, 1.1f}, {1.6f, 0.45f, 0.5f}, {0.9f, 0.5f, 1.2f},
            {1.0f, 2.0f, 0.5f}, {1.6f, 2.1f, 0.5f}, {2.0f, 2.1f, 0.5f}, {1.0f, 2.0f, 1.7f}, {1.6f, 0.5f, 2.1f},
            {1.6f, 0.1f, 0.7f}, {0.7f, 1.3f, 0.9f}, {0.8f, 1.7f, 0.7f}, {1.9f, 0.8f, 0.75f}
    };
    private static final float KEYBOARD_MOVE_STEP = 0.05f; // Movement increment per key press

    public MainAppFrame() {
        setTitle("Furniture Designer");
        setSize(1400, 900); // Default size
        setLocationRelativeTo(null); // Center window
        // Handle closing differently later when adding save prompts
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Simple exit for now
        // addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { handleExit(); } }); // Use later

        // Initialize core components
        designModel = new DesignModel();
        pickingHelper = new PickingHelper(); // Initialize PickingHelper
        // undoManager = new UndoManager(); // Init later
        // setupActions(); // Setup undo/redo actions later

        // Setup JOGL Canvas
        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true); // Enable multisampling
        caps.setNumSamples(4);      // Set number of samples
        designCanvas = new GLJPanel(caps);

        // Initialize Renderer and link to canvas
        renderer = new DesignRenderer(designModel); // Renderer uses the model
        designCanvas.addGLEventListener(renderer);

        // Make canvas focusable for keyboard input
        designCanvas.setFocusable(true);

        // Setup interaction listeners (mouse, keyboard)
        setupMouseInteraction();
        setupKeyInteraction();

        // Setup and start animator for continuous rendering
        animator = new FPSAnimator(designCanvas, 60); // Target 60 FPS
        animator.start();

        // Initialize camera based on the initial room AFTER renderer is created
        if (renderer != null && renderer.getCameraManager() != null) {
            renderer.updateCameraForModel(); // Let renderer handle camera setup based on model
        }

        // Setup UI elements
        setupMenuBar();
        JPanel mainContent = new JPanel(new BorderLayout());
        controlPanel = createControlPanel(); // Create the control panel
        mainContent.add(new JScrollPane(controlPanel), BorderLayout.WEST); // Control panel on the left
        mainContent.add(designCanvas, BorderLayout.CENTER); // GL canvas in the center
        add(mainContent); // Add main content to the JFrame

        // Initial UI update based on the starting model state
        updateUIFromModel();
        // updateUndoRedoState(); // Call later when undo is implemented
        if (renderer != null && showGridMenuItem != null) {
            renderer.setShowGrid(showGridMenuItem.isSelected()); // Set initial grid state
        }

        // Add listener AFTER panel creation to avoid triggering during setup
        if (roomShapeComboBox != null) {
            roomShapeComboBox.addActionListener(e -> {
                Room.RoomShape selectedShape = (Room.RoomShape) roomShapeComboBox.getSelectedItem();
                if (selectedShape != null && roomParameterCardLayout != null && roomParameterCardsPanel != null) {
                    // Switch the visible card based on selection
                    switch (selectedShape) {
                        case RECTANGULAR: roomParameterCardLayout.show(roomParameterCardsPanel, RECT_CARD); break;
                        case CIRCULAR:    roomParameterCardLayout.show(roomParameterCardsPanel, CIRC_CARD); break;
                        case L_SHAPED:    roomParameterCardLayout.show(roomParameterCardsPanel, L_CARD); break;
                        case T_SHAPED:    roomParameterCardLayout.show(roomParameterCardsPanel, T_CARD); break;
                    }
                }
                // Ensure layout updates visually
                if (roomParameterCardsPanel != null) {
                    roomParameterCardsPanel.revalidate();
                    roomParameterCardsPanel.repaint();
                }
                // IMPORTANT: Do NOT call handleUpdateRoomSize or updateUIFromModel here.
                // This listener should only change the view. Updates happen when the "Update" button is clicked.
            });
        }
    }

    // Setup Undo/Redo actions (placeholder for now)
    private void setupActions() {
        // Implement later
        System.out.println("Undo/Redo actions setup placeholder.");
    }

    // Setup the main menu bar
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New Design");
        newItem.addActionListener(e -> handleNewDesign());
        JMenuItem openItem = new JMenuItem("Open Design...");
        openItem.addActionListener(e -> handleLoadDesign()); // Implement later
        JMenuItem saveItem = new JMenuItem("Save Design...");
        saveItem.addActionListener(e -> handleSaveDesign()); // Implement later
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> handleExit()); // Implement later
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        // JMenuItem undoItem = new JMenuItem(undoAction); // Add later
        // JMenuItem redoItem = new JMenuItem(redoAction); // Add later
        JMenuItem undoItem = new JMenuItem("Undo"); // Placeholder
        undoItem.setEnabled(false); // Disable for now
        JMenuItem redoItem = new JMenuItem("Redo"); // Placeholder
        redoItem.setEnabled(false); // Disable for now
        JMenuItem deleteItem = new JMenuItem("Delete Selected Furniture");
        deleteItem.addActionListener(e -> handleDeleteSelectedFurniture());
        deleteItem.setEnabled(false); // Initially disabled
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        JRadioButtonMenuItem view2DItem = new JRadioButtonMenuItem("2D View (Top Down)");
        JRadioButtonMenuItem view3DItem = new JRadioButtonMenuItem("3D View");
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(view2DItem);
        viewGroup.add(view3DItem);
        view3DItem.setSelected(true); // Default to 3D view
        view2DItem.addActionListener(e -> setViewMode(false));
        view3DItem.addActionListener(e -> setViewMode(true));
        showGridMenuItem = new JCheckBoxMenuItem("Show Grid", true);
        showGridMenuItem.addActionListener(e -> {
            if (renderer != null) {
                renderer.setShowGrid(showGridMenuItem.isSelected());
                designCanvas.repaint(); // Redraw to show/hide grid
            }
        });
        viewMenu.add(view2DItem);
        viewMenu.add(view3DItem);
        viewMenu.addSeparator();
        viewMenu.add(showGridMenuItem);

        // Help Menu (placeholder)
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    // Create the main control panel on the left side
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Vertical stacking
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        panel.setPreferredSize(new Dimension(300, 800)); // Give it a preferred width

        // --- Room Properties Section ---
        JPanel roomPanel = createSectionPanel("Room Properties");
        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS));

        // Shape Selection
        JPanel shapeSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        shapeSelectPanel.add(new JLabel("Shape:"));
        roomShapeComboBox = new JComboBox<>(Room.RoomShape.values());
        shapeSelectPanel.add(roomShapeComboBox);
        shapeSelectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Constrain height to prevent vertical stretching
        shapeSelectPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, shapeSelectPanel.getPreferredSize().height));
        roomPanel.add(shapeSelectPanel);
        roomPanel.add(Box.createVerticalStrut(5));

        // Parameter Input Panels (using CardLayout)
        roomParameterCardLayout = new CardLayout();
        roomParameterCardsPanel = new JPanel(roomParameterCardLayout);
        roomParameterCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create the shared height field ONCE
        sharedHeightField = new JTextField(5);

        // Panel 1: Rectangular
        JPanel rectParamPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Grid layout
        rectParamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding inside grid
        roomWidthField = new JTextField(5);
        roomLengthField = new JTextField(5);
        rectParamPanel.add(new JLabel("Width (m):")); rectParamPanel.add(roomWidthField);
        rectParamPanel.add(new JLabel("Length (m):")); rectParamPanel.add(roomLengthField);
        rectParamPanel.add(new JLabel("Height (m):")); rectParamPanel.add(sharedHeightField); // Add shared field
        roomParameterCardsPanel.add(rectParamPanel, RECT_CARD);

        // Panel 2: Circular
        JPanel circParamPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        circParamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roomRadiusField = new JTextField(5);
        circParamPanel.add(new JLabel("Radius (m):")); circParamPanel.add(roomRadiusField);
        circParamPanel.add(new JLabel("Height (m):")); circParamPanel.add(sharedHeightField); // Reuse shared field
        roomParameterCardsPanel.add(circParamPanel, CIRC_CARD);

        // Panel 3: L-Shaped
        JPanel lParamPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        lParamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lOuterWidthField = new JTextField(5); lOuterLengthField = new JTextField(5);
        lInsetWidthField = new JTextField(5); lInsetLengthField = new JTextField(5);
        lParamPanel.add(new JLabel("Outer W (m):")); lParamPanel.add(lOuterWidthField);
        lParamPanel.add(new JLabel("Outer L (m):")); lParamPanel.add(lOuterLengthField);
        lParamPanel.add(new JLabel("Inset W (m):")); lParamPanel.add(lInsetWidthField);
        lParamPanel.add(new JLabel("Inset L (m):")); lParamPanel.add(lInsetLengthField);
        lParamPanel.add(new JLabel("Height (m):")); lParamPanel.add(sharedHeightField); // Reuse shared field
        roomParameterCardsPanel.add(lParamPanel, L_CARD);

        // Panel 4: T-Shaped
        JPanel tParamPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        tParamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tBarWidthField = new JTextField(5); tBarLengthField = new JTextField(5);
        tStemWidthField = new JTextField(5); tStemLengthField = new JTextField(5);
        tParamPanel.add(new JLabel("Bar W (m):")); tParamPanel.add(tBarWidthField);
        tParamPanel.add(new JLabel("Bar L (m):")); tParamPanel.add(tBarLengthField);
        tParamPanel.add(new JLabel("Stem W (m):")); tParamPanel.add(tStemWidthField);
        tParamPanel.add(new JLabel("Stem L (m):")); tParamPanel.add(tStemLengthField);
        tParamPanel.add(new JLabel("Height (m):")); tParamPanel.add(sharedHeightField); // Reuse shared field
        roomParameterCardsPanel.add(tParamPanel, T_CARD);

        roomPanel.add(roomParameterCardsPanel); // Add the card panel container
        roomPanel.add(Box.createVerticalStrut(5));

        // Update Room Dimensions Button
        updateRoomSizeButton = new JButton("Update Dimensions");
        updateRoomSizeButton.addActionListener(e -> handleUpdateRoomSize()); // Implement later
        updateRoomSizeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomPanel.add(updateRoomSizeButton);
        roomPanel.add(Box.createVerticalStrut(10));

        // --- Room Appearance ---
        wallColorButton = new JButton("Set Wall Color");
        wallColorButton.addActionListener(e -> handleSetRoomColor(true)); // Implement later
        floorColorButton = new JButton("Set Floor Color");
        floorColorButton.addActionListener(e -> handleSetRoomColor(false)); // Implement later
        JButton wallTextureButtonLocal = new JButton("Set Wall Texture...");
        wallTextureButtonLocal.addActionListener(e -> handleSetRoomTexture(true)); // Implement later
        JButton floorTextureButtonLocal = new JButton("Set Floor Texture...");
        floorTextureButtonLocal.addActionListener(e -> handleSetRoomTexture(false)); // Implement later

        JPanel appearanceButtonsPanel = new JPanel(); // Use a sub-panel for layout
        appearanceButtonsPanel.setLayout(new BoxLayout(appearanceButtonsPanel, BoxLayout.Y_AXIS));
        // Ensure buttons align left
        wallColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        floorColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        wallTextureButtonLocal.setAlignmentX(Component.LEFT_ALIGNMENT);
        floorTextureButtonLocal.setAlignmentX(Component.LEFT_ALIGNMENT);
        appearanceButtonsPanel.add(wallColorButton);
        appearanceButtonsPanel.add(Box.createVerticalStrut(5));
        appearanceButtonsPanel.add(floorColorButton);
        appearanceButtonsPanel.add(Box.createVerticalStrut(5));
        appearanceButtonsPanel.add(wallTextureButtonLocal);
        appearanceButtonsPanel.add(Box.createVerticalStrut(5));
        appearanceButtonsPanel.add(floorTextureButtonLocal);
        appearanceButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align the sub-panel itself
        roomPanel.add(appearanceButtonsPanel); // Add the sub-panel to the room panel

        panel.add(roomPanel);
        panel.add(Box.createVerticalStrut(15)); // Space between sections

        // --- Furniture Library Section ---
        JPanel libraryPanel = createSectionPanel("Furniture Library");
        libraryPanel.setLayout(new BoxLayout(libraryPanel, BoxLayout.Y_AXIS));
        furnitureLibraryList = new JList<>(FURNITURE_TYPES);
        furnitureLibraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        furnitureLibraryList.setVisibleRowCount(8); // Show more rows
        furnitureLibraryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = furnitureLibraryList.getSelectedIndex();
                // Update dimension fields only if NO furniture is selected in the scene
                if (idx != -1 && designModel.getSelectedFurniture() == null) {
                    furnWidthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][0]));
                    furnDepthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][1]));
                    furnHeightField.setText(String.format("%.2f", FURNITURE_DIMS[idx][2]));
                    furnRotationYField.setText("0.0"); // Default rotation
                }
            }
        });
        JScrollPane libraryScrollPane = new JScrollPane(furnitureLibraryList);
        libraryScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Constrain scroll pane height
        libraryScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        libraryPanel.add(libraryScrollPane);
        libraryPanel.add(Box.createVerticalStrut(5));

        JButton addFurnitureButtonLocal = new JButton("Add Selected Furniture");
        addFurnitureButtonLocal.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFurnitureButtonLocal.addActionListener(e -> handleAddFurniture());
        libraryPanel.add(addFurnitureButtonLocal);

        panel.add(libraryPanel);
        panel.add(Box.createVerticalStrut(15));

        // --- Selected Furniture Section ---
        JPanel furniturePropsPanelOuter = createSectionPanel("Selected Furniture");
        furniturePropsPanelOuter.setLayout(new BoxLayout(furniturePropsPanelOuter, BoxLayout.Y_AXIS));

        // Sub-panel for Dimensions and Rotation
        furnitureDimsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        furnitureDimsPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
        furnWidthField = new JTextField(5); furnDepthField = new JTextField(5);
        furnHeightField = new JTextField(5); furnRotationYField = new JTextField(5);
        furnitureDimsPanel.add(new JLabel("Width:")); furnitureDimsPanel.add(furnWidthField);
        furnitureDimsPanel.add(new JLabel("Depth:")); furnitureDimsPanel.add(furnDepthField);
        furnitureDimsPanel.add(new JLabel("Height:")); furnitureDimsPanel.add(furnHeightField);
        furnitureDimsPanel.add(new JLabel("Rotation Y:")); furnitureDimsPanel.add(furnRotationYField);
        furnitureDimsPanel.add(new JLabel("")); // Spacer
        updateFurnDimsButton = new JButton("Update Dimensions");
        updateFurnDimsButton.addActionListener(e -> handleUpdateFurnitureDimensions()); // Implement later
        furnitureDimsPanel.add(updateFurnDimsButton);
        furnitureDimsPanel.add(new JLabel("")); // Spacer
        updateFurnRotationButton = new JButton("Update Rotation");
        updateFurnRotationButton.addActionListener(e -> handleUpdateFurnitureRotation()); // Implement later
        furnitureDimsPanel.add(updateFurnRotationButton);
        furnitureDimsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        furniturePropsPanelOuter.add(furnitureDimsPanel);
        furniturePropsPanelOuter.add(Box.createVerticalStrut(10));

        // Sub-panel for Appearance
        furnitureAppearancePanel = new JPanel();
        furnitureAppearancePanel.setLayout(new BoxLayout(furnitureAppearancePanel, BoxLayout.Y_AXIS));
        furnitureAppearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));
        furnitureColorButton = new JButton("Set Furniture Color");
        furnitureColorButton.addActionListener(e -> handleSetFurnitureColor()); // Implement later
        furnitureTextureButton = new JButton("Set Furniture Texture...");
        furnitureTextureButton.addActionListener(e -> handleSetFurnitureTexture()); // Implement later
        furnitureColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        furnitureTextureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        furnitureAppearancePanel.add(furnitureColorButton);
        furnitureAppearancePanel.add(Box.createVerticalStrut(5));
        furnitureAppearancePanel.add(furnitureTextureButton);
        furnitureAppearancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        furniturePropsPanelOuter.add(furnitureAppearancePanel);
        furniturePropsPanelOuter.add(Box.createVerticalStrut(10));

        // Delete Button for Selected Furniture
        deleteFurnitureButton = new JButton("Delete Selected Furniture");
        deleteFurnitureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteFurnitureButton.addActionListener(e -> handleDeleteSelectedFurniture());
        furniturePropsPanelOuter.add(deleteFurnitureButton);

        panel.add(furniturePropsPanelOuter);
        panel.add(Box.createVerticalStrut(15));

        // --- View Controls Section ---
        JPanel viewPanel = createSectionPanel("View Controls");
        viewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        viewModeComboBox = new JComboBox<>(new String[]{"3D View", "2D View (Top Down)"});
        viewModeComboBox.addActionListener(e -> setViewMode(viewModeComboBox.getSelectedIndex() == 0));
        viewPanel.add(new JLabel("Mode:"));
        viewPanel.add(viewModeComboBox);
        viewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, viewPanel.getPreferredSize().height));
        panel.add(viewPanel);

        panel.add(Box.createVerticalGlue()); // Pushes content towards the top

        // Initial state for furniture controls (disabled)
        setFurnitureControlsEnabled(false);

        return panel;
    }

    // Helper to create styled section panels
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        // Set alignment for BoxLayout container
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    // Helper method to enable/disable all furniture property controls
    private void setFurnitureControlsEnabled(boolean enabled) {
        // Check if panels/components exist before enabling/disabling
        if (furnitureDimsPanel != null) {
            for (Component c : furnitureDimsPanel.getComponents()) {
                c.setEnabled(enabled);
            }
        }
        if (furnitureAppearancePanel != null) {
            for (Component c : furnitureAppearancePanel.getComponents()) {
                c.setEnabled(enabled);
            }
        }
        if (deleteFurnitureButton != null) {
            deleteFurnitureButton.setEnabled(enabled);
        }
        // Also handle the menu item
        JMenuBar mb = getJMenuBar();
        if (mb != null) {
            try {
                JMenu editMenu = mb.getMenu(1); // Assuming Edit is the second menu
                if (editMenu != null && editMenu.getItemCount() > 3) {
                    editMenu.getItem(3).setEnabled(enabled); // Delete item
                }
            } catch (Exception e) { /* Ignore potential errors */ }
        }
    }


    // Update the UI elements based on the current state of the DesignModel
    private void updateUIFromModel() {
        if (designModel == null || designModel.getRoom() == null) return;

        Room room = designModel.getRoom();

        // --- Update Room Section ---
        // Temporarily remove listener to prevent selection change triggering updates
        ActionListener roomShapeListener = roomShapeComboBox.getActionListeners().length > 0 ? roomShapeComboBox.getActionListeners()[0] : null;
        if (roomShapeListener != null) roomShapeComboBox.removeActionListener(roomShapeListener);
        roomShapeComboBox.setSelectedItem(room.getShape()); // Set combo box to actual model shape
        if (roomShapeListener != null) roomShapeComboBox.addActionListener(roomShapeListener);

        // Update the shared height field (always present)
        // Check focus to avoid overwriting user input
        if (sharedHeightField != null && !sharedHeightField.hasFocus()) {
            sharedHeightField.setText(String.format("%.2f", room.getHeight()));
        }

        // Update dimension fields specific to the current shape
        switch (room.getShape()) {
            case RECTANGULAR:
                if (roomWidthField != null && !roomWidthField.hasFocus()) roomWidthField.setText(String.format("%.2f", room.getWidth()));
                if (roomLengthField != null && !roomLengthField.hasFocus()) roomLengthField.setText(String.format("%.2f", room.getLength()));
                break;
            case CIRCULAR:
                if (roomRadiusField != null && !roomRadiusField.hasFocus()) roomRadiusField.setText(String.format("%.2f", room.getRadius()));
                break;
            case L_SHAPED:
                if (lOuterWidthField != null && !lOuterWidthField.hasFocus()) lOuterWidthField.setText(String.format("%.2f", room.getL_outerWidth()));
                if (lOuterLengthField != null && !lOuterLengthField.hasFocus()) lOuterLengthField.setText(String.format("%.2f", room.getL_outerLength()));
                if (lInsetWidthField != null && !lInsetWidthField.hasFocus()) lInsetWidthField.setText(String.format("%.2f", room.getL_insetWidth()));
                if (lInsetLengthField != null && !lInsetLengthField.hasFocus()) lInsetLengthField.setText(String.format("%.2f", room.getL_insetLength()));
                break;
            case T_SHAPED:
                if (tBarWidthField != null && !tBarWidthField.hasFocus()) tBarWidthField.setText(String.format("%.2f", room.getT_barWidth()));
                if (tBarLengthField != null && !tBarLengthField.hasFocus()) tBarLengthField.setText(String.format("%.2f", room.getT_barLength()));
                if (tStemWidthField != null && !tStemWidthField.hasFocus()) tStemWidthField.setText(String.format("%.2f", room.getT_stemWidth()));
                if (tStemLengthField != null && !tStemLengthField.hasFocus()) tStemLengthField.setText(String.format("%.2f", room.getT_stemLength()));
                break;
        }
        // Ensure the correct card is showing (redundant if listener only switches view, but safe)
        if (roomParameterCardLayout != null && roomParameterCardsPanel != null) {
            String cardToShow = RECT_CARD; // Default
            switch (room.getShape()) {
                case CIRCULAR: cardToShow = CIRC_CARD; break;
                case L_SHAPED: cardToShow = L_CARD; break;
                case T_SHAPED: cardToShow = T_CARD; break;
            }
            roomParameterCardLayout.show(roomParameterCardsPanel, cardToShow);
        }


        // Update room appearance buttons
        if (wallColorButton != null) {
            wallColorButton.setBackground(room.getWallColor());
            wallColorButton.setForeground(getContrastColor(room.getWallColor()));
        }
        if (floorColorButton != null) {
            floorColorButton.setBackground(room.getFloorColor());
            floorColorButton.setForeground(getContrastColor(room.getFloorColor()));
        }

        // --- Update Selected Furniture Section ---
        Furniture selected = designModel.getSelectedFurniture();
        boolean furnitureSelected = (selected != null);

        // Enable/Disable furniture controls based on selection
        setFurnitureControlsEnabled(furnitureSelected);

        // Update fields if furniture is selected
        if (furnitureSelected) {
            if (furnWidthField != null && !furnWidthField.hasFocus()) furnWidthField.setText(String.format("%.2f", selected.getWidth()));
            if (furnDepthField != null && !furnDepthField.hasFocus()) furnDepthField.setText(String.format("%.2f", selected.getDepth()));
            if (furnHeightField != null && !furnHeightField.hasFocus()) furnHeightField.setText(String.format("%.2f", selected.getHeight()));
            if (furnRotationYField != null && !furnRotationYField.hasFocus()) furnRotationYField.setText(String.format("%.1f", selected.getRotation().y));
            if (furnitureColorButton != null) {
                furnitureColorButton.setBackground(selected.getColor());
                furnitureColorButton.setForeground(getContrastColor(selected.getColor()));
            }
            // Clear selection in the library list when furniture is selected in the scene
            if (furnitureLibraryList != null) furnitureLibraryList.clearSelection();

        } else {
            // No furniture selected in scene - check if library item is selected
            if (furnitureLibraryList != null && furnitureLibraryList.getSelectedIndex() != -1) {
                // Library item is selected, show its default dimensions
                int idx = furnitureLibraryList.getSelectedIndex();
                if (furnWidthField != null && !furnWidthField.hasFocus()) furnWidthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][0]));
                if (furnDepthField != null && !furnDepthField.hasFocus()) furnDepthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][1]));
                if (furnHeightField != null && !furnHeightField.hasFocus()) furnHeightField.setText(String.format("%.2f", FURNITURE_DIMS[idx][2]));
                if (furnRotationYField != null && !furnRotationYField.hasFocus()) furnRotationYField.setText("0.0");
            } else {
                // Nothing selected anywhere, clear fields
                if (furnWidthField != null && !furnWidthField.hasFocus()) furnWidthField.setText("");
                if (furnDepthField != null && !furnDepthField.hasFocus()) furnDepthField.setText("");
                if (furnHeightField != null && !furnHeightField.hasFocus()) furnHeightField.setText("");
                if (furnRotationYField != null && !furnRotationYField.hasFocus()) furnRotationYField.setText("");
            }
            // Reset furniture color button appearance
            if (furnitureColorButton != null) {
                furnitureColorButton.setBackground(UIManager.getColor("Button.background"));
                furnitureColorButton.setForeground(UIManager.getColor("Button.foreground"));
            }
        }

        // Ensure canvas gets focus back if needed after UI updates
        designCanvas.requestFocusInWindow();
    }

    // Helper to determine text color based on background brightness
    private Color getContrastColor(Color background) {
        if (background == null) return Color.BLACK;
        // Formula for perceived luminance (Y)
        double y = (299 * background.getRed() + 587 * background.getGreen() + 114 * background.getBlue()) / 1000.0;
        return (y >= 128) ? Color.BLACK : Color.WHITE; // Black text on light bg, white text on dark bg
    }

    // Update Undo/Redo menu item state (placeholder)
    private void updateUndoRedoState() {
        // Implement later
        // undoAction.setEnabled(undoManager.canUndo());
        // undoAction.putValue(Action.NAME, undoManager.getUndoPresentationName());
        // redoAction.setEnabled(undoManager.canRedo());
        // redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());
    }

    // Set the view mode (3D or 2D)
    private void setViewMode(boolean is3D) {
        if (renderer != null && renderer.getCameraManager() != null) {
            renderer.set3DMode(is3D);
            // Update UI elements to reflect the mode change
            if (viewModeComboBox != null) viewModeComboBox.setSelectedIndex(is3D ? 0 : 1);
            JMenuBar mb = getJMenuBar();
            if (mb != null) {
                try {
                    JMenu viewMenu = mb.getMenu(2); // Assuming View is the third menu
                    if (viewMenu != null && viewMenu.getItemCount() > 1) {
                        ((JRadioButtonMenuItem)viewMenu.getItem(0)).setSelected(!is3D); // 2D item
                        ((JRadioButtonMenuItem)viewMenu.getItem(1)).setSelected(is3D);  // 3D item
                    }
                } catch (Exception e) { /* Ignore potential errors */ }
            }
            designCanvas.repaint(); // Redraw with the new projection/view
        }
    }

    // --- Mouse Interaction ---
    private void setupMouseInteraction() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                designCanvas.requestFocusInWindow(); // Ensure canvas has focus for keyboard events

                // Right Mouse Button: Always Camera Rotation
                if (SwingUtilities.isRightMouseButton(e)) {
                    isDraggingFurniture = false; // Ensure furniture drag is off
                    draggedFurniture = null;
                    isDraggingCamera = true;     // Start camera drag (rotation mode)
                }
                // Middle Mouse Button or Shift + Left Mouse: Always Camera Pan
                else if (SwingUtilities.isMiddleMouseButton(e) || (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown())) {
                    isDraggingFurniture = false;
                    draggedFurniture = null;
                    isDraggingCamera = true;     // Start camera drag (pan mode)
                }
                // Left Mouse Button (without Shift): Picking / Furniture Drag / Deselect
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDraggingCamera = false; // Ensure camera drag is off

                    // Perform picking using PickingHelper
                    Furniture pickedFurniture = null;
                    if (pickingHelper != null && renderer != null) {
                        // Need current matrices and viewport from renderer (implement capture later)
                        // For now, pass placeholder matrices/viewport if not captured yet
                        double[] mv = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1}; // Identity placeholder
                        double[] proj = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1}; // Identity placeholder
                        int[] vp = {0, 0, designCanvas.getWidth(), designCanvas.getHeight()}; // Current viewport

                        // renderer.captureMatricesForPicking(gl); // Needs GL context, do in display
                        pickedFurniture = pickingHelper.pickFurniture(e.getX(), e.getY(), designModel,
                                renderer.lastModelview, // Use renderer's cache
                                renderer.lastProjection,// Use renderer's cache
                                renderer.lastViewport); // Use renderer's cache
                    }

                    if (pickedFurniture != null) { // Clicked ON existing furniture
                        // If different furniture, select it
                        if (designModel.getSelectedFurniture() != pickedFurniture) {
                            finalizeKeyboardMove(); // Finalize keyboard move before changing selection
                            designModel.setSelectedFurniture(pickedFurniture);
                            updateUIFromModel(); // Update UI for new selection
                        }
                        // Start dragging this furniture
                        isDraggingFurniture = true;
                        draggedFurniture = pickedFurniture;

                        // Calculate offset from furniture origin to click point ON THE FLOOR
                        Vector3f clickFloorPos = null;
                        if (renderer != null) {
                            // Use renderer's cache
                            clickFloorPos = pickingHelper.screenToWorldOnPlane(e.getX(), e.getY(), 0.0f,
                                    renderer.lastModelview, renderer.lastProjection, renderer.lastViewport);
                        }
                        if (clickFloorPos != null) {
                            dragOffset = new Vector3f(
                                    clickFloorPos.x - draggedFurniture.getPosition().x,
                                    0, // We are dragging relative to the floor click
                                    clickFloorPos.z - draggedFurniture.getPosition().z
                            );
                        } else {
                            dragOffset = new Vector3f(0, 0, 0); // Fallback if floor click fails
                        }
                    } else { // Clicked on EMPTY space
                        // Deselect any currently selected furniture
                        if (designModel.getSelectedFurniture() != null) {
                            finalizeKeyboardMove(); // Finalize move before deselecting
                            designModel.setSelectedFurniture(null);
                            updateUIFromModel();    // Update UI (disable controls, clear fields)
                            designCanvas.repaint(); // Redraw to remove selection indicator
                        }
                        isDraggingFurniture = false; // Ensure furniture drag is off
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint == null) return;

                float deltaX = e.getX() - lastMousePoint.x;
                float deltaY = e.getY() - lastMousePoint.y;

                if (isDraggingCamera && renderer != null) {
                    // Distinguish between pan (middle/shift+left) and rotate (right)
                    // This logic assumes the button state persists from mousePressed
                    // A more robust way might check e.getModifiersEx() here too
                    if (SwingUtilities.isMiddleMouseButton(e) || (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown())) {
                        renderer.panCamera(deltaX, deltaY);
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        renderer.rotateCamera(deltaX, deltaY);
                    }
                } else if (isDraggingFurniture && draggedFurniture != null && renderer != null) {
                    // Get current mouse position on the floor
                    Vector3f floorPos = pickingHelper.screenToWorldOnPlane(e.getX(), e.getY(), 0.0f,
                            renderer.lastModelview, renderer.lastProjection, renderer.lastViewport);

                    if (floorPos != null && dragOffset != null) {
                        // Calculate new position based on floor click minus the initial offset
                        float newX = floorPos.x - dragOffset.x;
                        float newZ = floorPos.z - dragOffset.z;
                        // Keep original Y position (height)
                        float originalY = draggedFurniture.getPosition().y;

                        // This requires knowing the room shape and dimensions.

                        draggedFurniture.setPosition(new Vector3f(newX, originalY, newZ));
                        updateUIFromModel(); // Update position fields while dragging (optional)
                    }
                }

                lastMousePoint = e.getPoint();
                designCanvas.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Finalize keyboard move if one was active
                finalizeKeyboardMove();

                // If we were dragging furniture, record the final position for Undo (later)
                if (isDraggingFurniture && draggedFurniture != null) {
                    System.out.println("Finished dragging " + draggedFurniture.getType());
                    // Register Undoable Edit here later
                    updateUIFromModel(); // Ensure final position is reflected in UI
                }

                // Reset all dragging flags and states
                isDraggingCamera = false;
                isDraggingFurniture = false;
                draggedFurniture = null;
                dragOffset = null;
                lastMousePoint = null;

                // updateUndoRedoState(); // Update undo state later
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                finalizeKeyboardMove(); // Stop keyboard move on zoom
                if (renderer != null) {
                    // Negative rotation for standard zoom direction (wheel down = zoom out)
                    float delta = -e.getWheelRotation();
                    renderer.zoomCamera(delta);
                    designCanvas.repaint();
                }
            }
        };

        designCanvas.addMouseListener(mouseAdapter);
        designCanvas.addMouseMotionListener(mouseAdapter);
        designCanvas.addMouseWheelListener(mouseAdapter);
    }

    // --- Keyboard Interaction ---
    private void setupKeyInteraction() {
        designCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
        // Add FocusListener to stop keyboard move if focus is lost
        designCanvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finalizeKeyboardMove();
            }
        });
    }

    // Handle key press events for furniture movement
    private void handleKeyPress(KeyEvent e) {
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return; // Only act if furniture is selected

        int keyCode = e.getKeyCode();

        // Check for arrow keys
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
        {
            if (!isMovingWithKeyboard) { // Start of a keyboard move sequence
                isMovingWithKeyboard = true;
                // Record starting position for potential undo later
                // keyboardMoveStartPosition = selected.getPosition().clone();
            }
            pressedKeys.add(keyCode); // Record which key is pressed

            // Calculate movement based on pressed keys
            float dx = 0, dz = 0;
            if (pressedKeys.contains(KeyEvent.VK_UP))    dz -= KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_DOWN))  dz += KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_LEFT))  dx -= KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) dx += KEYBOARD_MOVE_STEP;

            // Apply movement relative to current position
            Vector3f currentPos = selected.getPosition();
            float newX = currentPos.x + dx;
            float newZ = currentPos.z + dz;


            selected.setPosition(new Vector3f(newX, currentPos.y, newZ));
            updateUIFromModel(); // Update position fields during move
            designCanvas.repaint();

        } else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
            handleDeleteSelectedFurniture();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            // Optional: Cancel ongoing keyboard move
            // cancelKeyboardMove();
        }
    }

    // Handle key release events
    private void handleKeyRelease(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (pressedKeys.contains(keyCode)) {
            pressedKeys.remove(keyCode); // Remove the released key

            // If no more arrow keys are pressed, finalize the move sequence
            if (isMovingWithKeyboard && !isAnyArrowKeyPressed()) {
                finalizeKeyboardMove();
            }
        }
    }

    // Check if any arrow key is currently held down
    private boolean isAnyArrowKeyPressed() {
        return pressedKeys.contains(KeyEvent.VK_UP) || pressedKeys.contains(KeyEvent.VK_DOWN) ||
                pressedKeys.contains(KeyEvent.VK_LEFT) || pressedKeys.contains(KeyEvent.VK_RIGHT);
    }

    // Finalize a keyboard movement sequence (for undo later)
    private void finalizeKeyboardMove() {
        if (isMovingWithKeyboard) {
            System.out.println("Keyboard move finished.");
            // Register Undoable Edit here later using start/end positions
            isMovingWithKeyboard = false;
            // keyboardMoveStartPosition = null;
            pressedKeys.clear(); // Clear all keys
            // updateUndoRedoState(); // Update undo state later
        }
    }


    // --- Action Handlers (Placeholders or Basic Implementation) ---

    private void handleNewDesign() {
        finalizeKeyboardMove(); // Finalize any pending move
        int choice = JOptionPane.showConfirmDialog(this,
                "Clear the current design? Unsaved changes will be lost.",
                "New Design", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            performClearDesign();
        }
    }

    private void performClearDesign() {
        designModel.clearDesign(); // Clears furniture, resets room
        // undoManager.discardAllEdits(); // Clear undo history later
        if (renderer != null) {
            renderer.setDesignModel(designModel); // Update renderer's model reference
            renderer.updateCameraForModel(); // Reset camera for the new default room
        }
        updateUIFromModel();      // Update UI to reflect cleared state
        // updateUndoRedoState(); // Update undo state later
        designCanvas.repaint();
        System.out.println("Design cleared.");
    }


    private void handleLoadDesign() {
        finalizeKeyboardMove();
        JOptionPane.showMessageDialog(this, "Load Design: Not implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        // Implement file chooser and deserialization later
    }

    private void handleSaveDesign() {
        finalizeKeyboardMove();
        JOptionPane.showMessageDialog(this, "Save Design: Not implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        // Implement file chooser and serialization later
    }

    // Handle adding furniture from the library
    private void handleAddFurniture() {
        finalizeKeyboardMove();
        int selectedIndex = furnitureLibraryList.getSelectedIndex();
        if (selectedIndex != -1) {
            String type = FURNITURE_TYPES[selectedIndex];
            try {
                // Use current values from text fields if edited, else defaults
                float w = Float.parseFloat(furnWidthField.getText());
                float d = Float.parseFloat(furnDepthField.getText());
                float h = Float.parseFloat(furnHeightField.getText());
                if (w <= 0 || d <= 0 || h <= 0) throw new NumberFormatException("Dimensions must be positive.");

                // Calculate initial position (e.g., center of the room floor)
                Vector3f initialPos = designModel.getRoom().calculateCenter();
                initialPos.y = 0; // Place on floor

                Furniture newFurniture = new Furniture(type, initialPos, w, d, h);
                designModel.addFurniture(newFurniture); // Adds and selects
                System.out.println("Added: " + type);

                // Register Undoable Edit here later

                updateUIFromModel();    // Update UI to show new selection/properties
                // updateUndoRedoState(); // Update undo state later
                designCanvas.repaint(); // Redraw scene

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid dimensions: Please enter positive numbers.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                // Optionally, revert fields to library defaults or clear them
                updateUIFromModel();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a furniture type from the library.", "No Furniture Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Handle deleting the currently selected furniture
    private void handleDeleteSelectedFurniture() {
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected != null) {
            System.out.println("Deleting: " + selected.getType());
            designModel.removeFurniture(selected); // Removes and deselects

            // Register Undoable Edit here later

            updateUIFromModel();      // Update UI (controls disabled, fields cleared/reset)
            // updateUndoRedoState(); // Update undo state later
            designCanvas.repaint();   // Redraw scene without the furniture

        } else {
            JOptionPane.showMessageDialog(this, "No furniture selected to delete.", "Delete Furniture", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Handle updating room dimensions based on UI fields
    private void handleUpdateRoomSize() {
        finalizeKeyboardMove();
        Room room = designModel.getRoom();
        if (room == null) return;

        try {
            // Get selected shape from COMBO BOX
            Room.RoomShape selectedShape = (Room.RoomShape) roomShapeComboBox.getSelectedItem();
            if (selectedShape == null) return; // Should not happen
            float h = Float.parseFloat(sharedHeightField.getText());
            if (h <= 0) throw new NumberFormatException("Height must be positive.");

            // Read specific parameters based on selected shape from UI
            switch (selectedShape) {
                case RECTANGULAR:
                    float w = Float.parseFloat(roomWidthField.getText());
                    float l = Float.parseFloat(roomLengthField.getText());
                    if (w <= 0 || l <= 0) throw new NumberFormatException("Rectangular dimensions must be positive.");
                    // Apply changes to the model
                    room.setDimensionsRectangular(w, l, h);
                    break;
                case CIRCULAR:
                    float r = Float.parseFloat(roomRadiusField.getText());
                    if (r <= 0) throw new NumberFormatException("Radius must be positive.");
                    room.setDimensionsCircular(r, h);
                    break;
                case L_SHAPED:
                    float loW = Float.parseFloat(lOuterWidthField.getText());
                    float loL = Float.parseFloat(lOuterLengthField.getText());
                    float liW = Float.parseFloat(lInsetWidthField.getText());
                    float liL = Float.parseFloat(lInsetLengthField.getText());
                    // Validation happens within setDimensionsLShape
                    room.setDimensionsLShape(loW, loL, liW, liL, h);
                    break;
                case T_SHAPED:
                    float tbW = Float.parseFloat(tBarWidthField.getText());
                    float tbL = Float.parseFloat(tBarLengthField.getText());
                    float tsW = Float.parseFloat(tStemWidthField.getText());
                    float tsL = Float.parseFloat(tStemLengthField.getText());
                    // Validation happens within setDimensionsTShape
                    room.setDimensionsTShape(tbW, tbL, tsW, tsL, h);
                    break;
            }

            System.out.println("Room dimensions updated for shape: " + selectedShape);
            // Register Undoable Edit here later

            // Reset camera and redraw
            if (renderer != null) {
                renderer.updateCameraForModel(); // Update camera based on new room size/center
            }
            updateUIFromModel(); // Ensure UI reflects the model change (though it should already)
            designCanvas.repaint();
            // updateUndoRedoState(); // Later

        } catch (NumberFormatException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format or dimensions:\n" + ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            updateUIFromModel(); // Reset UI to current valid model state
        }
    }


    private void handleUpdateFurnitureDimensions() {
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        try {
            float newWidth = Float.parseFloat(furnWidthField.getText());
            float newDepth = Float.parseFloat(furnDepthField.getText());
            float newHeight = Float.parseFloat(furnHeightField.getText());
            if (newWidth <= 0 || newDepth <= 0 || newHeight <= 0) {
                throw new NumberFormatException("Dimensions must be positive.");
            }
            // Apply changes directly for now
            selected.setWidth(newWidth);
            selected.setDepth(newDepth);
            selected.setHeight(newHeight);
            System.out.println("Updated dimensions for: " + selected.getType());
            // Register Undoable Edit here later
            designCanvas.repaint();
            updateUIFromModel(); // Refresh UI

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid dimensions: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            updateUIFromModel(); // Reset UI fields
        }
    }

    private void handleUpdateFurnitureRotation() {
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        try {
            float newRotationY = Float.parseFloat(furnRotationYField.getText());
            // Apply changes directly for now
            selected.getRotation().y = newRotationY;
            System.out.println("Updated rotation for: " + selected.getType());
            // Register Undoable Edit here later
            designCanvas.repaint();
            updateUIFromModel(); // Refresh UI

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid rotation angle.", "Input Error", JOptionPane.ERROR_MESSAGE);
            updateUIFromModel(); // Reset UI field
        }
    }

    private void handleSetRoomColor(boolean isWall) {
        finalizeKeyboardMove();
        JOptionPane.showMessageDialog(this, "Set Room Color: Not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        // Implement JColorChooser later
    }

    private void handleSetFurnitureColor() {
        finalizeKeyboardMove();
        JOptionPane.showMessageDialog(this, "Set Furniture Color: Not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        // Implement JColorChooser later
    }

    private void handleSetRoomTexture(boolean isWall) {
        finalizeKeyboardMove();
        JOptionPane.showMessageDialog(this, "Set Room Texture: Not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        // Implement JFileChooser later
    }

    private void handleSetFurnitureTexture() {
        finalizeKeyboardMove();
        JOptionPane.showMessageDialog(this, "Set Furniture Texture: Not fully implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        // Implement JFileChooser later
    }

    private void handleExit() {
        finalizeKeyboardMove();
        // Basic exit for now, add save confirmation later
        System.out.println("Exiting application.");
        if (animator != null && animator.isStarted()) {
            animator.stop(); // Stop animator before disposing
        }
        dispose();       // Close the window
        System.exit(0);  // Terminate the application
    }


    // --- Undo/Redo Infrastructure (Placeholders) ---
    private void registerUndoableEdit(Object edit) { // Use placeholder Object type for now
        // if (edit != null) {
        //     undoManager.addEdit((UndoableEdit)edit);
        //     updateUndoRedoState();
        // }
    }

    // --- Main Method (for testing MainAppFrame directly) ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { e.printStackTrace(); }
            MainAppFrame frame = new MainAppFrame();
            frame.setVisible(true);
        });
    }

}