// Modified RoomPropertiesPanel.java
// This version has been updated to prevent color buttons from changing appearance

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;

public class RoomPropertiesPanel {

    private final MainAppFrame mainAppFrame; // Reference to main application

    // UI Components managed by this panel
    private JPanel mainPanel;
    private JComboBox<Room.RoomShape> roomShapeComboBox;
    private JPanel roomParameterCardsPanel;
    private CardLayout roomParameterCardLayout;
    private JTextField roomWidthField, roomLengthField;
    private JTextField roomRadiusField;
    private JTextField lOuterWidthField, lOuterLengthField, lInsetWidthField, lInsetLengthField;
    private JTextField tBarWidthField, tBarLengthField, tStemWidthField, tStemLengthField;
    private JTextField sharedHeightField; // Shared across cards
    private JButton wallColorButton, floorColorButton;
    private JButton resetWallAppearanceButton, resetFloorAppearanceButton;

    private ActionListener roomShapeActionListener; // Store listener

    // Constants for CardLayout
    private static final String RECT_CARD = "Rectangular";
    private static final String CIRC_CARD = "Circular";
    private static final String L_CARD = "L-Shaped";
    private static final String T_CARD = "T-Shaped";

    // Constants for button colors
    private static final Color BUTTON_BACKGROUND = new Color(64, 64, 64);
    private static final Color BUTTON_FOREGROUND = Color.WHITE;

    public RoomPropertiesPanel(MainAppFrame mainAppFrame) {
        this.mainAppFrame = mainAppFrame;
        createPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    private void createPanel() {
        mainPanel = createSectionPanel("Room Properties");
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Shape Selection
        JPanel shapeSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        shapeSelectPanel.add(new JLabel("Shape:"));
        roomShapeComboBox = new JComboBox<>(Room.RoomShape.values());
        shapeSelectPanel.add(roomShapeComboBox);
        shapeSelectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        shapeSelectPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, shapeSelectPanel.getPreferredSize().height));
        mainPanel.add(shapeSelectPanel);
        mainPanel.add(Box.createVerticalStrut(5));

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

        mainPanel.add(roomParameterCardsPanel);
        mainPanel.add(Box.createVerticalStrut(5));

        // Action Listener for shape combo box
        roomShapeActionListener = e -> {
            Room.RoomShape selectedShape = (Room.RoomShape) roomShapeComboBox.getSelectedItem();
            if (selectedShape != null) {
                switch (selectedShape) {
                    case RECTANGULAR: roomParameterCardLayout.show(roomParameterCardsPanel, RECT_CARD); break;
                    case CIRCULAR:    roomParameterCardLayout.show(roomParameterCardsPanel, CIRC_CARD); break;
                    case L_SHAPED:    roomParameterCardLayout.show(roomParameterCardsPanel, L_CARD); break;
                    case T_SHAPED:    roomParameterCardLayout.show(roomParameterCardsPanel, T_CARD); break;
                }
                // Update layout
                roomParameterCardsPanel.revalidate();
                roomParameterCardsPanel.repaint();
            }
        };
        roomShapeComboBox.addActionListener(roomShapeActionListener);

        JButton updateRoomSizeButton = new JButton("Update Dimensions");
        // Call the handler method in MainAppFrame
        updateRoomSizeButton.addActionListener(e -> mainAppFrame.handleUpdateRoomSize());
        updateRoomSizeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(updateRoomSizeButton);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Room Appearance ---
        JPanel roomAppearancePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        roomAppearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));

        wallColorButton = new JButton("Set Wall Color");
        floorColorButton = new JButton("Set Floor Color");
        JButton wallTextureButton = new JButton("Set Wall Texture");
        JButton floorTextureButton = new JButton("Set Floor Texture");
        resetWallAppearanceButton = new JButton("Reset Wall Appearance");
        resetFloorAppearanceButton = new JButton("Reset Floor Appearance");

        // Set fixed appearance for color buttons
        applyFixedButtonStyle(wallColorButton);
        applyFixedButtonStyle(floorColorButton);
        applyFixedButtonStyle(wallTextureButton);
        applyFixedButtonStyle(floorTextureButton);
        applyFixedButtonStyle(resetWallAppearanceButton);
        applyFixedButtonStyle(resetFloorAppearanceButton);

        wallColorButton.addActionListener(e -> mainAppFrame.handleSetRoomColor(true));
        floorColorButton.addActionListener(e -> mainAppFrame.handleSetRoomColor(false));
        wallTextureButton.addActionListener(e -> mainAppFrame.handleSetRoomTexture(true));
        floorTextureButton.addActionListener(e -> mainAppFrame.handleSetRoomTexture(false));
        resetWallAppearanceButton.addActionListener(e -> mainAppFrame.handleResetRoomAppearance(true));
        resetFloorAppearanceButton.addActionListener(e -> mainAppFrame.handleResetRoomAppearance(false));

        roomAppearancePanel.add(wallColorButton);
        roomAppearancePanel.add(floorColorButton);
        roomAppearancePanel.add(wallTextureButton);
        roomAppearancePanel.add(floorTextureButton);
        roomAppearancePanel.add(resetWallAppearanceButton);
        roomAppearancePanel.add(resetFloorAppearanceButton);

        roomAppearancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(roomAppearancePanel);
    }

    /**
     * Applies a fixed style to the button regardless of color selection
     */
    private void applyFixedButtonStyle(JButton button) {
        button.setBackground(BUTTON_BACKGROUND);
        button.setForeground(BUTTON_FOREGROUND);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setMargin(new Insets(5, 10, 5, 10));

        // Override UI to remove any platform-specific styling
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            protected void paintButtonPressed(Graphics g, AbstractButton b) {
                // Override to prevent pressed effect - make it stay the same color
                g.setColor(BUTTON_BACKGROUND);
                g.fillRect(0, 0, b.getWidth(), b.getHeight());
            }
        });
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * Updates the UI components within this panel based on the model
     * MODIFIED to prevent color button appearance changes
     */
    public void updateUI(DesignModel designModel) {
        if (designModel == null || designModel.getRoom() == null) return;
        Room room = designModel.getRoom();

        // Temporarily remove listener to prevent combo box update from re-triggering handlers
        if (roomShapeComboBox != null && roomShapeActionListener != null) roomShapeComboBox.removeActionListener(roomShapeActionListener);
        if (roomShapeComboBox != null) roomShapeComboBox.setSelectedItem(room.getShape());
        if (roomShapeComboBox != null && roomShapeActionListener != null) roomShapeComboBox.addActionListener(roomShapeActionListener);

        // Show the correct card panel
        if (roomParameterCardLayout != null && roomParameterCardsPanel != null) {
            String cardToShow = RECT_CARD;
            switch (room.getShape()) {
                case CIRCULAR:    cardToShow = CIRC_CARD; break;
                case L_SHAPED:    cardToShow = L_CARD; break;
                case T_SHAPED:    cardToShow = T_CARD; break;
            }
            roomParameterCardLayout.show(roomParameterCardsPanel, cardToShow);
        }

        // Update dimension fields on the correct card
        sharedHeightField.setText(String.format("%.2f", room.getHeight()));
        switch (room.getShape()) {
            case RECTANGULAR:
                roomWidthField.setText(String.format("%.2f", room.getWidth()));
                roomLengthField.setText(String.format("%.2f", room.getLength()));
                break;
            case CIRCULAR:
                roomRadiusField.setText(String.format("%.2f", room.getRadius()));
                break;
            case L_SHAPED:
                lOuterWidthField.setText(String.format("%.2f", room.getL_outerWidth()));
                lOuterLengthField.setText(String.format("%.2f", room.getL_outerLength()));
                lInsetWidthField.setText(String.format("%.2f", room.getL_insetWidth()));
                lInsetLengthField.setText(String.format("%.2f", room.getL_insetLength()));
                break;
            case T_SHAPED:
                tBarWidthField.setText(String.format("%.2f", room.getT_barWidth()));
                tBarLengthField.setText(String.format("%.2f", room.getT_barLength()));
                tStemWidthField.setText(String.format("%.2f", room.getT_stemWidth()));
                tStemLengthField.setText(String.format("%.2f", room.getT_stemLength()));
                break;
        }

        // REMOVED: The code that would change button colors has been removed
        // We're now using fixed button styles instead
    }

    // --- Getters for MainAppFrame handlers ---
    // These are needed by MainAppFrame's handleUpdateRoomSize method
    public JComboBox<Room.RoomShape> getRoomShapeComboBox() { return roomShapeComboBox; }
    public JTextField getSharedHeightField() { return sharedHeightField; }
    public JTextField getRoomWidthField() { return roomWidthField; }
    public JTextField getRoomLengthField() { return roomLengthField; }
    public JTextField getRoomRadiusField() { return roomRadiusField; }
    public JTextField getLOuterWidthField() { return lOuterWidthField; }
    public JTextField getLOuterLengthField() { return lOuterLengthField; }
    public JTextField getLInsetWidthField() { return lInsetWidthField; }
    public JTextField getLInsetLengthField() { return lInsetLengthField; }
    public JTextField getTBarWidthField() { return tBarWidthField; }
    public JTextField getTBarLengthField() { return tBarLengthField; }
    public JTextField getTStemWidthField() { return tStemWidthField; }
    public JTextField getTStemLengthField() { return tStemLengthField; }
}