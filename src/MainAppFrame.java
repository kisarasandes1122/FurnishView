// START OF MODIFIED MainAppFrame.java

import javax.swing.*;
import javax.swing.border.Border; // Keep this
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.AbstractUndoableEdit; // **** ADDED ****
import javax.swing.undo.CannotRedoException;  // **** ADDED ****
import javax.swing.undo.CannotUndoException;   // **** ADDED ****
import javax.swing.undo.UndoManager;          // **** ADDED ****
import javax.swing.undo.UndoableEdit;         // **** ADDED ****

import com.jogamp.opengl.GLCapabilities;       // **** ADDED ****
import com.jogamp.opengl.GLProfile;            // **** ADDED ****
import com.jogamp.opengl.awt.GLJPanel;         // **** ADDED ****
import com.jogamp.opengl.util.FPSAnimator;     // **** ADDED ****

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Set;
import java.util.HashSet;

// The rest of the MainAppFrame class remains the same as the previous version...
public class MainAppFrame extends JFrame {

    // Keep panel for overall structure
    private JPanel controlPanel;
    private GLJPanel designCanvas; // Now recognized
    private JComboBox<String> viewModeComboBox;
    private JCheckBoxMenuItem showGridMenuItem;

    // --- NEW: References to the dedicated panel classes ---
    private RoomPropertiesPanel roomPropertiesPanel;
    private FurnitureLibraryPanel furnitureLibraryPanel;
    private SelectedFurniturePanel selectedFurniturePanel;

    private FPSAnimator animator; // Now recognized
    private DesignRenderer renderer;
    private DesignModel designModel;

    private UndoManager undoManager; // Now recognized
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
    private Set<Integer> pressedKeys = new HashSet<>();

    // Floating point comparison epsilon
    private static final float EPSILON = 1e-3f;

    private File currentProjectFile = null;
    private String currentProjectName = null;
    private String currentUsername = null;

    private boolean isRotatingWithKeyboard = false;
    private float keyboardRotateStartAngle = 0f;
    private static final float KEYBOARD_ROTATE_STEP = 5.0f;
    private FloatingPricePanel floatingPricePanel;
    private JToggleButton togglePricePanelButton;


    // --- Furniture Library Data (Make static and add getters) ---
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


    // --- NEW Getters for static data ---
    public static String[] getFurnitureTypes() { return FURNITURE_TYPES; }
    public static float[][] getFurnitureDims() { return FURNITURE_DIMS; }

    public MainAppFrame() {
        // Basic setup
        setTitle("Furniture Designer - New Design");
        setSize(1600, 1000);    // Original size
        setMinimumSize(new Dimension(1600, 1000));  // Prevent UI from disappearing
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        // Continue with your existing constructor code...
        designModel = new DesignModel();
        undoManager = new UndoManager();
        setupActions();

        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true);
        caps.setNumSamples(4);
        designCanvas = new GLJPanel(caps);
        renderer = new DesignRenderer(designModel);
        designCanvas.addGLEventListener(renderer);

        designCanvas.setFocusable(true);
        setupMouseInteraction();
        setupKeyInteraction();

        animator = new FPSAnimator(designCanvas, 60);
        animator.start();

        if (renderer != null && designModel != null && designModel.getRoom() != null) {
            renderer.updateCameraForModel();
        }

        setupMenuBar();
        JPanel mainContent = new JPanel(new BorderLayout());

        roomPropertiesPanel = new RoomPropertiesPanel(this);
        furnitureLibraryPanel = new FurnitureLibraryPanel(this);
        selectedFurniturePanel = new SelectedFurniturePanel(this);

        controlPanel = createControlPanel();

        mainContent.add(new JScrollPane(controlPanel), BorderLayout.WEST);
        mainContent.add(designCanvas, BorderLayout.CENTER);
        add(mainContent);

        updateUIFromModel();
        updateUndoRedoState();
        if (renderer != null && showGridMenuItem != null) {
            renderer.setShowGrid(showGridMenuItem.isSelected());
        }

        // Add these at the end of the constructor
        pack();  // Ensure components are sized correctly
        setLocationRelativeTo(null);  // Center on screen
        applyFlatButtonStylingToAllButtons();
        floatingPricePanel = new FloatingPricePanel(this);
        floatingPricePanel.setVisible(false);
    }

    public MainAppFrame(DesignModel model, File projectFile, String projectName, String username) {
        // Store project information
        this.currentProjectFile = projectFile;
        this.currentProjectName = projectName;
        this.currentUsername = username;

        // Set up the frame
        setTitle("Furniture Designer - " + (projectName != null ? projectName : "New Design"));
        setSize(1600, 1000);
        setMinimumSize(new Dimension(1600, 1000));  // Prevent UI from disappearing
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        // Use the provided model instead of creating a new one
        if (model != null) {
            this.designModel = model;

            // Ensure the creator is set in the model
            if (this.designModel.getCreatedBy() == null && username != null) {
                this.designModel.setCreatedBy(username);
            }
        } else {
            this.designModel = new DesignModel();

            // Set the creator for new model
            if (username != null) {
                this.designModel.setCreatedBy(username);
            }
        }

        // Continue with initialization as in original constructor
        undoManager = new UndoManager();
        setupActions();

        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true);
        caps.setNumSamples(4);
        designCanvas = new GLJPanel(caps);
        renderer = new DesignRenderer(designModel);
        designCanvas.addGLEventListener(renderer);

        designCanvas.setFocusable(true);
        setupMouseInteraction();
        setupKeyInteraction();

        animator = new FPSAnimator(designCanvas, 60);
        animator.start();

        if (renderer != null && designModel != null && designModel.getRoom() != null) {
            renderer.updateCameraForModel();
        }

        setupMenuBar();
        JPanel mainContent = new JPanel(new BorderLayout());

        roomPropertiesPanel = new RoomPropertiesPanel(this);
        furnitureLibraryPanel = new FurnitureLibraryPanel(this);
        selectedFurniturePanel = new SelectedFurniturePanel(this);

        controlPanel = createControlPanel();

        mainContent.add(new JScrollPane(controlPanel), BorderLayout.WEST);
        mainContent.add(designCanvas, BorderLayout.CENTER);
        add(mainContent);

        updateUIFromModel();
        updateUndoRedoState();
        if (renderer != null && showGridMenuItem != null) {
            renderer.setShowGrid(showGridMenuItem.isSelected());
        }

        // Add these at the end of the constructor

        pack();  // Ensure components are sized correctly
        setLocationRelativeTo(null);  // Center on screen
        applyFlatButtonStylingToAllButtons();
        floatingPricePanel = new FloatingPricePanel(this);
        floatingPricePanel.setVisible(false);
    }

    // --- Simplified createControlPanel ---
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(400, panel.getPreferredSize().height));


        // Add the panels obtained from the dedicated classes
        panel.add(roomPropertiesPanel.getPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(furnitureLibraryPanel.getPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(selectedFurniturePanel.getPanel());
        panel.add(Box.createVerticalStrut(15));


        // --- Keep View Controls Section Here ---
        JPanel viewPanel = createSectionPanel("View Controls");
        viewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        viewModeComboBox = new JComboBox<>(new String[]{"3D View", "2D View (Top Down)"});
        viewModeComboBox.addActionListener(e -> setViewMode(viewModeComboBox.getSelectedIndex() == 0)); // Defined below
        viewPanel.add(new JLabel("Mode:")); viewPanel.add(viewModeComboBox);
        viewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, viewPanel.getPreferredSize().height));
        panel.add(viewPanel);

        togglePricePanelButton = new JToggleButton("Show Price Panel");
        togglePricePanelButton.addActionListener(e -> togglePricePanel());
        viewPanel.add(Box.createHorizontalStrut(10));
        viewPanel.add(togglePricePanelButton);

        panel.add(Box.createVerticalGlue()); // Pushes content to the top

        return panel;
    }

    private void togglePricePanel() {
        floatingPricePanel.toggleVisibility();
        togglePricePanelButton.setText(
                floatingPricePanel.isVisible() ? "Hide Price Panel" : "Show Price Panel"
        );
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(); panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    // --- Refactored updateUIFromModel ---
    private void updateUIFromModel() {
        if (designModel == null) return;

        // Delegate updates to the specific panels
        if (roomPropertiesPanel != null) {
            roomPropertiesPanel.updateUI(designModel);
        }
        if (furnitureLibraryPanel != null) {
            furnitureLibraryPanel.updateUI(designModel);
        }
        if (selectedFurniturePanel != null) {
            selectedFurniturePanel.updateUI(designModel);
        }

        if (floatingPricePanel != null) {
            floatingPricePanel.updatePriceData(designModel);
        }

        // Update components still managed directly by MainAppFrame (like the Edit menu)
        Furniture selected = designModel.getSelectedFurniture();
        boolean furnitureSelected = (selected != null);
        JMenuBar mb = getJMenuBar();
        if (mb != null) {
            try {
                JMenu editMenu = mb.getMenu(1); // Assuming Edit is the second menu
                if (editMenu != null && editMenu.getItemCount() > 3) {
                    // Assuming "Delete Selected Furniture" is the 4th item (index 3)
                    editMenu.getItem(3).setEnabled(furnitureSelected);
                }
            } catch (Exception e) {
                System.err.println("Error updating Edit menu enablement: " + e.getMessage());
            }
        }
        // Update view mode combo box based on renderer state
        if (viewModeComboBox != null && renderer != null) {
            viewModeComboBox.setSelectedIndex(renderer.is3DMode() ? 0 : 1);
        }
    }

    // --- NEW: Handler for library selection changes ---
    /** Called by FurnitureLibraryPanel when selection changes */
    public void handleLibrarySelectionChange(int selectedIndex) {
        if (designModel != null && designModel.getSelectedFurniture() == null && selectedIndex != -1) {
            if (selectedFurniturePanel != null) {
                JTextField wField = selectedFurniturePanel.getFurnWidthField();
                JTextField dField = selectedFurniturePanel.getFurnDepthField();
                JTextField hField = selectedFurniturePanel.getFurnHeightField();
                JTextField rField = selectedFurniturePanel.getFurnRotationYField();

                if (wField != null && !wField.hasFocus()) wField.setText(String.format("%.2f", FURNITURE_DIMS[selectedIndex][0]));
                if (dField != null && !dField.hasFocus()) dField.setText(String.format("%.2f", FURNITURE_DIMS[selectedIndex][1]));
                if (hField != null && !hField.hasFocus()) hField.setText(String.format("%.2f", FURNITURE_DIMS[selectedIndex][2]));
                if (rField != null && !rField.hasFocus()) rField.setText("0.0");
            }
        }
    }

    // --- Action Handlers ---
    private void setupActions() { // Now recognized as defined
        undoAction = new AbstractAction("Undo") {
            @Override public void actionPerformed(ActionEvent e) {
                try { if (undoManager.canUndo()) undoManager.undo(); }
                catch (CannotUndoException ex) { System.err.println("Undo failed: " + ex); }
                updateUndoRedoState();
            }
        };
        undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoAction = new AbstractAction("Redo") {
            @Override public void actionPerformed(ActionEvent e) {
                try { if (undoManager.canRedo()) undoManager.redo(); }
                catch (CannotRedoException ex) { System.err.println("Redo failed: " + ex); }
                updateUndoRedoState();
            }
        };
        redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        }
        updateUndoRedoState(); // Call here to set initial state
    }

    private void setupMenuBar() { // Now recognized as defined
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem dashboardItem = new JMenuItem("Return to Dashboard");
        dashboardItem.addActionListener(e -> returnToDashboard());
        JMenuItem newItem = new JMenuItem("New Design");
        newItem.addActionListener(e -> handleNewDesign());
        JMenuItem openItem = new JMenuItem("Open Design..."); openItem.addActionListener(e -> handleLoadDesign());
        JMenuItem saveItem = new JMenuItem("Save Design..."); saveItem.addActionListener(e -> handleSaveDesign());
        JMenuItem exitItem = new JMenuItem("Exit"); exitItem.addActionListener(e -> handleExit());
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        fileMenu.add(dashboardItem);
        fileMenu.addSeparator();
        fileMenu.add(newItem);
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem(undoAction); JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem deleteItem = new JMenuItem("Delete Selected Furniture"); deleteItem.addActionListener(e -> handleDeleteSelectedFurniture());
        editMenu.add(undoItem); editMenu.add(redoItem); editMenu.addSeparator(); editMenu.add(deleteItem); // Index 3
        JMenu viewMenu = new JMenu("View");
        JRadioButtonMenuItem view2DItem = new JRadioButtonMenuItem("2D View (Top Down)");
        JRadioButtonMenuItem view3DItem = new JRadioButtonMenuItem("3D View", true);
        ButtonGroup viewGroup = new ButtonGroup(); viewGroup.add(view2DItem); view3DItem.setSelected(true);
        viewGroup.add(view3DItem);
        view2DItem.addActionListener(e -> setViewMode(false)); view3DItem.addActionListener(e -> setViewMode(true));
        showGridMenuItem = new JCheckBoxMenuItem("Show Grid", true);
        showGridMenuItem.addActionListener(e -> { if (renderer != null) { renderer.setShowGrid(showGridMenuItem.isSelected()); designCanvas.repaint(); } });
        viewMenu.add(view2DItem); viewMenu.add(view3DItem); viewMenu.addSeparator(); viewMenu.add(showGridMenuItem);
        JMenuItem togglePricePanelMenuItem = new JMenuItem("Toggle Price Panel");
        togglePricePanelMenuItem.addActionListener(e -> togglePricePanel());
        viewMenu.add(togglePricePanelMenuItem);
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu); menuBar.add(editMenu); menuBar.add(viewMenu); menuBar.add(helpMenu); setJMenuBar(menuBar);
    }

    // --- Mouse Interaction ---
    private void setupMouseInteraction() { // Now recognized as defined
        MouseAdapter mouseAdapter = new MouseAdapter() {
            // ... (MouseAdapter implementation remains the same) ...
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                designCanvas.requestFocusInWindow(); // Request focus
                finalizeKeyboardMove();

                if (SwingUtilities.isRightMouseButton(e)) {
                    isDraggingFurniture = false;
                    draggedFurniture = null;
                    isDraggingCamera = true;
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    finalizeKeyboardMove();

                    Furniture pickedFurniture = renderer.pickFurniture(e.getX(), e.getY());

                    if (pickedFurniture != null) {
                        isDraggingCamera = false;
                        if (designModel.getSelectedFurniture() != pickedFurniture) {
                            designModel.setSelectedFurniture(pickedFurniture);
                            updateUIFromModel();
                        }
                        isDraggingFurniture = true;
                        draggedFurniture = pickedFurniture;
                        dragStartPosition = draggedFurniture.getPosition().clone();

                        Vector3f clickFloorPos = renderer.screenToWorldFloor(e.getX(), e.getY());
                        if (clickFloorPos != null) {
                            dragOffset = new Vector3f(
                                    clickFloorPos.x - draggedFurniture.getPosition().x,
                                    0,
                                    clickFloorPos.z - draggedFurniture.getPosition().z
                            );
                        } else {
                            dragOffset = new Vector3f(0, 0, 0);
                        }

                    } else {
                        isDraggingCamera = false;
                        if (designModel.getSelectedFurniture() != null) {
                            designModel.setSelectedFurniture(null);
                            updateUIFromModel();
                            designCanvas.repaint();
                        }
                        if (e.isShiftDown()) {
                            isDraggingFurniture = false;
                            draggedFurniture = null;
                            isDraggingCamera = true;
                        } else {
                            isDraggingCamera = false;
                            isDraggingFurniture = false;
                        }
                    }
                } else if (SwingUtilities.isMiddleMouseButton(e)){
                    finalizeKeyboardMove();
                    isDraggingFurniture = false;
                    draggedFurniture = null;
                    isDraggingCamera = true;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint == null || (!isDraggingCamera && !isDraggingFurniture)) return;

                if (isDraggingCamera && isDraggingFurniture) {
                    System.err.println("Warning: Both camera and furniture drag flags are true!");
                    isDraggingFurniture = false;
                    draggedFurniture = null;
                    dragOffset = null;
                    dragStartPosition = null;
                }

                float deltaX = e.getX() - lastMousePoint.x;
                float deltaY = e.getY() - lastMousePoint.y;

                if (isDraggingCamera) {
                    if (SwingUtilities.isMiddleMouseButton(e) || (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown())) {
                        renderer.panCamera(deltaX, deltaY);
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        renderer.rotateCamera(deltaX, deltaY);
                    }
                } else if (isDraggingFurniture && draggedFurniture != null) {
                    Vector3f clickFloorPos = renderer.screenToWorldFloor(e.getX(), e.getY());
                    if (clickFloorPos != null) {
                        float proposedX = clickFloorPos.x - dragOffset.x;
                        float proposedZ = clickFloorPos.z - dragOffset.z;

                        Room currentRoom = designModel.getRoom();
                        if (currentRoom != null) {
                            Vector3f newPosition = new Vector3f(proposedX, draggedFurniture.getPosition().y, proposedZ);
                            if (isFootprintInsideRoom(newPosition, draggedFurniture, currentRoom)) {
                                draggedFurniture.setPosition(newPosition);
                            }
                        } else {
                            draggedFurniture.setPosition(new Vector3f(proposedX, draggedFurniture.getPosition().y, proposedZ));
                        }
                    }
                }

                lastMousePoint = e.getPoint();
                designCanvas.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                finalizeKeyboardMove();

                if (isDraggingFurniture && draggedFurniture != null && dragStartPosition != null) {
                    if (!draggedFurniture.getPosition().equals(dragStartPosition)) {
                        registerUndoableEdit(new MoveFurnitureEdit(draggedFurniture, dragStartPosition, draggedFurniture.getPosition()));
                    }
                } else {
                    dragStartPosition = null;
                }

                isDraggingCamera = false;
                isDraggingFurniture = false;
                draggedFurniture = null;
                dragOffset = null;
                lastMousePoint = null;
                dragStartPosition = null;

                updateUndoRedoState();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                finalizeKeyboardMove();
                float delta = -e.getWheelRotation();
                renderer.zoomCamera(delta);
                designCanvas.repaint();
            }
        };
        designCanvas.addMouseListener(mouseAdapter);
        designCanvas.addMouseMotionListener(mouseAdapter);
        designCanvas.addMouseWheelListener(mouseAdapter);
    }

    private void handleSaveDesignAs() {
        finalizeKeyboardMove();

        // Ensure the creator is set before saving
        if (designModel.getCreatedBy() == null && currentUsername != null) {
            designModel.setCreatedBy(currentUsername);
        }

        String suggestedName = (currentProjectName != null) ? currentProjectName : "MyDesign";
        String newName = JOptionPane.showInputDialog(this,
                "Enter name for project:",
                suggestedName);

        if (newName == null || newName.trim().isEmpty()) {
            return; // User cancelled
        }

        // Use project manager to save with new name
        ProjectManager.ProjectMetadata metadata = ProjectManager.saveNewProject(
                designModel,
                newName,
                (currentUsername != null) ? currentUsername : "designer"
        );

        if (metadata != null) {
            // Update current project info
            currentProjectFile = new File(metadata.filename);
            currentProjectName = metadata.projectName;
            setTitle("Furniture Designer - " + currentProjectName);

            JOptionPane.showMessageDialog(this,
                    "Design saved successfully as " + metadata.projectName,
                    "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error saving design file.",
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Keyboard Interaction ---
    private void setupKeyInteraction() { // Now recognized as defined
        designCanvas.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { handleKeyPress(e); }
            @Override public void keyReleased(KeyEvent e) { handleKeyRelease(e); }
        });
        designCanvas.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { finalizeKeyboardMove(); }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;

        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
        {
            pressedKeys.add(keyCode);

            if (!isMovingWithKeyboard) {
                isMovingWithKeyboard = true;
                keyboardMoveStartPosition = selected.getPosition().clone();
            }

            float dx = 0, dz = 0;
            if (pressedKeys.contains(KeyEvent.VK_UP)) dz -= KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_DOWN)) dz += KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_LEFT)) dx -= KEYBOARD_MOVE_STEP;
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) dx += KEYBOARD_MOVE_STEP;

            Vector3f currentPos = selected.getPosition();
            Vector3f proposedPos = new Vector3f(currentPos.x + dx, currentPos.y, currentPos.z + dz);

            Room currentRoom = designModel.getRoom();
            if (currentRoom != null) {
                if (isFootprintInsideRoom(proposedPos, selected, currentRoom)) {
                    selected.setPosition(proposedPos);
                }
            } else {
                selected.setPosition(proposedPos);
            }
            designCanvas.repaint();

        } else if (keyCode == KeyEvent.VK_Q || keyCode == KeyEvent.VK_E) {
            // New rotation handling
            if (!isRotatingWithKeyboard) {
                isRotatingWithKeyboard = true;
                keyboardRotateStartAngle = selected.getRotation().y;
            }

            float rotationAmount = 0;
            if (keyCode == KeyEvent.VK_Q) rotationAmount = -KEYBOARD_ROTATE_STEP; // Counter-clockwise
            if (keyCode == KeyEvent.VK_E) rotationAmount = KEYBOARD_ROTATE_STEP;  // Clockwise

            // Apply rotation
            float currentRotation = selected.getRotation().y;
            float newRotation = currentRotation + rotationAmount;

            // Normalize the rotation to 0-360 degrees
            while (newRotation >= 360) newRotation -= 360;
            while (newRotation < 0) newRotation += 360;

            selected.getRotation().y = newRotation;

            // Update the rotation field in the UI
            if (selectedFurniturePanel != null) {
                JTextField rotField = selectedFurniturePanel.getFurnRotationYField();
                if (rotField != null && !rotField.hasFocus()) {
                    rotField.setText(String.format("%.1f", newRotation));
                }
            }

            designCanvas.repaint();
        } else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
            handleDeleteSelectedFurniture();
        }
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            cancelKeyboardMove();
            cancelKeyboardRotate(); // We'll add this method
        }
    }

    private void finalizeKeyboardRotate() {
        if (isRotatingWithKeyboard) {
            Furniture selected = designModel.getSelectedFurniture();
            if (selected != null && Math.abs(selected.getRotation().y - keyboardRotateStartAngle) > 0.5f) {
                registerUndoableEdit(new ChangeFurnitureRotationEdit(selected, selected.getRotation().y));
            }
            isRotatingWithKeyboard = false;
            keyboardRotateStartAngle = 0f;
            updateUndoRedoState();
        }
    }

    private void cancelKeyboardRotate() {
        if (isRotatingWithKeyboard) {
            Furniture selected = designModel.getSelectedFurniture();
            if (selected != null) {
                selected.getRotation().y = keyboardRotateStartAngle;
            }
            isRotatingWithKeyboard = false;
            keyboardRotateStartAngle = 0f;
            designCanvas.repaint();
            System.out.println("Keyboard rotation cancelled.");
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)
        {
            pressedKeys.remove(keyCode);

            if (isMovingWithKeyboard && pressedKeys.isEmpty()) {
                finalizeKeyboardMove();
            }
        }
        else if (keyCode == KeyEvent.VK_Q || keyCode == KeyEvent.VK_E) {
            // When Q or E are released, finalize the rotation
            finalizeKeyboardRotate();
        }
    }

    private void finalizeKeyboardMove() {
        if (isMovingWithKeyboard && keyboardMoveStartPosition != null) {
            Furniture selected = designModel.getSelectedFurniture();
            if (selected != null && !selected.getPosition().equals(keyboardMoveStartPosition)) {
                registerUndoableEdit(new MoveFurnitureEdit(selected, keyboardMoveStartPosition, selected.getPosition()));
            }
            isMovingWithKeyboard = false;
            keyboardMoveStartPosition = null;
            updateUndoRedoState();
        }
        pressedKeys.clear();

        // Also finalize any rotation
        finalizeKeyboardRotate();
    }

    private void cancelKeyboardMove() { // Now recognized as defined
        if (isMovingWithKeyboard && keyboardMoveStartPosition != null) {
            Furniture selected = designModel.getSelectedFurniture();
            if (selected != null) {
                selected.setPosition(keyboardMoveStartPosition);
            }
            isMovingWithKeyboard = false;
            keyboardMoveStartPosition = null;
            pressedKeys.clear();
            designCanvas.repaint();
            System.out.println("Keyboard move cancelled.");
        }
    }

    // --- Boundary Check Helper ---
    private boolean isFootprintInsideRoom(Vector3f proposedPos, Furniture furniture, Room room) { // Now recognized as defined
        if (room == null || furniture == null || proposedPos == null) return false;

        float proposedX = proposedPos.x;
        float proposedZ = proposedPos.z;
        float halfW = furniture.getWidth() / 2.0f;
        float halfD = furniture.getDepth() / 2.0f;

        switch (room.getShape()) {
            case RECTANGULAR:
                float roomW = room.getWidth();
                float roomL = room.getLength();
                boolean c1 = (proposedX - halfW >= 0 - EPSILON && proposedX - halfW <= roomW + EPSILON && proposedZ - halfD >= 0 - EPSILON && proposedZ - halfD <= roomL + EPSILON);
                boolean c2 = (proposedX + halfW >= 0 - EPSILON && proposedX + halfW <= roomW + EPSILON && proposedZ - halfD >= 0 - EPSILON && proposedZ - halfD <= roomL + EPSILON);
                boolean c3 = (proposedX + halfW >= 0 - EPSILON && proposedX + halfW <= roomW + EPSILON && proposedZ + halfD >= 0 - EPSILON && proposedZ + halfD <= roomL + EPSILON);
                boolean c4 = (proposedX - halfW >= 0 - EPSILON && proposedX - halfW <= roomW + EPSILON && proposedZ + halfD >= 0 - EPSILON && proposedZ + halfD <= roomL + EPSILON);
                return c1 && c2 && c3 && c4;
            case CIRCULAR:
                float roomR = room.getRadius();
                float distSq = proposedX * proposedX + proposedZ * proposedZ;
                return distSq <= (roomR * roomR) + EPSILON;
            case L_SHAPED:
                float oW = room.getL_outerWidth(); float oL = room.getL_outerLength();
                float iW = room.getL_insetWidth(); float iL = room.getL_insetLength();
                boolean inR1 = (proposedX >= 0 - EPSILON && proposedX <= oW + EPSILON && proposedZ >= 0 - EPSILON && proposedZ <= iL + EPSILON);
                boolean inR2 = (proposedX >= 0 - EPSILON && proposedX <= iW + EPSILON && proposedZ >= iL - EPSILON && proposedZ <= oL + EPSILON);
                return inR1 || inR2;
            case T_SHAPED:
                float bW = room.getT_barWidth(); float bL = room.getT_barLength();
                float sW = room.getT_stemWidth(); float sL = room.getT_stemLength();
                float stemStartX = (bW - sW) / 2.0f; float stemEndX = stemStartX + sW;
                float totalLength = bL + sL;
                boolean inBar = (proposedX >= 0 - EPSILON && proposedX <= bW + EPSILON && proposedZ >= 0 - EPSILON && proposedZ <= bL + EPSILON);
                boolean inStem = (proposedX >= stemStartX - EPSILON && proposedX <= stemEndX + EPSILON && proposedZ >= bL - EPSILON && proposedZ <= totalLength + EPSILON);
                return inBar || inStem;
            default:
                System.err.println("Unknown room shape in boundary check.");
                return true;
        }
    }

    // --- File/Action Handlers (Protected/Public for panel access) ---
    protected void handleAddFurniture() {
        finalizeKeyboardMove();
        JList<String> libraryList = furnitureLibraryPanel.getFurnitureLibraryList();
        int selectedIndex = libraryList.getSelectedIndex();

        if (selectedIndex != -1) {
            String type = FURNITURE_TYPES[selectedIndex];
            try {
                JTextField wField = selectedFurniturePanel.getFurnWidthField();
                JTextField dField = selectedFurniturePanel.getFurnDepthField();
                JTextField hField = selectedFurniturePanel.getFurnHeightField();

                float w = FURNITURE_DIMS[selectedIndex][0]; try { w = Float.parseFloat(wField.getText()); } catch (Exception ignored) {}
                float d = FURNITURE_DIMS[selectedIndex][1]; try { d = Float.parseFloat(dField.getText()); } catch (Exception ignored) {}
                float h = FURNITURE_DIMS[selectedIndex][2]; try { h = Float.parseFloat(hField.getText()); } catch (Exception ignored) {}
                if (w <= 0 || d <= 0 || h <= 0) throw new NumberFormatException("Dimensions must be positive");

                Vector3f initialPos = designModel.getRoom().calculateCenter();
                if (initialPos == null) initialPos = new Vector3f(2.5f, 0, 2.5f);
                initialPos.y = 0;

                if (!isFootprintInsideRoom(initialPos, new Furniture(type, initialPos, w, d, h), designModel.getRoom())) {
                    System.err.println("Warning: Default furniture position is outside the room bounds.");
                }

                Furniture newFurniture = new Furniture(type, initialPos, w, d, h);
                designModel.addFurniture(newFurniture);
                registerUndoableEdit(new AddFurnitureEdit(newFurniture));

                // Ensure UI is updated including price panels
                updateUIFromModel();

                // Update undo/redo state
                updateUndoRedoState();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,"Invalid dimensions for new furniture. Using defaults.","Input Error", JOptionPane.WARNING_MESSAGE);
                try {
                    int defaultIdx = (selectedIndex >= 0 && selectedIndex < FURNITURE_DIMS.length) ? selectedIndex : 0;
                    String defaultType = FURNITURE_TYPES[defaultIdx];
                    float w = FURNITURE_DIMS[defaultIdx][0];
                    float d = FURNITURE_DIMS[defaultIdx][1];
                    float h = FURNITURE_DIMS[defaultIdx][2];
                    Vector3f initialPos = designModel.getRoom().calculateCenter();
                    if (initialPos == null) initialPos = new Vector3f(2.5f, 0, 2.5f);
                    initialPos.y = 0;

                    Furniture newFurniture = new Furniture(defaultType, initialPos, w, d, h);
                    designModel.addFurniture(newFurniture);
                    registerUndoableEdit(new AddFurnitureEdit(newFurniture));

                    // Ensure UI is updated including price panels
                    updateUIFromModel();

                    // Update undo/redo state
                    updateUndoRedoState();
                } catch (Exception finalEx) {
                    JOptionPane.showMessageDialog(this,"Failed to add furniture with defaults: " + finalEx.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a furniture type from the library.", "No Furniture Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Applies a flat button style with dark grey background and white text to all buttons in the application.
     */
    private void applyFlatButtonStylingToAllButtons() {
        // Apply styling to all buttons in controlPanel
        applyFlatButtonStylingToContainer(controlPanel);

        // Also apply to any other containers with buttons
        if (roomPropertiesPanel != null) {
            applyFlatButtonStylingToContainer(roomPropertiesPanel.getPanel());
        }
        if (furnitureLibraryPanel != null) {
            applyFlatButtonStylingToContainer(furnitureLibraryPanel.getPanel());
        }
        if (selectedFurniturePanel != null) {
            applyFlatButtonStylingToContainer(selectedFurniturePanel.getPanel());
        }
    }

    /**
     * Recursively applies flat button styling to all JButtons in a container
     * @param container The container to process
     */
    private void applyFlatButtonStylingToContainer(Container container) {
        if (container == null) return;

        // Apply to all components in this container
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                applyFlatButtonStyle((JButton) comp);
            } else if (comp instanceof Container) {
                // Recursively process nested containers
                applyFlatButtonStylingToContainer((Container) comp);
            }
        }
    }

    /**
     * Applies a flat button style with hover effects.
     * Reset and Delete buttons get a muted red style.
     * @param button The JButton to style
     */
    private void applyFlatButtonStyle(JButton button) {
        // Check if this is a "caution" button (reset or delete)
        boolean isCautionButton = button.getText().contains("Reset") ||
                button.getText().contains("Delete");

        // Default colors - different for regular vs caution buttons
        final Color defaultBackground = isCautionButton ?
                new Color(255, 220, 220) : new Color(255, 255, 255);
        final Color defaultForeground = isCautionButton ?
                new Color(180, 0, 0) : new Color(24, 24, 24);
        final Color hoverBackground = isCautionButton ?
                new Color(255, 200, 200) : new Color(114, 114, 114);
        final Color pressedBackground = isCautionButton ?
                new Color(200, 0, 0) : new Color(64, 64, 64);
        final Color pressedForeground = Color.WHITE;

        // Initial appearance
        button.setBackground(defaultBackground);
        button.setForeground(defaultForeground);

        // Remove border and focus painted effects
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // Ensure the background is painted
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        // Add some padding for better appearance
        button.setMargin(new Insets(5, 10, 5, 10));

        // Add mouse listeners for hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBackground);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(defaultBackground);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setBackground(pressedBackground);
                button.setForeground(pressedForeground);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (button.contains(e.getPoint())) {
                    button.setBackground(hoverBackground); // Still hovering
                } else {
                    button.setBackground(defaultBackground); // Mouse moved away
                }
                button.setForeground(defaultForeground);
            }
        });

        // Override UI to remove any platform-specific styling
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            protected void paintButtonPressed(Graphics g, AbstractButton b) {
                // Override to prevent default pressed effect
                g.setColor(pressedBackground);
                g.fillRect(0, 0, b.getWidth(), b.getHeight());
            }
        });
    }

    protected void handleUpdateRoomSize() { // Make protected or public
        finalizeKeyboardMove();
        Room room = designModel.getRoom();
        if (room == null || roomPropertiesPanel == null) return;

        try {
            Room.RoomShape selectedShape = (Room.RoomShape) roomPropertiesPanel.getRoomShapeComboBox().getSelectedItem();
            if (selectedShape == null) return;

            float h = Float.parseFloat(roomPropertiesPanel.getSharedHeightField().getText());
            if (h <= EPSILON) throw new NumberFormatException("Height must be positive");

            float currentW = room.getWidth(), currentL = room.getLength();
            float currentR = room.getRadius();
            float currentLoW = room.getL_outerWidth(), currentLoL = room.getL_outerLength();
            float currentLiW = room.getL_insetWidth(), currentLiL = room.getL_insetLength();
            float currentTbW = room.getT_barWidth(), currentTbL = room.getT_barLength();
            float currentTsW = room.getT_stemWidth(), currentTsL = room.getT_stemLength();
            float currentH = room.getHeight();
            Room.RoomShape currentShape = room.getShape();

            float w = currentW, l = currentL, r = currentR;
            float loW = currentLoW, loL = currentLoL, liW = currentLiW, liL = currentLiL;
            float tbW = currentTbW, tbL = currentTbL, tsW = currentTsW, tsL = currentTsL;

            boolean changed = false;

            switch (selectedShape) {
                case RECTANGULAR:
                    w = Float.parseFloat(roomPropertiesPanel.getRoomWidthField().getText());
                    l = Float.parseFloat(roomPropertiesPanel.getRoomLengthField().getText());
                    if (w <= EPSILON || l <= EPSILON) throw new NumberFormatException("Rectangular dimensions must be positive");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > EPSILON ||
                            Math.abs(w - currentW) > EPSILON || Math.abs(l - currentL) > EPSILON;
                    break;
                case CIRCULAR:
                    r = Float.parseFloat(roomPropertiesPanel.getRoomRadiusField().getText());
                    if (r <= EPSILON) throw new NumberFormatException("Radius must be positive");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > EPSILON ||
                            Math.abs(r - currentR) > EPSILON;
                    break;
                case L_SHAPED:
                    loW = Float.parseFloat(roomPropertiesPanel.getLOuterWidthField().getText());
                    loL = Float.parseFloat(roomPropertiesPanel.getLOuterLengthField().getText());
                    liW = Float.parseFloat(roomPropertiesPanel.getLInsetWidthField().getText());
                    liL = Float.parseFloat(roomPropertiesPanel.getLInsetLengthField().getText());
                    if (loW <= EPSILON || loL <= EPSILON || liW < 0 || liL < 0 || liW >= loW - EPSILON || liL >= loL - EPSILON)
                        throw new NumberFormatException("Invalid L-Shape dimensions");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > EPSILON ||
                            Math.abs(loW - currentLoW) > EPSILON || Math.abs(loL - currentLoL) > EPSILON ||
                            Math.abs(liW - currentLiW) > EPSILON || Math.abs(liL - currentLiL) > EPSILON;
                    break;
                case T_SHAPED:
                    tbW = Float.parseFloat(roomPropertiesPanel.getTBarWidthField().getText());
                    tbL = Float.parseFloat(roomPropertiesPanel.getTBarLengthField().getText());
                    tsW = Float.parseFloat(roomPropertiesPanel.getTStemWidthField().getText());
                    tsL = Float.parseFloat(roomPropertiesPanel.getTStemLengthField().getText());
                    if (tbW <= EPSILON || tbL <= EPSILON || tsW <= EPSILON || tsL <= EPSILON || tsW > tbW - EPSILON)
                        throw new NumberFormatException("Invalid T-Shape dimensions");
                    changed = currentShape != selectedShape || Math.abs(h - currentH) > EPSILON ||
                            Math.abs(tbW - currentTbW) > EPSILON || Math.abs(tbL - currentTbL) > EPSILON ||
                            Math.abs(tsW - currentTsW) > EPSILON || Math.abs(tsL - currentTsL) > EPSILON;
                    break;
            }

            if (changed) {
                registerUndoableEdit(new ChangeRoomPropertiesEdit(room, selectedShape, h, w, l, r, loW, loL, liW, liL, tbW, tbL, tsW, tsL)); // Defined below
                updateUndoRedoState();
                return;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,"Invalid number format or dimensions:\n" + ex.getMessage(),"Input Error",JOptionPane.ERROR_MESSAGE);
            if (roomPropertiesPanel != null) roomPropertiesPanel.updateUI(designModel);
        }
    }

    protected void handleUpdateFurnitureDimensions() {
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null || selectedFurniturePanel == null) return;
        try {
            float newWidth = Float.parseFloat(selectedFurniturePanel.getFurnWidthField().getText());
            float newDepth = Float.parseFloat(selectedFurniturePanel.getFurnDepthField().getText());
            float newHeight = Float.parseFloat(selectedFurniturePanel.getFurnHeightField().getText());
            if (newWidth <= EPSILON || newDepth <= EPSILON || newHeight <= EPSILON) {
                throw new NumberFormatException("Dimensions must be positive.");
            }

            if (Math.abs(newWidth - selected.getWidth()) > EPSILON ||
                    Math.abs(newDepth - selected.getDepth()) > EPSILON ||
                    Math.abs(newHeight - selected.getHeight()) > EPSILON)
            {
                Furniture tempFurniture = new Furniture(selected.getType(), selected.getPosition().clone(), newWidth, newDepth, newHeight);
                Room currentRoom = designModel.getRoom();
                if (currentRoom != null && !isFootprintInsideRoom(tempFurniture.getPosition(), tempFurniture, currentRoom)) {
                    JOptionPane.showMessageDialog(this,"Cannot change dimensions: New size would place furniture outside the room.","Input Error", JOptionPane.ERROR_MESSAGE);
                    if (selectedFurniturePanel != null) selectedFurniturePanel.updateUI(designModel);
                    return;
                }

                registerUndoableEdit(new ChangeFurnitureDimensionsEdit(selected, newWidth, newDepth, newHeight));

                // Make sure UI is updated including price panels
                updateUIFromModel();

                updateUndoRedoState();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid dimensions: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            if (selectedFurniturePanel != null) selectedFurniturePanel.updateUI(designModel);
        }
    }

    protected void handleUpdateFurnitureRotation() { // Make protected or public
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null || selectedFurniturePanel == null) return;
        try {
            float newRotationY = Float.parseFloat(selectedFurniturePanel.getFurnRotationYField().getText());

            if (Math.abs(newRotationY - selected.getRotation().y) > 0.5f) {
                registerUndoableEdit(new ChangeFurnitureRotationEdit(selected, newRotationY)); // Defined below
                updateUndoRedoState();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid rotation angle.", "Input Error", JOptionPane.ERROR_MESSAGE);
            if (selectedFurniturePanel != null) selectedFurniturePanel.updateUI(designModel);
        }
    }

    protected void handleSetRoomColor(boolean isWall) { // Make protected or public
        finalizeKeyboardMove();
        Room room = designModel.getRoom();
        if (room == null) return;

        Color initialColor = isWall ? room.getWallColor() : room.getFloorColor();
        Color newColor = JColorChooser.showDialog(this, "Select " + (isWall ? "Wall" : "Floor") + " Color", initialColor);

        if (newColor != null) {
            registerUndoableEdit(new ChangeRoomAppearanceEdit(room, isWall, newColor, null)); // Defined below
            updateUndoRedoState();
        }
    }

    protected void handleResetRoomAppearance(boolean isWall) { // Make protected or public
        finalizeKeyboardMove();
        Room room = designModel.getRoom();
        if (room == null) return;

        Color defaultColor = isWall ? Color.WHITE : Color.LIGHT_GRAY;

        registerUndoableEdit(new ChangeRoomAppearanceEdit(room, isWall, defaultColor, "")); // Defined below

        updateUndoRedoState();
    }

    protected void handleSetRoomTexture(boolean isWall) { // Make protected or public
        finalizeKeyboardMove();
        Room room = designModel.getRoom();
        if (room == null) return;

        String newTexturePath = selectTextureFile(); // Defined below

        int choice = JOptionPane.NO_OPTION;
        if (newTexturePath == null) {
            choice = JOptionPane.showConfirmDialog(this,
                    "Clear existing " + (isWall ? "wall" : "floor") + " texture?",
                    "Clear Texture", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                newTexturePath = "";
            } else {
                return;
            }
        }
        Color currentColor = isWall ? room.getWallColor() : room.getFloorColor();
        registerUndoableEdit(new ChangeRoomAppearanceEdit(room, isWall, null, newTexturePath)); // Defined below

        updateUndoRedoState();
    }

    protected void handleDeleteSelectedFurniture() {
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected != null) {
            // Store a reference to the furniture before removing it
            Furniture furnitureToRemove = selected;

            // Remove from model (this also sets selected furniture to null)
            designModel.removeFurniture(furnitureToRemove);

            // Register the undoable edit
            registerUndoableEdit(new RemoveFurnitureEdit(furnitureToRemove));

            // Ensure UI is updated including price panels
            updateUIFromModel();

            // Refresh canvas
            designCanvas.repaint();

            // Update undo/redo state
            updateUndoRedoState();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No furniture selected to delete.",
                    "Delete Furniture",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void handleSetFurnitureColor() { // Make protected or public
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        Color initialColor = selected.getColor();
        Color newColor = JColorChooser.showDialog(this, "Select Furniture Color", initialColor);
        if (newColor != null && !newColor.equals(initialColor)) {
            registerUndoableEdit(new ChangeFurnitureAppearanceEdit(selected, newColor, null)); // Defined below
            updateUndoRedoState();
        }
    }

    protected void handleSetFurnitureTexture() { // Make protected or public
        finalizeKeyboardMove();
        Furniture selected = designModel.getSelectedFurniture();
        if (selected == null) return;
        String newTexturePath = selectTextureFile(); // Defined below

        int choice = JOptionPane.NO_OPTION;
        if (newTexturePath == null) {
            choice = JOptionPane.showConfirmDialog(this,"Clear existing furniture texture?","Clear Texture", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) { newTexturePath = ""; }
            else { return; }
        }
        registerUndoableEdit(new ChangeFurnitureAppearanceEdit(selected, null, newTexturePath)); // Defined below

        updateUndoRedoState();
    }

    private void handleNewDesign() { // Now recognized as defined
        finalizeKeyboardMove();
        int choice = JOptionPane.showConfirmDialog(this,
                "Clear the current design? Unsaved changes will be lost.",
                "New Design", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            performClearDesign();
        }
    }
    private void performClearDesign() { // Now recognized as defined
        designModel.clearDesign();
        undoManager.discardAllEdits();
        renderer.setDesignModel(designModel);
        updateUIFromModel();
        updateUndoRedoState();
        designCanvas.repaint();
    }


    private void handleLoadDesign() {
        finalizeKeyboardMove();
        JFileChooser fc = new JFileChooser("./designs");
        fc.setDialogTitle("Open Design File");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Furniture Design Files (*.furn)", "furn");
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                // Use ProjectManager to load the design model
                DesignModel loadedModel = ProjectManager.loadDesignModel(file);

                if (loadedModel != null) {
                    // Update the current project information
                    designModel = loadedModel;
                    currentProjectFile = file;
                    currentProjectName = file.getName();
                    if (currentProjectName.toLowerCase().endsWith(".furn")) {
                        currentProjectName = currentProjectName.substring(0, currentProjectName.length() - 5);
                    }

                    // Update the application title to reflect the loaded project
                    setTitle("Furniture Designer - " + currentProjectName);

                    // Update renderer and UI
                    renderer.setDesignModel(designModel);
                    undoManager.discardAllEdits();
                    updateUIFromModel();
                    updateUndoRedoState();

                    // Reset camera to fit the new room
                    if (renderer != null && designModel.getRoom() != null) {
                        renderer.updateCameraForModel();
                    }

                    designCanvas.repaint();

                    JOptionPane.showMessageDialog(this,
                            "Design loaded successfully from " + file.getName(),
                            "Load Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error: Could not load design file.",
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading design file:\n" + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSaveDesign() {
        finalizeKeyboardMove();

        // Ensure the creator is set before saving
        if (designModel.getCreatedBy() == null && currentUsername != null) {
            designModel.setCreatedBy(currentUsername);
        }

        // If we have a current project, update it
        if (currentProjectFile != null && currentProjectFile.exists()) {
            if (ProjectManager.updateProject(designModel, currentProjectFile)) {
                JOptionPane.showMessageDialog(this,
                        "Design updated successfully.",
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error updating design file.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        handleSaveDesignAs();
    }

    private String selectTextureFile() { // Now recognized as defined
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
            return null;
        }
    }

    // Add after other methods
    private void returnToDashboard() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Return to project dashboard? Unsaved changes will be lost.",
                "Confirm Navigation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            animator.stop();
            dispose();

            // Open the dashboard with the last username (if known)
            String username = (currentUsername != null) ? currentUsername : "designer";
            ProjectDashboardFrame dashboard = new ProjectDashboardFrame(username);
            dashboard.setVisible(true);
        }
    }

    // Replace the existing handleExit method
    private void handleExit() {
        Object[] options = {"Return to Dashboard", "Exit Application", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Would you like to return to the dashboard or exit completely?\nUnsaved changes will be lost.",
                "Navigation Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) { // Return to Dashboard
            returnToDashboard();
        } else if (choice == 1) { // Exit Application
            if (floatingPricePanel != null) {
                floatingPricePanel.dispose();
            }
            animator.stop();
            dispose();
            // System.exit(0); // Optional
        }
        // If choice == 2 (Cancel), do nothing
    }


    // --- Undo/Redo Infrastructure ---
    private void registerUndoableEdit(UndoableEdit edit) { // Now recognized as defined
        if (edit != null) {
            undoManager.addEdit(edit);
            updateUndoRedoState();
        }
    }

    private void updateUndoRedoState() { // Now recognized as defined
        undoAction.setEnabled(undoManager.canUndo()); undoAction.putValue(Action.NAME, undoManager.getUndoPresentationName());
        redoAction.setEnabled(undoManager.canRedo()); redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());
    }

    private void setViewMode(boolean is3D) { // Now recognized as defined
        if (renderer == null) return; renderer.set3DMode(is3D);
        if (viewModeComboBox != null) viewModeComboBox.setSelectedIndex(is3D ? 0 : 1);
        JMenuBar mb = getJMenuBar(); if (mb != null) { try { JMenu viewMenu = mb.getMenu(2); if (viewMenu != null && viewMenu.getItemCount() > 1) { ((JRadioButtonMenuItem)viewMenu.getItem(0)).setSelected(!is3D); ((JRadioButtonMenuItem)viewMenu.getItem(1)).setSelected(is3D); } } catch(Exception e) {} }
        designCanvas.repaint();
    }

    // Make sure contrast color getter is accessible
    public Color getContrastColor(Color background) { // Now recognized as defined
        if (background == null) return Color.BLACK;
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255.0;
        return (luminance > 0.5) ? Color.BLACK : Color.WHITE;
    }


    // --- Undoable Edit Classes (Inner classes) ---
    private class AddFurnitureEdit extends AbstractUndoableEdit {
        private final Furniture addedFurniture;
        public AddFurnitureEdit(Furniture f) { this.addedFurniture = f; }
        @Override public String getPresentationName() { return "Add " + addedFurniture.getType(); }
        @Override public void undo() throws CannotUndoException {
            super.undo();
            designModel.removeFurniture(addedFurniture);
            updateUIFromModel(); // This will now update the price panel too
            designCanvas.repaint();
        }
        @Override public void redo() throws CannotRedoException {
            super.redo();
            designModel.addFurniture(addedFurniture);
            updateUIFromModel(); // This will now update the price panel too
            designCanvas.repaint();
        }
    }

    private class RemoveFurnitureEdit extends AbstractUndoableEdit {
        private final Furniture removedFurniture;

        public RemoveFurnitureEdit(Furniture f) {
            this.removedFurniture = f;
        }

        @Override
        public String getPresentationName() {
            return "Remove " + removedFurniture.getType();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            designModel.addFurniture(removedFurniture);
            updateUIFromModel(); // This already exists and should update all panels
            designCanvas.repaint();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            designModel.removeFurniture(removedFurniture);
            updateUIFromModel(); // This already exists and should update all panels
            designCanvas.repaint();
        }
    }
    private class MoveFurnitureEdit extends AbstractUndoableEdit { // Now recognized
        private final Furniture movedFurniture; private final Vector3f oldPos, newPos;
        public MoveFurnitureEdit(Furniture f, Vector3f oldP, Vector3f newP) {
            this.movedFurniture = f; this.oldPos = oldP.clone(); this.newPos = newP.clone(); }
        @Override public String getPresentationName() { return "Move " + movedFurniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); movedFurniture.setPosition(oldPos); designModel.setSelectedFurniture(movedFurniture); updateUIFromModel(); designCanvas.repaint(); }
        @Override public void redo() throws CannotRedoException { super.redo(); movedFurniture.setPosition(newPos); designModel.setSelectedFurniture(movedFurniture); updateUIFromModel(); designCanvas.repaint(); }
    }
    private class ChangeFurnitureDimensionsEdit extends AbstractUndoableEdit {
        private final Furniture furniture;
        private final float oldW, oldD, oldH, newW, newD, newH;

        public ChangeFurnitureDimensionsEdit(Furniture f, float nw, float nd, float nh) {
            this.furniture = f;
            this.oldW = f.getWidth(); this.oldD = f.getDepth(); this.oldH = f.getHeight();
            this.newW = nw; this.newD = nd; this.newH = nh;
            apply(newW, newD, newH); // Apply immediately
        }

        private void apply(float w, float d, float h) {
            furniture.setWidth(w); furniture.setDepth(d); furniture.setHeight(h);
        }

        @Override
        public String getPresentationName() {
            return "Resize " + furniture.getType();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            apply(oldW, oldD, oldH);
            designModel.setSelectedFurniture(furniture);
            updateUIFromModel(); // This will update all panels including price panel
            designCanvas.repaint();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            apply(newW, newD, newH);
            designModel.setSelectedFurniture(furniture);
            updateUIFromModel(); // This will update all panels including price panel
            designCanvas.repaint();
        }
    }

    private class ChangeFurnitureRotationEdit extends AbstractUndoableEdit { // Now recognized
        private final Furniture furniture; private final float oldRotY, newRotY;
        public ChangeFurnitureRotationEdit(Furniture f, float newRot) {
            this.furniture = f; this.oldRotY = f.getRotation().y; this.newRotY = newRot;
            apply(newRotY); // Apply immediately
        }
        private void apply(float rotY) { furniture.getRotation().y = rotY; }
        @Override public String getPresentationName() { return "Rotate " + furniture.getType(); }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldRotY); designModel.setSelectedFurniture(furniture); updateUIFromModel(); designCanvas.repaint(); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newRotY); designModel.setSelectedFurniture(furniture); updateUIFromModel(); designCanvas.repaint(); }
    }
    private class ChangeFurnitureAppearanceEdit extends AbstractUndoableEdit { // Now recognized
        private final Furniture furniture; private final Color oldColor, newColor; private final String oldTexture, newTexture;
        public ChangeFurnitureAppearanceEdit(Furniture f, Color c, String t) {
            this.furniture = f;
            this.oldColor = f.getColor(); this.oldTexture = f.getTexturePath();
            this.newColor = (c != null) ? c : oldColor;
            this.newTexture = t;
            apply(this.newColor, this.newTexture);
        }
        private void apply(Color c, String t) {
            furniture.setColor(c);
            if (t != null) furniture.setTexturePath(t.isEmpty() ? null : t);
        }
        @Override public String getPresentationName() { return "Change " + furniture.getType() + " Appearance"; }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldColor, oldTexture); designModel.setSelectedFurniture(furniture); updateUIFromModel(); designCanvas.repaint(); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newColor, newTexture); designModel.setSelectedFurniture(furniture); updateUIFromModel(); designCanvas.repaint(); }
    }
    private class ChangeRoomAppearanceEdit extends AbstractUndoableEdit { // Now recognized
        private final Room room; private final boolean isWall;
        private final Color oldColor, newColor; private final String oldTexture, newTexture;
        public ChangeRoomAppearanceEdit(Room r, boolean wall, Color c, String t) {
            this.room = r; this.isWall = wall;
            this.oldColor = wall ? r.getWallColor() : r.getFloorColor();
            this.oldTexture = wall ? r.getWallTexturePath() : r.getFloorTexturePath();
            this.newColor = (c != null) ? c : oldColor;
            this.newTexture = t;
            apply(this.newColor, this.newTexture);
        }
        private void apply(Color c, String t) {
            if (isWall) {
                room.setWallColor(c);
                if (t != null) room.setWallTexturePath(t.isEmpty() ? null : t);
            } else {
                room.setFloorColor(c);
                if (t != null) room.setFloorTexturePath(t.isEmpty() ? null : t);
            }
        }
        @Override public String getPresentationName() { return "Change " + (isWall ? "Wall" : "Floor") + " Appearance"; }
        @Override public void undo() throws CannotUndoException { super.undo(); apply(oldColor, oldTexture); updateUIFromModel(); designCanvas.repaint(); }
        @Override public void redo() throws CannotRedoException { super.redo(); apply(newColor, newTexture); updateUIFromModel(); designCanvas.repaint(); }
    }
    private class ChangeRoomPropertiesEdit extends AbstractUndoableEdit { // Now recognized
        private final Room room;
        private final Room.RoomShape oldShape, newShape;
        private final float oldH, newH;
        private final float oldW, newW, oldL, newL; private final float oldR, newR;
        private final float oldLoW, newLoW, oldLoL, newLoL, oldLiW, newLiW, oldLiL, newLiL;
        private final float oldTbW, newTbW, oldTbL, newTbL, oldTsW, newTsW, oldTsL, newTsL;
        public ChangeRoomPropertiesEdit(Room r, Room.RoomShape shape, float h, float w, float l, float rad, float loW, float loL, float liW, float liL, float tbW, float tbL, float tsW, float tsL) {
            this.room = r;
            this.oldShape = r.getShape(); this.oldH = r.getHeight(); this.oldW = r.getWidth(); this.oldL = r.getLength(); this.oldR = r.getRadius();
            this.oldLoW = r.getL_outerWidth(); this.oldLoL = r.getL_outerLength(); this.oldLiW = r.getL_insetWidth(); this.oldLiL = r.getL_insetLength();
            this.oldTbW = r.getT_barWidth(); this.oldTbL = r.getT_barLength(); this.oldTsW = r.getT_stemWidth(); this.oldTsL = r.getT_stemLength();
            this.newShape = shape; this.newH = h; this.newW = w; this.newL = l; this.newR = rad;
            this.newLoW = loW; this.newLoL = loL; this.newLiW = liW; this.newLiL = liL;
            this.newTbW = tbW; this.newTbL = tbL; this.newTsW = tsW; this.newTsL = tsL;
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
        private void afterChange() {
            designCanvas.repaint();
            updateUIFromModel();
            if(renderer != null && designModel.getRoom() != null) { renderer.updateCameraForModel(); }
        }
    }

}
