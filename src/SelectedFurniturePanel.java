// START OF MODIFIED SelectedFurniturePanel.java

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder; // **** ADDED ****
import java.awt.*;

// The rest of the SelectedFurniturePanel class remains the same...
public class SelectedFurniturePanel {

    private final MainAppFrame mainAppFrame; // Reference to main application

    // UI Components managed by this panel
    private JPanel mainPanel;
    private JPanel furnitureDimsPanel; // Container for dimension fields/buttons
    private JPanel furnitureAppearancePanel; // Container for appearance buttons
    private JTextField furnWidthField, furnDepthField, furnHeightField, furnRotationYField;
    private JButton furnitureColorButton, furnitureTextureButton;
    private JButton deleteFurnitureButton;
    private JButton updateFurnDimsButton, updateFurnRotationButton; // Keep buttons here

    public SelectedFurniturePanel(MainAppFrame mainAppFrame) {
        this.mainAppFrame = mainAppFrame;
        createPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    private void createPanel() {
        mainPanel = createSectionPanel("Selected Furniture");
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // --- Dimensions and Rotation ---
        furnitureDimsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        furnitureDimsPanel.setBorder(BorderFactory.createTitledBorder("Properties")); // TitledBorder used here
        furnWidthField = new JTextField("1.00");
        furnDepthField = new JTextField("1.00");
        furnHeightField = new JTextField("1.00");
        furnRotationYField = new JTextField("0.0");

        furnitureDimsPanel.add(new JLabel("Width:")); furnitureDimsPanel.add(furnWidthField);
        furnitureDimsPanel.add(new JLabel("Depth:")); furnitureDimsPanel.add(furnDepthField);
        furnitureDimsPanel.add(new JLabel("Height:")); furnitureDimsPanel.add(furnHeightField);
        furnitureDimsPanel.add(new JLabel("Rotation Y:")); furnitureDimsPanel.add(furnRotationYField);
        furnitureDimsPanel.add(new JLabel("")); // Spacer

        updateFurnDimsButton = new JButton("Update Dimensions");
        updateFurnDimsButton.addActionListener(e -> mainAppFrame.handleUpdateFurnitureDimensions());
        furnitureDimsPanel.add(updateFurnDimsButton);
        furnitureDimsPanel.add(new JLabel("")); // Spacer

        updateFurnRotationButton = new JButton("Update Rotation");
        updateFurnRotationButton.addActionListener(e -> mainAppFrame.handleUpdateFurnitureRotation());
        furnitureDimsPanel.add(updateFurnRotationButton);

        furnitureDimsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(furnitureDimsPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Appearance ---
        furnitureAppearancePanel = new JPanel();
        furnitureAppearancePanel.setLayout(new BoxLayout(furnitureAppearancePanel, BoxLayout.Y_AXIS));
        furnitureAppearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance")); // TitledBorder used here

        furnitureColorButton = new JButton("Set Furniture Color");
        furnitureColorButton.addActionListener(e -> mainAppFrame.handleSetFurnitureColor());

        furnitureTextureButton = new JButton("Set Furniture Texture...");
        furnitureTextureButton.addActionListener(e -> mainAppFrame.handleSetFurnitureTexture());

        furnitureColorButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        furnitureTextureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        furnitureAppearancePanel.add(furnitureColorButton);
        furnitureAppearancePanel.add(Box.createVerticalStrut(5));
        furnitureAppearancePanel.add(furnitureTextureButton);

        furnitureAppearancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(furnitureAppearancePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Delete Button ---
        deleteFurnitureButton = new JButton("Delete Selected Furniture");
        deleteFurnitureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteFurnitureButton.addActionListener(e -> mainAppFrame.handleDeleteSelectedFurniture());
        mainPanel.add(deleteFurnitureButton);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title)); // TitledBorder used here
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /** Updates the UI components within this panel based on the model */
    public void updateUI(DesignModel designModel) {
        Furniture selected = (designModel != null) ? designModel.getSelectedFurniture() : null;
        boolean furnitureSelected = (selected != null);

        setPanelEnabled(furnitureDimsPanel, furnitureSelected);
        setPanelEnabled(furnitureAppearancePanel, furnitureSelected);
        deleteFurnitureButton.setEnabled(furnitureSelected);

        if (furnitureSelected) {
            if (!furnWidthField.hasFocus()) furnWidthField.setText(String.format("%.2f", selected.getWidth()));
            if (!furnDepthField.hasFocus()) furnDepthField.setText(String.format("%.2f", selected.getDepth()));
            if (!furnHeightField.hasFocus()) furnHeightField.setText(String.format("%.2f", selected.getHeight()));
            if (!furnRotationYField.hasFocus()) furnRotationYField.setText(String.format("%.1f", selected.getRotation().y));

            furnitureColorButton.setBackground(selected.getColor());
            furnitureColorButton.setForeground(mainAppFrame.getContrastColor(selected.getColor()));
        } else {
            furnitureColorButton.setBackground(UIManager.getColor("Button.background"));
            furnitureColorButton.setForeground(UIManager.getColor("Button.foreground"));
        }
    }

    // Helper to enable/disable all components within a container
    private void setPanelEnabled(Container container, boolean enabled) {
        container.setEnabled(enabled);
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof Container) {
                // Simplified: Just enable/disable direct children for this layout
                component.setEnabled(enabled);
            } else {
                component.setEnabled(enabled);
            }
        }
        // Check if the border is a TitledBorder (now recognized)
        if (container instanceof JComponent && ((JComponent)container).getBorder() instanceof TitledBorder) {
            // No easy way to change title color based on enabled state, but the check is now valid
        }
    }

    // --- Getters ---
    public JTextField getFurnWidthField() { return furnWidthField; }
    public JTextField getFurnDepthField() { return furnDepthField; }
    public JTextField getFurnHeightField() { return furnHeightField; }
    public JTextField getFurnRotationYField() { return furnRotationYField; }
    public JButton getDeleteFurnitureButton() { return deleteFurnitureButton; }
    public JButton getFurnitureColorButton() { return furnitureColorButton; }
    public JButton getFurnitureTextureButton() { return furnitureTextureButton; }
}
// END OF MODIFIED SelectedFurniturePanel.java