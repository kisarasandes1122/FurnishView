// START OF FILE FurnitureLibraryPanel.java

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class FurnitureLibraryPanel {

    private final MainAppFrame mainAppFrame;

    // UI Components managed by this panel
    private JPanel mainPanel;
    private JList<String> furnitureLibraryList;

    // Need access to the data used by the list
    private static final String[] FURNITURE_TYPES = MainAppFrame.getFurnitureTypes(); // Get from MainAppFrame
    private static final float[][] FURNITURE_DIMS = MainAppFrame.getFurnitureDims();  // Get from MainAppFrame

    public FurnitureLibraryPanel(MainAppFrame mainAppFrame) {
        this.mainAppFrame = mainAppFrame;
        createPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    private void createPanel() {
        mainPanel = createSectionPanel("Furniture Library");
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        furnitureLibraryList = new JList<>(FURNITURE_TYPES);
        furnitureLibraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        furnitureLibraryList.setVisibleRowCount(6);

        // List selection listener updates the "Selected Furniture" panel's fields
        // (if nothing is selected in the scene) via a method in MainAppFrame.
        furnitureLibraryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mainAppFrame.handleLibrarySelectionChange(furnitureLibraryList.getSelectedIndex());
            }
        });

        JScrollPane libraryScrollPane = new JScrollPane(furnitureLibraryList);
        libraryScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        libraryScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, libraryScrollPane.getPreferredSize().height * 2));
        mainPanel.add(libraryScrollPane);
        mainPanel.add(Box.createVerticalStrut(5));

        JButton addFurnitureButton = new JButton("Add Selected Furniture");
        addFurnitureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Call the handler method in MainAppFrame
        addFurnitureButton.addActionListener(e -> mainAppFrame.handleAddFurniture());
        mainPanel.add(addFurnitureButton);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /** Updates the UI components within this panel based on the model */
    public void updateUI(DesignModel designModel) {
        // If furniture is selected in the model, clear the library selection
        if (designModel != null && designModel.getSelectedFurniture() != null) {
            furnitureLibraryList.clearSelection();
        }
        // Otherwise, the selection is driven by user interaction
    }

    // --- Getter needed by MainAppFrame ---
    public JList<String> getFurnitureLibraryList() {
        return furnitureLibraryList;
    }
}
// END OF FILE FurnitureLibraryPanel.java