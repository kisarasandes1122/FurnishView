import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Set;
import java.util.HashSet;



public class MainAppFrame extends JFrame {

    private JPanel controlPanel;
    private GLJPanel designCanvas;
    private JComboBox<String> viewModeComboBox;
    private JButton deleteFurnitureButton;
    private JButton wallColorButton, floorColorButton, furnitureColorButton, furnitureTextureButton;
    // private JButton updateFurnDimsButton, updateFurnRotationButton;
    private JTextField roomWidthField, roomLengthField;
    private JTextField furnWidthField, furnDepthField, furnHeightField, furnRotationYField;
    private JList<String> furnitureLibraryList;
    private JCheckBoxMenuItem showGridMenuItem;

    // --- NEW UI Fields for Room Shapes ---
    // Use Room.RoomShape here
    private JComboBox<Room.RoomShape> roomShapeComboBox;
    private JPanel roomParameterCardsPanel;
    private CardLayout roomParameterCardLayout;
    private JTextField roomRadiusField;
    private JTextField lOuterWidthField, lOuterLengthField, lInsetWidthField, lInsetLengthField;
    private JTextField tBarWidthField, tBarLengthField, tStemWidthField, tStemLengthField;
    private JTextField sharedHeightField;
    private ActionListener roomShapeActionListener; // <<< Store listener instance variable
    private JPanel furnitureAppearancePanel; // Instance variable
    private JPanel furnitureDimsPanel; // Instance variable

    // --- Constants for CardLayout ---
    private static final String RECT_CARD = "Rectangular";
    private static final String CIRC_CARD = "Circular";
    private static final String L_CARD = "L-Shaped";
    private static final String T_CARD = "T-Shaped";


    private FPSAnimator animator;
    private DesignRenderer renderer;
    private DesignModel designModel;

    private UndoManager undoManager;
    private Action undoAction, redoAction;

    // Mouse Interaction State
    private Point lastMousePoint;
    private boolean isDraggingCamera = false;
    private boolean isDraggingFurniture = false;
    private Furniture draggedFurniture = null;
    private Vector3f dragOffset = null;
    private Vector3f dragStartPosition = null;

    // Keyboard Interaction State
    private boolean isMovingWithKeyboard = false;
    private Vector3f keyboardMoveStartPosition = null;
    private static final float KEYBOARD_MOVE_STEP = 0.05f;
    private Set<Integer> pressedKeys = new HashSet<>(); // Keep track of pressed keys


    // --- Furniture Library Data ---
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


    public MainAppFrame() {
        setTitle("Furniture Designer"); setSize(1400, 900);
        setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { handleExit(); } });

        designModel = new DesignModel(); undoManager = new UndoManager(); setupActions();

        GLProfile glp = GLProfile.get(GLProfile.GL2); GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true); caps.setNumSamples(4);
        designCanvas = new GLJPanel(caps);
        renderer = new DesignRenderer(designModel); designCanvas.addGLEventListener(renderer);

        designCanvas.setFocusable(true);
        setupMouseInteraction();
        setupKeyInteraction();

        animator = new FPSAnimator(designCanvas, 60); animator.start();

        // Initialize camera after renderer and model are ready
        if (renderer != null && designModel != null && designModel.getRoom() != null) {
            renderer.getCameraManager().resetTargetToCenter(designModel.getRoom());
            renderer.updateCameraForModel(); // Call this to set initial state fully
        }

        setupMenuBar();
        JPanel mainContent = new JPanel(new BorderLayout());
        controlPanel = createControlPanel();
        mainContent.add(new JScrollPane(controlPanel), BorderLayout.WEST);
        mainContent.add(designCanvas, BorderLayout.CENTER); add(mainContent);

        updateUIFromModel(); // Initial UI state based on model
        updateUndoRedoState();
        if (renderer != null && showGridMenuItem != null) { renderer.setShowGrid(showGridMenuItem.isSelected()); }

        // Add listener AFTER panel creation
        if (roomShapeComboBox != null) {
            // Define the listener separately
            roomShapeActionListener = e -> {
                Room.RoomShape selectedShape = (Room.RoomShape) roomShapeComboBox.getSelectedItem();
                if (selectedShape != null && roomParameterCardLayout != null && roomParameterCardsPanel != null) {
                    // This listener should ONLY switch the card panel view.
                    // It should NOT update the model or call updateUIFromModel,
                    // as that would overwrite the user's selection before they click "Update".

                    // Switch the displayed card panel
                    switch (selectedShape) {
                        case RECTANGULAR: roomParameterCardLayout.show(roomParameterCardsPanel, RECT_CARD); break;
                        case CIRCULAR:    roomParameterCardLayout.show(roomParameterCardsPanel, CIRC_CARD); break;
                        case L_SHAPED:    roomParameterCardLayout.show(roomParameterCardsPanel, L_CARD); break;
                        case T_SHAPED:    roomParameterCardLayout.show(roomParameterCardsPanel, T_CARD); break;
                    }
                    // Don't call updateUIFromModel() here.
                }
                // Ensure layout is updated visually
                if (roomParameterCardsPanel != null) {
                    roomParameterCardsPanel.revalidate();
                    roomParameterCardsPanel.repaint();
                }
            };
            // Add the defined listener
            roomShapeComboBox.addActionListener(roomShapeActionListener);
        }
    }

    private void setupActions() {
        undoAction = new AbstractAction("Undo") {
            @Override public void actionPerformed(ActionEvent e) {
                try { if (undoManager.canUndo()) undoManager.undo(); }
                catch (CannotUndoException ex) { System.err.println("Undo failed: " + ex); }
                updateUndoRedoState(); updateUIFromModel(); designCanvas.repaint();
            }
        };
        undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoAction = new AbstractAction("Redo") {
            @Override public void actionPerformed(ActionEvent e) {
                try { if (undoManager.canRedo()) undoManager.redo(); }
                catch (CannotRedoException ex) { System.err.println("Redo failed: " + ex); }
                updateUndoRedoState(); updateUIFromModel(); designCanvas.repaint();
            }
        };
        redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        }
        updateUndoRedoState();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New Design"); newItem.addActionListener(e -> handleNewDesign());
        JMenuItem openItem = new JMenuItem("Open Design..."); openItem.addActionListener(e -> handleLoadDesign());
        JMenuItem saveItem = new JMenuItem("Save Design..."); saveItem.addActionListener(e -> handleSaveDesign());
        JMenuItem exitItem = new JMenuItem("Exit"); exitItem.addActionListener(e -> handleExit());
        fileMenu.add(newItem); fileMenu.add(openItem); fileMenu.add(saveItem); fileMenu.addSeparator(); fileMenu.add(exitItem);
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem(undoAction); JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem deleteItem = new JMenuItem("Delete Selected Furniture"); deleteItem.addActionListener(e -> handleDeleteSelectedFurniture());
        editMenu.add(undoItem); editMenu.add(redoItem); editMenu.addSeparator(); editMenu.add(deleteItem);
        JMenu viewMenu = new JMenu("View");
        JRadioButtonMenuItem view2DItem = new JRadioButtonMenuItem("2D View (Top Down)");
        JRadioButtonMenuItem view3DItem = new JRadioButtonMenuItem("3D View", true);
        ButtonGroup viewGroup = new ButtonGroup(); viewGroup.add(view2DItem); viewGroup.add(view3DItem);
        view2DItem.addActionListener(e -> setViewMode(false)); view3DItem.addActionListener(e -> setViewMode(true));
        showGridMenuItem = new JCheckBoxMenuItem("Show Grid", true);
        showGridMenuItem.addActionListener(e -> { if (renderer != null) { renderer.setShowGrid(showGridMenuItem.isSelected()); designCanvas.repaint(); } });
        viewMenu.add(view2DItem); viewMenu.add(view3DItem); viewMenu.addSeparator(); viewMenu.add(showGridMenuItem);
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu); menuBar.add(editMenu); menuBar.add(viewMenu); menuBar.add(helpMenu); setJMenuBar(menuBar);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Room Properties Section ---
        JPanel roomPanel = createSectionPanel("Room Properties");
        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS));

        // Shape Selection
        JPanel shapeSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        shapeSelectPanel.add(new JLabel("Shape:"));
        roomShapeComboBox = new JComboBox<>(Room.RoomShape.values());
        shapeSelectPanel.add(roomShapeComboBox);
        shapeSelectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        shapeSelectPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, shapeSelectPanel.getPreferredSize().height));
        roomPanel.add(shapeSelectPanel);
        roomPanel.add(Box.createVerticalStrut(5));

        // Parameter Input Panels (using CardLayout)
        roomParameterCardLayout = new CardLayout();
        roomParameterCardsPanel = new JPanel(roomParameterCardLayout);
        roomParameterCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Use a single instance for shared height field
        sharedHeightField = new JTextField(5);

        // Panel 1: Rectangular
        JPanel rectParamPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        rectParamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roomWidthField = new JTextField(5);
        roomLengthField = new JTextField(5);
        rectParamPanel.add(new JLabel("Width (m):")); rectParamPanel.add(roomWidthField);
        rectParamPanel.add(new JLabel("Length (m):")); rectParamPanel.add(roomLengthField);
        rectParamPanel.add(new JLabel("Height (m):")); rectParamPanel.add(sharedHeightField);
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
        lOuterWidthField = new JTextField(5);
        lOuterLengthField = new JTextField(5);
        lInsetWidthField = new JTextField(5);
        lInsetLengthField = new JTextField(5);
        lParamPanel.add(new JLabel("Outer W (m):")); lParamPanel.add(lOuterWidthField);
        lParamPanel.add(new JLabel("Outer L (m):")); lParamPanel.add(lOuterLengthField);
        lParamPanel.add(new JLabel("Inset W (m):")); lParamPanel.add(lInsetWidthField);
        lParamPanel.add(new JLabel("Inset L (m):")); lParamPanel.add(lInsetLengthField);
        lParamPanel.add(new JLabel("Height (m):")); lParamPanel.add(sharedHeightField); // Reuse shared field
        roomParameterCardsPanel.add(lParamPanel, L_CARD);

        // Panel 4: T-Shaped
        JPanel tParamPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        tParamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tBarWidthField = new JTextField(5);
        tBarLengthField = new JTextField(5);
        tStemWidthField = new JTextField(5);
        tStemLengthField = new JTextField(5);
        tParamPanel.add(new JLabel("Bar W (m):")); tParamPanel.add(tBarWidthField);
        tParamPanel.add(new JLabel("Bar L (m):")); tParamPanel.add(tBarLengthField);
        tParamPanel.add(new JLabel("Stem W (m):")); tParamPanel.add(tStemWidthField);
        tParamPanel.add(new JLabel("Stem L (m):")); tParamPanel.add(tStemLengthField);
        tParamPanel.add(new JLabel("Height (m):")); tParamPanel.add(sharedHeightField); // Reuse shared field
        roomParameterCardsPanel.add(tParamPanel, T_CARD);

        roomPanel.add(roomParameterCardsPanel);
        roomPanel.add(Box.createVerticalStrut(5));

        JButton updateRoomSizeButton = new JButton("Update Dimensions");
        updateRoomSizeButton.addActionListener(e -> handleUpdateRoomSize());
        updateRoomSizeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomPanel.add(updateRoomSizeButton);
        roomPanel.add(Box.createVerticalStrut(10));

        // --- Room Appearance ---
        wallColorButton = new JButton("Set Wall Color");
        wallColorButton.addActionListener(e -> handleSetRoomColor(true));
        floorColorButton = new JButton("Set Floor Color");
        floorColorButton.addActionListener(e -> handleSetRoomColor(false));
        JButton wallTextureButtonLocal = new JButton("Set Wall Texture...");
        wallTextureButtonLocal.addActionListener(e -> handleSetRoomTexture(true));
        JButton floorTextureButtonLocal = new JButton("Set Floor Texture...");
        floorTextureButtonLocal.addActionListener(e -> handleSetRoomTexture(false));

        JPanel appearanceButtonsPanel = new JPanel();
        appearanceButtonsPanel.setLayout(new BoxLayout(appearanceButtonsPanel, BoxLayout.Y_AXIS));
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
        appearanceButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomPanel.add(appearanceButtonsPanel);

        panel.add(roomPanel); panel.add(Box.createVerticalStrut(15));

        // --- Furniture Library Section ---
        JPanel libraryPanel = createSectionPanel("Furniture Library");
        libraryPanel.setLayout(new BoxLayout(libraryPanel, BoxLayout.Y_AXIS));
        furnitureLibraryList = new JList<>(FURNITURE_TYPES);
        furnitureLibraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        furnitureLibraryList.setVisibleRowCount(6);
        furnitureLibraryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = furnitureLibraryList.getSelectedIndex();
                if (idx != -1 && designModel.getSelectedFurniture() == null) {
                    // Update the properties fields when a library item is selected
                    // ONLY if no furniture is currently selected in the scene
                    furnWidthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][0]));
                    furnDepthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][1]));
                    furnHeightField.setText(String.format("%.2f", FURNITURE_DIMS[idx][2]));
                    furnRotationYField.setText("0.0");
                }
            }
        });
        JScrollPane libraryScrollPane = new JScrollPane(furnitureLibraryList);
        libraryScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        libraryScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, libraryScrollPane.getPreferredSize().height * 2));
        libraryPanel.add(libraryScrollPane);
        libraryPanel.add(Box.createVerticalStrut(5));
        JButton addFurnitureButtonLocal = new JButton("Add Selected Furniture");
        addFurnitureButtonLocal.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFurnitureButtonLocal.addActionListener(e -> handleAddFurniture());
        libraryPanel.add(addFurnitureButtonLocal);
        panel.add(libraryPanel); panel.add(Box.createVerticalStrut(15));

        // --- Selected Furniture Section ---
        JPanel furniturePropsPanelOuter = createSectionPanel("Selected Furniture");
        furniturePropsPanelOuter.setLayout(new BoxLayout(furniturePropsPanelOuter, BoxLayout.Y_AXIS));
        furnitureDimsPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Assign to instance variable
        furnitureDimsPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
        furnWidthField = new JTextField("1.00");
        furnDepthField = new JTextField("1.00");
        furnHeightField = new JTextField("1.00");
        furnRotationYField = new JTextField("0.0");
        furnitureDimsPanel.add(new JLabel("Width:")); furnitureDimsPanel.add(furnWidthField);
        furnitureDimsPanel.add(new JLabel("Depth:")); furnitureDimsPanel.add(furnDepthField);
        furnitureDimsPanel.add(new JLabel("Height:")); furnitureDimsPanel.add(furnHeightField);
        furnitureDimsPanel.add(new JLabel("Rotation Y:")); furnitureDimsPanel.add(furnRotationYField);
        furnitureDimsPanel.add(new JLabel("")); // Spacer
        JButton updateFurnDimsButton = new JButton("Update Dimensions");
        updateFurnDimsButton.addActionListener(e -> handleUpdateFurnitureDimensions());
        furnitureDimsPanel.add(updateFurnDimsButton);
        furnitureDimsPanel.add(new JLabel("")); // Spacer
        JButton updateFurnRotationButton = new JButton("Update Rotation");
        updateFurnRotationButton.addActionListener(e -> handleUpdateFurnitureRotation());
        furnitureDimsPanel.add(updateFurnRotationButton);
        furnitureDimsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        furniturePropsPanelOuter.add(furnitureDimsPanel);
        furniturePropsPanelOuter.add(Box.createVerticalStrut(10));

        furnitureAppearancePanel = new JPanel(); // <<< Initialize instance variable
        furnitureAppearancePanel.setLayout(new BoxLayout(furnitureAppearancePanel, BoxLayout.Y_AXIS));
        furnitureAppearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));
        furnitureColorButton = new JButton("Set Furniture Color");
        furnitureColorButton.addActionListener(e -> handleSetFurnitureColor());
        furnitureTextureButton = new JButton("Set Furniture Texture...");
        furnitureTextureButton.addActionListener(e -> handleSetFurnitureTexture());
        furnitureColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        furnitureTextureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        furnitureAppearancePanel.add(furnitureColorButton);
        furnitureAppearancePanel.add(Box.createVerticalStrut(5));
        furnitureAppearancePanel.add(furnitureTextureButton);
        furnitureAppearancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        furniturePropsPanelOuter.add(furnitureAppearancePanel);
        furniturePropsPanelOuter.add(Box.createVerticalStrut(10));
        deleteFurnitureButton = new JButton("Delete Selected Furniture");
        deleteFurnitureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteFurnitureButton.addActionListener(e -> handleDeleteSelectedFurniture());
        furniturePropsPanelOuter.add(deleteFurnitureButton);
        panel.add(furniturePropsPanelOuter); panel.add(Box.createVerticalStrut(15));

        // --- View Controls Section ---
        JPanel viewPanel = createSectionPanel("View Controls");
        viewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        viewModeComboBox = new JComboBox<>(new String[]{"3D View", "2D View (Top Down)"});
        viewModeComboBox.addActionListener(e -> setViewMode(viewModeComboBox.getSelectedIndex() == 0));
        viewPanel.add(new JLabel("Mode:")); viewPanel.add(viewModeComboBox);
        viewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, viewPanel.getPreferredSize().height));
        panel.add(viewPanel);

        panel.add(Box.createVerticalGlue()); // Pushes content to the top

        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(); panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    // This method updates the entire control panel UI based on the current DesignModel state.
    // It's called after loading, clearing, undo/redo, or changing properties.
    private void updateUIFromModel() {
        if (designModel == null || designModel.getRoom() == null || roomShapeComboBox == null) return;
        Room room = designModel.getRoom();

        // -- Update Room Section --

        // Temporarily remove listener to prevent combo box update from re-triggering this method
        // Use the stored listener instance for reliable removal/addition
        if (roomShapeActionListener != null) roomShapeComboBox.removeActionListener(roomShapeActionListener);
        // Set the combo box selection to match the actual shape in the model
        roomShapeComboBox.setSelectedItem(room.getShape());
        // Re-add the listener
        if (roomShapeActionListener != null) roomShapeComboBox.addActionListener(roomShapeActionListener);


        // Update shared height field always
        if (sharedHeightField != null) {
            sharedHeightField.setText(String.format("%.2f", room.getHeight()));
        }

        // Show the correct card panel corresponding to the model's shape
        // NOTE: This should ideally happen *before* updating the fields on the card
        //       if the listener wasn't calling this method itself. Now the order matters less.
        if (roomParameterCardLayout != null && roomParameterCardsPanel != null) {
            String cardToShow = RECT_CARD; // Default
            switch (room.getShape()) {
                case CIRCULAR: cardToShow = CIRC_CARD; break;
                case L_SHAPED: cardToShow = L_CARD; break;
                case T_SHAPED: cardToShow = T_CARD; break;
            }
            roomParameterCardLayout.show(roomParameterCardsPanel, cardToShow);
        }

        // Update the dimension fields on the (now visible) card to match the model
        switch (room.getShape()) {
            case RECTANGULAR:
                if (roomWidthField != null) roomWidthField.setText(String.format("%.2f", room.getWidth()));
                if (roomLengthField != null) roomLengthField.setText(String.format("%.2f", room.getLength()));
                break;
            case CIRCULAR:
                if (roomRadiusField != null) roomRadiusField.setText(String.format("%.2f", room.getRadius()));
                break;
            case L_SHAPED:
                if (lOuterWidthField != null) lOuterWidthField.setText(String.format("%.2f", room.getL_outerWidth()));
                if (lOuterLengthField != null) lOuterLengthField.setText(String.format("%.2f", room.getL_outerLength()));
                if (lInsetWidthField != null) lInsetWidthField.setText(String.format("%.2f", room.getL_insetWidth()));
                if (lInsetLengthField != null) lInsetLengthField.setText(String.format("%.2f", room.getL_insetLength()));
                break;
            case T_SHAPED:
                if (tBarWidthField != null) tBarWidthField.setText(String.format("%.2f", room.getT_barWidth()));
                if (tBarLengthField != null) tBarLengthField.setText(String.format("%.2f", room.getT_barLength()));
                if (tStemWidthField != null) tStemWidthField.setText(String.format("%.2f", room.getT_stemWidth()));
                if (tStemLengthField != null) tStemLengthField.setText(String.format("%.2f", room.getT_stemLength()));
                break;
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

        // -- Update Selected Furniture Section --
        Furniture selected = designModel.getSelectedFurniture();
        boolean furnitureSelected = (selected != null);

        // Enable/Disable furniture property fields and buttons based on selection
        if(furnWidthField != null) furnWidthField.setEnabled(furnitureSelected);
        if(furnDepthField != null) furnDepthField.setEnabled(furnitureSelected);
        if(furnHeightField != null) furnHeightField.setEnabled(furnitureSelected);
        if(furnRotationYField != null) furnRotationYField.setEnabled(furnitureSelected);
        if(furnitureColorButton != null) furnitureColorButton.setEnabled(furnitureSelected);
        if(furnitureDimsPanel != null) furnitureDimsPanel.setEnabled(furnitureSelected); // Also enable the panel itself? Maybe not necessary.
        if(furnitureTextureButton != null) furnitureTextureButton.setEnabled(furnitureSelected);
        if(deleteFurnitureButton != null) deleteFurnitureButton.setEnabled(furnitureSelected);

        // Enable/Disable the container panel and its buttons
        if (furnitureDimsPanel != null) {
            furnitureDimsPanel.setEnabled(furnitureSelected); // Maybe enable/disable panel
            Component[] comps = furnitureDimsPanel.getComponents();
            for(Component c : comps) { // Enable/disable all components within
                if (c instanceof JButton || c instanceof JTextField || c instanceof JLabel) {
                    c.setEnabled(furnitureSelected);
                }
            }
        }
        if (furnitureAppearancePanel != null) { // Also enable/disable appearance controls
            furnitureAppearancePanel.setEnabled(furnitureSelected); // <<< Corrected access
            Component[] comps = furnitureAppearancePanel.getComponents(); // <<< Corrected access
            for(Component c : comps) { // <<< Corrected access
                if (c instanceof JButton || c instanceof JLabel) { // Add other types if needed
                    c.setEnabled(furnitureSelected);
                }
            }
        }


        // Update values if furniture is selected
        if (furnitureSelected) {
            // Only update fields if they don't have focus to prevent overwriting user input
            if (furnWidthField != null && !furnWidthField.hasFocus()) furnWidthField.setText(String.format("%.2f", selected.getWidth()));
            if (furnDepthField != null && !furnDepthField.hasFocus()) furnDepthField.setText(String.format("%.2f", selected.getDepth()));
            if (furnHeightField != null && !furnHeightField.hasFocus()) furnHeightField.setText(String.format("%.2f", selected.getHeight()));
            if (furnRotationYField != null && !furnRotationYField.hasFocus()) furnRotationYField.setText(String.format("%.1f", selected.getRotation().y));
            if(furnitureColorButton != null) {
                furnitureColorButton.setBackground(selected.getColor());
                furnitureColorButton.setForeground(getContrastColor(selected.getColor()));
            }
            // De-select item in library list if furniture is selected in scene
            if (furnitureLibraryList != null) furnitureLibraryList.clearSelection();

        } else {
            // If nothing selected, potentially clear fields or show library default
            if (furnitureLibraryList != null && furnitureLibraryList.getSelectedIndex() == -1) {
                // No library item selected either, clear fields
                if (furnWidthField != null && !furnWidthField.hasFocus()) furnWidthField.setText("");
                if (furnDepthField != null && !furnDepthField.hasFocus()) furnDepthField.setText("");
                if (furnHeightField != null && !furnHeightField.hasFocus()) furnHeightField.setText("");
                if (furnRotationYField != null && !furnRotationYField.hasFocus()) furnRotationYField.setText("");
            } else if (furnitureLibraryList != null) {
                // A library item is selected, show its default dims
                int idx = furnitureLibraryList.getSelectedIndex();
                if (idx != -1) {
                    if (furnWidthField != null && !furnWidthField.hasFocus()) furnWidthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][0]));
                    if (furnDepthField != null && !furnDepthField.hasFocus()) furnDepthField.setText(String.format("%.2f", FURNITURE_DIMS[idx][1]));
                    if (furnHeightField != null && !furnHeightField.hasFocus()) furnHeightField.setText(String.format("%.2f", FURNITURE_DIMS[idx][2]));
                    if (furnRotationYField != null && !furnRotationYField.hasFocus()) furnRotationYField.setText("0.0");
                }
            }
            // Reset furniture color button appearance when nothing selected
            if(furnitureColorButton != null) {
                furnitureColorButton.setBackground(UIManager.getColor("Button.background"));
                furnitureColorButton.setForeground(UIManager.getColor("Button.foreground"));
            }
        }

        // Update Edit menu item enablement
        JMenuBar mb = getJMenuBar();
        if (mb != null) {
            try {
                JMenu editMenu = mb.getMenu(1);
                if (editMenu != null && editMenu.getItemCount() > 3) {
                    editMenu.getItem(3).setEnabled(furnitureSelected); // Enable/disable "Delete Selected Furniture" menu item
                }
            } catch (Exception e) {
                // Handle potential index out of bounds or null pointers gracefully
                System.err.println("Error updating Edit menu enablement: " + e.getMessage());
            }
        }
    }


    private Color getContrastColor(Color background) {
        if (background == null) return Color.BLACK;
        double y = (299 * background.getRed() + 587 * background.getGreen() + 114 * background.getBlue()) / 1000.0;
        return (y >= 128) ? Color.BLACK : Color.WHITE;
    }

    private void updateUndoRedoState() {
        undoAction.setEnabled(undoManager.canUndo()); undoAction.putValue(Action.NAME, undoManager.getUndoPresentationName());
        redoAction.setEnabled(undoManager.canRedo()); redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());
    }

    private void setViewMode(boolean is3D) {
        if (renderer == null) return; renderer.set3DMode(is3D);
        if (viewModeComboBox != null) viewModeComboBox.setSelectedIndex(is3D ? 0 : 1);
        JMenuBar mb = getJMenuBar(); if (mb != null) { try { JMenu viewMenu = mb.getMenu(2); if (viewMenu != null && viewMenu.getItemCount() > 1) { ((JRadioButtonMenuItem)viewMenu.getItem(0)).setSelected(!is3D); ((JRadioButtonMenuItem)viewMenu.getItem(1)).setSelected(is3D); } } catch(Exception e) {} }
        designCanvas.repaint();
    }

    // --- Mouse Interaction ---
    private void setupMouseInteraction() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                designCanvas.requestFocusInWindow(); // Request focus

                if (SwingUtilities.isRightMouseButton(e)) {
                    // Explicitly ensure furniture dragging is off
                    isDraggingFurniture = false;
                    draggedFurniture = null;
                    isDraggingCamera = true;
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    Furniture pickedFurniture = renderer.pickFurniture(e.getX(), e.getY());

                    if (pickedFurniture != null) { // Clicked ON existing furniture
                        isDraggingCamera = false; // Ensure camera drag is off
                        if (designModel.getSelectedFurniture() != pickedFurniture) {
                            // Finalize any previous keyboard move before selecting new furniture
                            finalizeKeyboardMove();
                            designModel.setSelectedFurniture(pickedFurniture);
                            updateUIFromModel(); // Update UI to show selected furniture's properties
                        }
                        isDraggingFurniture = true;
                        draggedFurniture = pickedFurniture;
                        // Prepare for UNDO: Store initial position *before* drag starts
                        dragStartPosition = draggedFurniture.getPosition().clone();

                        // Calculate offset from furniture origin to click point ON THE FLOOR
                        Vector3f clickFloorPos = renderer.screenToWorldFloor(e.getX(), e.getY());
                        if (clickFloorPos != null) {
                            dragOffset = new Vector3f(
                                    clickFloorPos.x - draggedFurniture.getPosition().x,
                                    0, // We are dragging on the floor plane
                                    clickFloorPos.z - draggedFurniture.getPosition().z
                            );
                        } else {
                            dragOffset = new Vector3f(0, 0, 0); // Fallback
                        }

                    } else { // Clicked on EMPTY space
                        isDraggingCamera = false; // Ensure camera drag is off
                        if (designModel.getSelectedFurniture() != null) {
                            finalizeKeyboardMove(); // Finalize move before deselecting
                            designModel.setSelectedFurniture(null); // Deselect furniture
                            updateUIFromModel();                  // Update UI to reflect deselection
                            designCanvas.repaint();               // Redraw to remove selection indicator
                        }
                        // If SHIFT is held, treat left-click drag as pan (like middle mouse)
                        if (e.isShiftDown()) {
                            isDraggingFurniture = false;
                            draggedFurniture = null;
                            isDraggingCamera = true; // Allow panning with Shift+Left Drag
                        } else {
                            isDraggingCamera = false;
                            isDraggingFurniture = false; // Ensure furniture drag is off
                        }
                    }
                } else if (SwingUtilities.isMiddleMouseButton(e)){ // Middle mouse always pans
                    isDraggingFurniture = false;
                    draggedFurniture = null;
                    isDraggingCamera = true;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint == null) return;

                // Additional check: ensure only one drag type is active
                if (isDraggingCamera && isDraggingFurniture) {
                    System.err.println("Warning: Both camera and furniture drag flags are true!");
                    isDraggingFurniture = false; // Prioritize camera? Or reset both? Reset furniture seems safer.
                }

                float deltaX = e.getX() - lastMousePoint.x;
                float deltaY = e.getY() - lastMousePoint.y;

                if (isDraggingCamera) {
                    // Use Shift+Left-Mouse or Middle-Mouse for Panning, Right-Mouse alone for Rotation
                    // Check Shift status dynamically during drag
                    if (e.isShiftDown() || SwingUtilities.isMiddleMouseButton(e)) {
                        renderer.panCamera(deltaX, deltaY);
                    } else if (SwingUtilities.isRightMouseButton(e)) { // Only rotate if right button is down (and not shift)
                        renderer.rotateCamera(deltaX, deltaY);
                    }
                    // If it's a left-drag that started with shift, it should continue panning
                    // The initial press sets the mode (camera/furniture)
                } else if (isDraggingFurniture && draggedFurniture != null) {
                    Vector3f floorPos = renderer.screenToWorldFloor(e.getX(), e.getY());
                    if (floorPos != null) {
                        float newX = floorPos.x - dragOffset.x;
                        float newZ = floorPos.z - dragOffset.z;
                        // TODO: Add boundary checks to prevent dragging outside the room
                        draggedFurniture.setPosition(new Vector3f(newX, draggedFurniture.getPosition().y, newZ));
                    }
                }

                lastMousePoint = e.getPoint();
                designCanvas.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Finalize keyboard move first, if any was in progress
                finalizeKeyboardMove();

                // Finalize furniture drag move for undo if needed
                if (isDraggingFurniture && draggedFurniture != null && dragStartPosition != null) {
                    // Check if position actually changed significantly before adding undo edit
                    if (!draggedFurniture.getPosition().equals(dragStartPosition)) {
                        registerUndoableEdit(new MoveFurnitureEdit(draggedFurniture, dragStartPosition, draggedFurniture.getPosition()));
                    }
                    // Update UI in case position didn't change significantly but selection remains
                    updateUIFromModel();
                } else {
                    // Clear start position even if no move happened, to be safe
                    dragStartPosition = null;
                }

                // Reset all flags and temporary state variables
                isDraggingCamera = false;
                isDraggingFurniture = false;
                draggedFurniture = null;
                dragOffset = null;
                lastMousePoint = null;
                dragStartPosition = null; // Clear again just to be sure

                updateUndoRedoState(); // Update undo/redo state after potential move
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                finalizeKeyboardMove(); // Stop keyboard move on zoom
                float delta = -e.getWheelRotation(); // Negative because wheel down should zoom out (increase distance)
                renderer.zoomCamera(delta);
                designCanvas.repaint();
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
        // Add a FocusListener to finalize keyboard move if focus is lost
        designCanvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finalizeKeyboardMove();
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return; // Only move selected furniture

        int keyCode = e.getKeyCode();

        // Check for arrow keys
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
        {
            if (!isMovingWithKeyboard) { // Start of a keyboard move sequence
                isMovingWithKeyboard = true;
                keyboardMoveStartPosition = selected.getPosition().clone(); // Record starting position for undo
            }
            pressedKeys.add(keyCode); // Add key to the set of currently pressed keys

            // Calculate cumulative movement based on currently pressed keys
            float dx = 0, dz = 0;
            if (pressedKeys.contains(KeyEvent.VK_UP)) dz -= KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_DOWN)) dz += KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_LEFT)) dx -= KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) dx += KEYBOARD_MOVE_STEP;

            // Apply movement relative to current position for continuous movement feel
            Vector3f currentPos = selected.getPosition();
            float newX = currentPos.x + dx;
            float newZ = currentPos.z + dz;


            // TODO: Add boundary checks here based on room shape and furniture size
            // Example (simple rectangular boundary - NEEDS IMPROVEMENT for other shapes):
            // float halfW = selected.getWidth() / 2f;
            // float halfD = selected.getDepth() / 2f;
            // Room room = designModel.getRoom(); // Get current room
            // if (room != null && room.getShape() == Room.RoomShape.RECTANGULAR) {
            //     if (newX - halfW < 0) newX = halfW;
            //     if (newX + halfW > room.getWidth()) newX = room.getWidth() - halfW;
            //     if (newZ - halfD < 0) newZ = halfD;
            //     if (newZ + halfD > room.getLength()) newZ = room.getLength() - halfD;
            // } // else: Implement checks for other shapes

            selected.setPosition(new Vector3f(newX, currentPos.y, newZ));
            designCanvas.repaint();

        } else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
            handleDeleteSelectedFurniture();
        }
        // Potentially handle other keys like Escape to cancel a move?
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            cancelKeyboardMove();
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        int keyCode = e.getKeyCode();
        // Only finalize if the released key was relevant (an arrow key)
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
        {
            pressedKeys.remove(keyCode); // Remove key from the set

            // If this was the last arrow key being released, finalize the move
            if (isMovingWithKeyboard && pressedKeys.isEmpty()) {
                finalizeKeyboardMove();
            }
        }
    }

    // Called when the keyboard move sequence ends (last arrow key released or focus lost/mouse click etc.)
    private void finalizeKeyboardMove() {
        if (isMovingWithKeyboard && keyboardMoveStartPosition != null) {
            Furniture selected = designModel.getSelectedFurniture();
            // Check if selected furniture still exists and if position changed
            if (selected != null && !selected.getPosition().equals(keyboardMoveStartPosition)) {
                // Register the completed move as a single undoable edit
                registerUndoableEdit(new MoveFurnitureEdit(selected, keyboardMoveStartPosition, selected.getPosition()));
                updateUIFromModel(); // Update UI in case position changed slightly
            }
            isMovingWithKeyboard = false;
            keyboardMoveStartPosition = null;
            updateUndoRedoState(); // Update undo state regardless of whether position changed
        }
        pressedKeys.clear(); // Clear any potentially stuck keys
    }

    // Method to cancel an ongoing keyboard move without saving an undo edit
    private void cancelKeyboardMove() {
        if (isMovingWithKeyboard && keyboardMoveStartPosition != null) {
            Furniture selected = designModel.getSelectedFurniture();
            if (selected != null) {
                selected.setPosition(keyboardMoveStartPosition); // Revert to start position
            }
            isMovingWithKeyboard = false;
            keyboardMoveStartPosition = null;
            pressedKeys.clear();
            designCanvas.repaint(); // Redraw with reverted position
            System.out.println("Keyboard move cancelled.");
        }
    }


    // --- Action Handlers ---
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
        designModel.clearDesign();
        undoManager.discardAllEdits();
        renderer.setDesignModel(designModel); // Re-set the model in the renderer
        // Camera should be reset by updateCameraForModel called within setDesignModel
        updateUIFromModel();
        updateUndoRedoState();
        designCanvas.repaint();
    }


    private void handleLoadDesign() {
        finalizeKeyboardMove(); // Finalize any pending move
        JFileChooser fc = new JFileChooser("./designs"); // Start in designs subfolder
        fc.setDialogTitle("Open Design File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Furniture Design Files (*.furn)", "furn");
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                DesignModel loadedModel = (DesignModel) ois.readObject();
                if (loadedModel != null) {
                    designModel = loadedModel;
                    renderer.setDesignModel(designModel); // Update renderer with new model
                    undoManager.discardAllEdits();
                    updateUIFromModel();
                    updateUndoRedoState();
                    designCanvas.repaint();
                    JOptionPane.showMessageDialog(this,"Design loaded successfully from " + file.getName(),"Load Successful", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (FileNotFoundException fnf) {
                JOptionPane.showMessageDialog(this,"Error: File not found.\n" + fnf.getMessage(),"Load Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException | ClassNotFoundException | ClassCastException ex) {
                JOptionPane.showMessageDialog(this,"Error loading design file:\n" + ex.getMessage(),"Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSaveDesign() {
        finalizeKeyboardMove(); // Finalize any pending move
        JFileChooser fc = new JFileChooser("./designs"); // Start in designs subfolder
        fc.setDialogTitle("Save Design File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Furniture Design Files (*.furn)", "furn");
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            // Ensure the file has the .furn extension
            if (!file.getName().toLowerCase().endsWith(".furn")) {
                file = new File(file.getParentFile(), file.getName() + ".furn");
            }
            // Confirm overwrite
            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?", "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite == JOptionPane.NO_OPTION) {
                    return; // Abort save
                }
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(designModel);
                JOptionPane.showMessageDialog(this,"Design saved successfully to " + file.getName(),"Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,"Error saving design file:\n" + ex.getMessage(),"Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAddFurniture() {
        finalizeKeyboardMove(); // Finalize any pending move
        int selectedIndex = furnitureLibraryList.getSelectedIndex();
        if (selectedIndex != -1) {
            String type = FURNITURE_TYPES[selectedIndex];
            try {
                // Use current values from text fields if user edited them, otherwise library defaults
                float w = FURNITURE_DIMS[selectedIndex][0]; try { w = Float.parseFloat(furnWidthField.getText()); } catch (NumberFormatException | NullPointerException ignored) {}
                float d = FURNITURE_DIMS[selectedIndex][1]; try { d = Float.parseFloat(furnDepthField.getText()); } catch (NumberFormatException | NullPointerException ignored) {}
                float h = FURNITURE_DIMS[selectedIndex][2]; try { h = Float.parseFloat(furnHeightField.getText()); } catch (NumberFormatException | NullPointerException ignored) {}
                if (w <= 0 || d <= 0 || h <= 0) throw new NumberFormatException("Dimensions must be positive");

                // Use room center for initial position instead of camera target
                Vector3f initialPos = designModel.getRoom().calculateCenter();
                if (initialPos == null) initialPos = new Vector3f(2.5f, 0, 2.5f); // Fallback
                initialPos.y = 0; // Place on floor

                Furniture newFurniture = new Furniture(type, initialPos, w, d, h);
                designModel.addFurniture(newFurniture); // Adds and selects
                registerUndoableEdit(new AddFurnitureEdit(newFurniture));
                updateUIFromModel(); // Reflect selection and properties
                updateUndoRedoState();
                designCanvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,"Invalid dimensions specified for new furniture. Using defaults.","Input Error", JOptionPane.WARNING_MESSAGE);
                // Try adding with default dimensions
                try {
                    float w = FURNITURE_DIMS[selectedIndex][0];
                    float d = FURNITURE_DIMS[selectedIndex][1];
                    float h = FURNITURE_DIMS[selectedIndex][2];
                    Vector3f initialPos = designModel.getRoom().calculateCenter();
                    if (initialPos == null) initialPos = new Vector3f(2.5f, 0, 2.5f);
                    initialPos.y = 0;
                    Furniture newFurniture = new Furniture(type, initialPos, w, d, h);
                    designModel.addFurniture(newFurniture);
                    registerUndoableEdit(new AddFurnitureEdit(newFurniture));
                    updateUIFromModel(); updateUndoRedoState(); designCanvas.repaint();
                } catch (Exception finalEx) { // Catch any other error during default add
                    JOptionPane.showMessageDialog(this,"Failed to add furniture: " + finalEx.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a furniture type from the library.", "No Furniture Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteSelectedFurniture() {
        finalizeKeyboardMove(); // Finalize any pending move
        Furniture selected = designModel.getSelectedFurniture();
        if (selected != null) {
            designModel.removeFurniture(selected); // Removes and deselects
            registerUndoableEdit(new RemoveFurnitureEdit(selected));
            updateUIFromModel();
            updateUndoRedoState();
            designCanvas.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "No furniture selected to delete.", "Delete Furniture", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleUpdateRoomSize() {
        finalizeKeyboardMove();
        Room room = designModel.getRoom();
        if (room == null) return;

        try { // START TRY BLOCK
            // Get selected shape from COMBO BOX (user's current selection)
            Room.RoomShape selectedShape = (Room.RoomShape) roomShapeComboBox.getSelectedItem();
            if (selectedShape == null) return;
            float h = Float.parseFloat(sharedHeightField.getText());
            if (h <= 0) throw new NumberFormatException("Height must be positive");

            // Prepare variables to hold new dimensions, initialize with current model values FOR COMPARISON
            float currentW = room.getWidth(), currentL = room.getLength();
            float currentR = room.getRadius();
            float currentLoW = room.getL_outerWidth(), currentLoL = room.getL_outerLength();
            float currentLiW = room.getL_insetWidth(), currentLiL = room.getL_insetLength();
            float currentTbW = room.getT_barWidth(), currentTbL = room.getT_barLength();
            float currentTsW = room.getT_stemWidth(), currentTsL = room.getT_stemLength();
            float currentH = room.getHeight();
            Room.RoomShape currentShape = room.getShape();

            // Variables to store the values READ FROM THE UI
            float w = currentW, l = currentL, r = currentR; // Initialize with current values
            float loW = currentLoW, loL = currentLoL, liW = currentLiW, liL = currentLiL;
            float tbW = currentTbW, tbL = currentTbL, tsW = currentTsW, tsL = currentTsL;

            boolean changed = false;

            // Read specific parameters based on selected shape from UI
            switch (selectedShape) {
                case RECTANGULAR:
                    w = Float.parseFloat(roomWidthField.getText());
                    l = Float.parseFloat(roomLengthField.getText());
                    if (w <= 0 || l <= 0) throw new NumberFormatException("Rectangular dimensions must be positive");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > 1e-3 ||
                            Math.abs(w - currentW) > 1e-3 || Math.abs(l - currentL) > 1e-3;
                    break;
                case CIRCULAR:
                    r = Float.parseFloat(roomRadiusField.getText());
                    if (r <= 0) throw new NumberFormatException("Radius must be positive");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > 1e-3 ||
                            Math.abs(r - currentR) > 1e-3;
                    break;
                case L_SHAPED:
                    loW = Float.parseFloat(lOuterWidthField.getText());
                    loL = Float.parseFloat(lOuterLengthField.getText());
                    liW = Float.parseFloat(lInsetWidthField.getText());
                    liL = Float.parseFloat(lInsetLengthField.getText());
                    if (loW <= 0 || loL <= 0 || liW <= 0 || liL <= 0 || liW >= loW || liL >= loL)
                        throw new NumberFormatException("Invalid L-Shape dimensions (insets must be smaller than outer)");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > 1e-3 ||
                            Math.abs(loW - currentLoW) > 1e-3 || Math.abs(loL - currentLoL) > 1e-3 ||
                            Math.abs(liW - currentLiW) > 1e-3 || Math.abs(liL - currentLiL) > 1e-3;
                    break;
                case T_SHAPED:
                    tbW = Float.parseFloat(tBarWidthField.getText());
                    tbL = Float.parseFloat(tBarLengthField.getText());
                    tsW = Float.parseFloat(tStemWidthField.getText());
                    tsL = Float.parseFloat(tStemLengthField.getText());
                    if (tbW <= 0 || tbL <= 0 || tsW <= 0 || tsL <= 0 || tsW > tbW)
                        throw new NumberFormatException("Invalid T-Shape dimensions (stem width <= bar width)");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > 1e-3 ||
                            Math.abs(tbW - currentTbW) > 1e-3 || Math.abs(tbL - currentTbL) > 1e-3 ||
                            Math.abs(tsW - currentTsW) > 1e-3 || Math.abs(tsL - currentTsL) > 1e-3;
                    break;
            } // END SWITCH

            // If anything changed, apply and register undo
            if (changed) {
                // The constructor of the Edit applies the change immediately using the values read from UI
                registerUndoableEdit(new ChangeRoomPropertiesEdit(room, selectedShape, h, w, l, r, loW, loL, liW, liL, tbW, tbL, tsW, tsL));
                designCanvas.repaint();
                if(renderer != null) {
                    renderer.getCameraManager().resetTargetToCenter(designModel.getRoom()); // Reset camera target
                    renderer.updateCameraForModel(); // Update camera distance/settings
                }
                updateUIFromModel(); // Refresh UI controls TO MATCH THE NEW MODEL STATE
                return; // Exit after successful update
            } // END IF (changed)

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format or dimensions:\n" + ex.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            updateUIFromModel(); // Reset UI to current valid model state
        } // END CATCH / END TRY
    } // END handleUpdateRoomSize METHOD


    private void handleUpdateFurnitureDimensions() {
        finalizeKeyboardMove(); // Finalize any pending move
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        try {
            float newWidth = Float.parseFloat(furnWidthField.getText());
            float newDepth = Float.parseFloat(furnDepthField.getText());
            float newHeight = Float.parseFloat(furnHeightField.getText());
            if (newWidth <= 0 || newDepth <= 0 || newHeight <= 0) {
                throw new NumberFormatException("Dimensions must be positive.");
            }
            // Check if dimensions actually changed
            if (Math.abs(newWidth - selected.getWidth()) > 1e-3 ||
                    Math.abs(newDepth - selected.getDepth()) > 1e-3 ||
                    Math.abs(newHeight - selected.getHeight()) > 1e-3)
            {
                registerUndoableEdit(new ChangeFurnitureDimensionsEdit(selected, newWidth, newDepth, newHeight));
                designCanvas.repaint();
                updateUIFromModel(); // Update UI to reflect changes
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid dimensions: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            updateUIFromModel(); // Reset UI fields to current model state
        }
    }
    private void handleUpdateFurnitureRotation() {
        finalizeKeyboardMove(); // Finalize any pending move
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        try {
            float newRotationY = Float.parseFloat(furnRotationYField.getText());
            // Check if rotation actually changed
            if (Math.abs(newRotationY - selected.getRotation().y) > 1e-2) {
                registerUndoableEdit(new ChangeFurnitureRotationEdit(selected, newRotationY));
                designCanvas.repaint();
                updateUIFromModel(); // Update UI to reflect changes
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid rotation angle.", "Input Error", JOptionPane.ERROR_MESSAGE);
            updateUIFromModel(); // Reset UI field to current model state
        }
    }

    private void handleSetRoomColor(boolean isWall) {
        finalizeKeyboardMove(); // Finalize any pending move
        Room room = designModel.getRoom();
        Color initialColor = isWall ? room.getWallColor() : room.getFloorColor();
        Color newColor = JColorChooser.showDialog(this, "Select " + (isWall ? "Wall" : "Floor") + " Color", initialColor);
        if (newColor != null && !newColor.equals(initialColor)) {
            registerUndoableEdit(new ChangeRoomAppearanceEdit(room, isWall, newColor, null));
            designCanvas.repaint();
            updateUIFromModel(); // Update button background
        }
    }
    private void handleSetFurnitureColor() {
        finalizeKeyboardMove(); // Finalize any pending move
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        Color initialColor = selected.getColor();
        Color newColor = JColorChooser.showDialog(this, "Select Furniture Color", initialColor);
        if (newColor != null && !newColor.equals(initialColor)) {
            registerUndoableEdit(new ChangeFurnitureAppearanceEdit(selected, newColor, selected.getTexturePath()));
            designCanvas.repaint();
            updateUIFromModel(); // Update button background
        }
    }

    private void handleSetRoomTexture(boolean isWall) {
        finalizeKeyboardMove(); // Finalize any pending move
        Room room = designModel.getRoom();
        String oldTexturePath = isWall ? room.getWallTexturePath() : room.getFloorTexturePath();
        String newTexturePath = selectTextureFile(); // Allow selecting a file

        // Allow clearing the texture
        int choice = JOptionPane.NO_OPTION; // Default to assuming a file was chosen or cancel
        if (newTexturePath == null) { // If user cancelled file chooser
            choice = JOptionPane.showConfirmDialog(this,
                    "Clear existing " + (isWall ? "wall" : "floor") + " texture?",
                    "Clear Texture", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                newTexturePath = ""; // Use empty string to signify no texture
            } else {
                return; // User cancelled both file chooser and clear confirmation
            }
        }
        // Check if texture actually changed (comparing paths, handle null/empty)
        boolean pathChanged = false;
        if (oldTexturePath == null && (newTexturePath != null && !newTexturePath.isEmpty())) pathChanged = true;
        else if (oldTexturePath != null && !oldTexturePath.equals(newTexturePath)) pathChanged = true;
        // else if (oldTexturePath != null && oldTexturePath.isEmpty() && (newTexturePath != null && !newTexturePath.isEmpty())) pathChanged = true; // Redundant
        // else if (oldTexturePath != null && !oldTexturePath.isEmpty() && (newTexturePath == null || newTexturePath.isEmpty())) pathChanged = true; // Covered by !equals


        if (pathChanged) {
            registerUndoableEdit(new ChangeRoomAppearanceEdit(room, isWall, null, newTexturePath));
            designCanvas.repaint();
            // No direct UI update needed for texture path itself, maybe a label later
        }
    }
    private void handleSetFurnitureTexture() {
        finalizeKeyboardMove(); // Finalize any pending move
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        String oldTexturePath = selected.getTexturePath();
        String newTexturePath = selectTextureFile();

        // Allow clearing the texture
        int choice = JOptionPane.NO_OPTION;
        if (newTexturePath == null) {
            choice = JOptionPane.showConfirmDialog(this,"Clear existing furniture texture?","Clear Texture", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) { newTexturePath = ""; }
            else { return; }
        }
        // Check if texture actually changed
        boolean pathChanged = false;
        if (oldTexturePath == null && (newTexturePath != null && !newTexturePath.isEmpty())) pathChanged = true;
        else if (oldTexturePath != null && !oldTexturePath.equals(newTexturePath)) pathChanged = true;
        // else if (oldTexturePath != null && oldTexturePath.isEmpty() && (newTexturePath != null && !newTexturePath.isEmpty())) pathChanged = true; // Redundant
        // else if (oldTexturePath != null && !oldTexturePath.isEmpty() && (newTexturePath == null || newTexturePath.isEmpty())) pathChanged = true; // Covered by !equals

        if (pathChanged) {
            registerUndoableEdit(new ChangeFurnitureAppearanceEdit(selected, selected.getColor(), newTexturePath));
            designCanvas.repaint();
            // No direct UI update needed for texture path itself
        }
    }

    // --- CORRECTED selectTextureFile ---
    private String selectTextureFile() {
        File textureDir = new File("./textures");
        if (!textureDir.exists()) {
            try { textureDir.mkdirs(); } catch (SecurityException se) { System.err.println("Warning: Failed to create ./textures directory: " + se.getMessage()); }
        }
        JFileChooser fc = new JFileChooser(textureDir.exists() && textureDir.isDirectory() ? textureDir : null);
        fc.setDialogTitle("Select Texture Image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (png, jpg, bmp, gif)", "png", "jpg", "jpeg", "bmp", "gif");
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            return (selectedFile != null) ? selectedFile.getAbsolutePath() : null;
        } else {
            return null; // Return null if cancelled
        }
    }

    private void handleExit() {
        finalizeKeyboardMove(); // Finalize any pending move
        int choice = JOptionPane.showConfirmDialog(this,
                "Exit the application? Unsaved changes will be lost.",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            animator.stop(); // Stop animator before disposing
            dispose();       // Close the window
            System.exit(0);  // Terminate the application
        }
    }


    // --- Undo/Redo Infrastructure ---
    private void registerUndoableEdit(UndoableEdit edit) {
        if (edit != null) {
            undoManager.addEdit(edit);
            updateUndoRedoState();
        }
    }

    // --- Undoable Edit Classes ---
    // (AddFurnitureEdit, RemoveFurnitureEdit, MoveFurnitureEdit, ChangeFurnitureDimensionsEdit,
    //  ChangeFurnitureRotationEdit, ChangeFurnitureAppearanceEdit, ChangeRoomAppearanceEdit,
    //  ChangeRoomPropertiesEdit remain the same as the previous correct version)
    private class AddFurnitureEdit extends AbstractUndoableEdit {
        private final Furniture addedFurniture;
        public AddFurnitureEdit(Furniture f) { this.addedFurniture = f; }
        @Override public String getPresentationName() { return "Add " + addedFurniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); designModel.removeFurniture(addedFurniture); }
        @Override public void redo() throws CannotRedoException { super.redo(); designModel.addFurniture(addedFurniture); }
    }
    private class RemoveFurnitureEdit extends AbstractUndoableEdit {
        private final Furniture removedFurniture;
        public RemoveFurnitureEdit(Furniture f) { this.removedFurniture = f; }
        @Override public String getPresentationName() { return "Remove " + removedFurniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); designModel.addFurniture(removedFurniture); } // Adds AND selects
        @Override public void redo() throws CannotRedoException { super.redo(); designModel.removeFurniture(removedFurniture); } // Removes AND deselects
    }
    private class MoveFurnitureEdit extends AbstractUndoableEdit {
        private final Furniture movedFurniture; private final Vector3f oldPos, newPos;
        public MoveFurnitureEdit(Furniture f, Vector3f oldP, Vector3f newP) {
            this.movedFurniture = f; this.oldPos = oldP.clone(); this.newPos = newP.clone(); }
        @Override public String getPresentationName() { return "Move " + movedFurniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); movedFurniture.setPosition(oldPos); designModel.setSelectedFurniture(movedFurniture); }
        @Override public void redo() throws CannotRedoException { super.redo(); movedFurniture.setPosition(newPos); designModel.setSelectedFurniture(movedFurniture); }
    }
    private class ChangeFurnitureDimensionsEdit extends AbstractUndoableEdit {
        private final Furniture furniture; private final float oldW, oldD, oldH, newW, newD, newH;
        public ChangeFurnitureDimensionsEdit(Furniture f, float nw, float nd, float nh) {
            this.furniture = f;
            this.oldW = f.getWidth(); this.oldD = f.getDepth(); this.oldH = f.getHeight();
            this.newW = nw; this.newD = nd; this.newH = nh;
            apply(newW, newD, newH); // Apply immediately
        }
        private void apply(float w, float d, float h) { furniture.setWidth(w); furniture.setDepth(d); furniture.setHeight(h); }
        @Override public String getPresentationName() { return "Resize " + furniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldW, oldD, oldH); designModel.setSelectedFurniture(furniture); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newW, newD, newH); designModel.setSelectedFurniture(furniture); }
    }
    private class ChangeFurnitureRotationEdit extends AbstractUndoableEdit {
        private final Furniture furniture; private final float oldRotY, newRotY;
        public ChangeFurnitureRotationEdit(Furniture f, float newRot) {
            this.furniture = f; this.oldRotY = f.getRotation().y; this.newRotY = newRot;
            apply(newRotY); // Apply immediately
        }
        private void apply(float rotY) { furniture.getRotation().y = rotY; }
        @Override public String getPresentationName() { return "Rotate " + furniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldRotY); designModel.setSelectedFurniture(furniture); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newRotY); designModel.setSelectedFurniture(furniture); }
    }
    private class ChangeFurnitureAppearanceEdit extends AbstractUndoableEdit {
        private final Furniture furniture; private final Color oldColor, newColor; private final String oldTexture, newTexture;
        public ChangeFurnitureAppearanceEdit(Furniture f, Color c, String t) {
            this.furniture = f;
            this.oldColor = f.getColor(); this.oldTexture = f.getTexturePath();
            this.newColor = c; this.newTexture = t;
            apply(newColor, newTexture); // Apply immediately
        }
        private void apply(Color c, String t) { furniture.setColor(c); furniture.setTexturePath(t); }
        @Override public String getPresentationName() { return "Change " + furniture.getType() + " Appearance"; }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldColor, oldTexture); designModel.setSelectedFurniture(furniture); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newColor, newTexture); designModel.setSelectedFurniture(furniture); }
    }
    private class ChangeRoomAppearanceEdit extends AbstractUndoableEdit {
        private final Room room; private final boolean isWall;
        private final Color oldColor, newColor; private final String oldTexture, newTexture;
        public ChangeRoomAppearanceEdit(Room r, boolean wall, Color c, String t) {
            this.room = r; this.isWall = wall;
            this.oldColor = wall ? r.getWallColor() : r.getFloorColor();
            this.oldTexture = wall ? r.getWallTexturePath() : r.getFloorTexturePath();
            // Only one of color or texture should be non-null from the caller
            this.newColor = c; this.newTexture = t;
            apply(newColor, newTexture); // Apply immediately
        }
        private void apply(Color c, String t) {
            if (isWall) { if (c != null) room.setWallColor(c); if (t != null) room.setWallTexturePath(t); }
            else { if (c != null) room.setFloorColor(c); if (t != null) room.setFloorTexturePath(t); }
        }
        @Override public String getPresentationName() { return "Change " + (isWall ? "Wall" : "Floor") + " Appearance"; }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldColor, oldTexture); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newColor, newTexture); }
    }
    private class ChangeRoomPropertiesEdit extends AbstractUndoableEdit {
        private final Room room;
        private final Room.RoomShape oldShape, newShape;
        private final float oldH, newH;
        private final float oldW, newW, oldL, newL; private final float oldR, newR;
        private final float oldLoW, newLoW, oldLoL, newLoL, oldLiW, newLiW, oldLiL, newLiL;
        private final float oldTbW, newTbW, oldTbL, newTbL, oldTsW, newTsW, oldTsL, newTsL;
        public ChangeRoomPropertiesEdit(Room r, Room.RoomShape shape, float h, float w, float l, float rad, float loW, float loL, float liW, float liL, float tbW, float tbL, float tsW, float tsL) {
            this.room = r;
            // Store state *before* change
            this.oldShape = r.getShape(); this.oldH = r.getHeight(); this.oldW = r.getWidth(); this.oldL = r.getLength(); this.oldR = r.getRadius();
            this.oldLoW = r.getL_outerWidth(); this.oldLoL = r.getL_outerLength(); this.oldLiW = r.getL_insetWidth(); this.oldLiL = r.getL_insetLength();
            this.oldTbW = r.getT_barWidth(); this.oldTbL = r.getT_barLength(); this.oldTsW = r.getT_stemWidth(); this.oldTsL = r.getT_stemLength();
            // Store new state from parameters
            this.newShape = shape; this.newH = h; this.newW = w; this.newL = l; this.newR = rad;
            this.newLoW = loW; this.newLoL = loL; this.newLiW = liW; this.newLiL = liL;
            this.newTbW = tbW; this.newTbL = tbL; this.newTsW = tsW; this.newTsL = tsL;
            // Apply the new state immediately (as part of the action that creates the edit)
            applyProperties(newShape, newH, newW, newL, newR, newLoW, newLoL, newLiW, newLiL, newTbW, newTbL, newTsW, newTsL);
        }
        private void applyProperties(Room.RoomShape shape, float h, float w, float l, float rad, float loW, float loL, float liW, float liL, float tbW, float tbL, float tsW, float tsL) {
            room.setShape(shape); room.setHeight(h);
            switch(shape) {
                case RECTANGULAR: room.setWidth(w); room.setLength(l); break;
                case CIRCULAR: room.setRadius(rad); break;
                case L_SHAPED: room.setL_outerWidth(loW); room.setL_outerLength(loL); room.setL_insetWidth(liW); room.setL_insetLength(liL); break;
                case T_SHAPED: room.setT_barWidth(tbW); room.setT_barLength(tbL); room.setT_stemWidth(tsW); room.setT_stemLength(tsL); break;
            }
        }
        @Override public String getPresentationName() { return "Change Room Shape/Size"; }
        @Override public void undo() throws CannotUndoException { super.undo(); applyProperties(oldShape, oldH, oldW, oldL, oldR, oldLoW, oldLoL, oldLiW, oldLiL, oldTbW, oldTbL, oldTsW, oldTsL); afterChange(); }
        @Override public void redo() throws CannotRedoException { super.redo(); applyProperties(newShape, newH, newW, newL, newR, newLoW, newLoL, newLiW, newLiL, newTbW, newTbL, newTsW, newTsL); afterChange(); }
        private void afterChange() { designCanvas.repaint(); updateUIFromModel(); if(renderer != null && designModel.getRoom() != null) { renderer.getCameraManager().resetTargetToCenter(designModel.getRoom()); renderer.updateCameraForModel(); } }
    }

}