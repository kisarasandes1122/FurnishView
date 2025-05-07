import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Admin panel for managing furniture inventory and prices.
 */
public class InventoryManagementFrame extends JFrame {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton saveButton;
    private JButton cancelButton;
    private JFormattedTextField basePriceField;
    private JFormattedTextField pricePerVolumeField;

    // Updated color palette with natural, desaturated tones (same as ProjectDashboardFrame)
    private Color textColor = new Color(68, 68, 68);      // Dark gray instead of black
    private Color backgroundColor = new Color(255, 255, 255); // Soft warm white
    private Color accentColor = new Color(213, 204, 189); // Light taupe/beige
    private Color subtleGray = new Color(240, 240, 238);  // Very light gray for dividers
    private Color headerBgColor = new Color(245, 244, 240); // Barely-there background for header

    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
    private Font headerFont = new Font("Segoe UI", Font.BOLD, 16);
    private Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

    private boolean changesMade = false;
    private Map<String, FurniturePrice> originalPrices;

    public InventoryManagementFrame(JFrame parent) {
        // Set frame properties
        setTitle("FurnishView - Inventory Management");
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
        });

        // Store original prices for comparison
        originalPrices = InventoryManager.getAllFurniturePrices();

        // Create main content panel
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPane.setBackground(backgroundColor);
        setContentPane(contentPane);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Center panel with table
        JPanel centerPanel = createTablePanel();
        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with editor form
        JPanel editorPanel = createEditorPanel();
        contentPane.add(editorPanel, BorderLayout.SOUTH);

        // Load inventory data
        loadInventoryData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, subtleGray),
                BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));

        JLabel titleLabel = new JLabel("Furniture Inventory Management");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(textColor);
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(backgroundColor);

        // Table model
        String[] columns = {"Furniture Type", "Base Price (Rs.)", "Price per Unit Volume (Rs.)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0; // Allow editing price columns
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(30);
        inventoryTable.setFont(mainFont);
        inventoryTable.setForeground(textColor);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Style the table
        inventoryTable.setBackground(backgroundColor);
        inventoryTable.setGridColor(subtleGray);
        inventoryTable.setShowGrid(false);
        inventoryTable.setIntercellSpacing(new Dimension(10, 5));
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        inventoryTable.getTableHeader().setForeground(textColor);
        inventoryTable.getTableHeader().setBackground(headerBgColor);
        inventoryTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, subtleGray));

        // Selection listener
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(subtleGray, 1));
        scrollPane.getViewport().setBackground(backgroundColor);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, subtleGray),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)
        ));

        // Editor form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Base price field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel basePriceLabel = new JLabel("Base Price (Rs.):");
        basePriceLabel.setFont(mainFont);
        formPanel.add(basePriceLabel, gbc);

        gbc.gridx = 1;
        NumberFormat currencyFormat = new DecimalFormat("#0.00");
        basePriceField = new JFormattedTextField(currencyFormat);
        basePriceField.setColumns(10);
        basePriceField.setFont(mainFont);
        formPanel.add(basePriceField, gbc);

        // Price per volume field
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel pricePerVolumeLabel = new JLabel("Price per Unit Volume (Rs.):");
        pricePerVolumeLabel.setFont(mainFont);
        formPanel.add(pricePerVolumeLabel, gbc);

        gbc.gridx = 1;
        pricePerVolumeField = new JFormattedTextField(currencyFormat);
        pricePerVolumeField.setColumns(10);
        pricePerVolumeField.setFont(mainFont);
        formPanel.add(pricePerVolumeField, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(backgroundColor);

        saveButton = new JButton("Update Selected Item");
        saveButton.setFont(buttonFont);
        saveButton.setEnabled(false);
        applyFlatButtonStyle(saveButton, false);
        saveButton.addActionListener(e -> handleSaveButtonClick());

        cancelButton = new JButton("Close");
        cancelButton.setFont(buttonFont);
        applyFlatButtonStyle(cancelButton, true);
        cancelButton.addActionListener(e -> handleClose());

        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Load inventory data into the table
     */
    private void loadInventoryData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Get all furniture prices
        Map<String, FurniturePrice> prices = InventoryManager.getAllFurniturePrices();

        // Add to table
        DecimalFormat df = new DecimalFormat("0.00");
        for (FurniturePrice price : prices.values()) {
            Object[] rowData = {
                    price.getFurnitureType(),
                    df.format(price.getBasePrice()),
                    df.format(price.getPricePerUnitVolume())
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * Handle table row selection
     */
    private void handleTableSelection() {
        int selectedRow = inventoryTable.getSelectedRow();

        if (selectedRow != -1) {
            // Get selected furniture type
            String furnitureType = (String) tableModel.getValueAt(selectedRow, 0);

            // Get pricing info
            FurniturePrice priceInfo = InventoryManager.getFurniturePrice(furnitureType);

            if (priceInfo != null) {
                // Update form fields
                basePriceField.setValue(priceInfo.getBasePrice());
                pricePerVolumeField.setValue(priceInfo.getPricePerUnitVolume());

                // Enable save button
                saveButton.setEnabled(true);
            }
        } else {
            // Clear form and disable save button
            basePriceField.setValue(0.0);
            pricePerVolumeField.setValue(0.0);
            saveButton.setEnabled(false);
        }
    }

    /**
     * Handle save button click
     */
    private void handleSaveButtonClick() {
        int selectedRow = inventoryTable.getSelectedRow();

        if (selectedRow != -1) {
            try {
                // Get values from form
                String furnitureType = (String) tableModel.getValueAt(selectedRow, 0);
                double basePrice = ((Number) basePriceField.getValue()).doubleValue();
                double pricePerVolume = ((Number) pricePerVolumeField.getValue()).doubleValue();

                // Validate values
                if (basePrice < 0 || pricePerVolume < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Prices cannot be negative.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update price in inventory manager
                boolean success = InventoryManager.updateFurniturePrice(
                        furnitureType, basePrice, pricePerVolume);

                if (success) {
                    // Update table
                    DecimalFormat df = new DecimalFormat("0.00");
                    tableModel.setValueAt(df.format(basePrice), selectedRow, 1);
                    tableModel.setValueAt(df.format(pricePerVolume), selectedRow, 2);

                    changesMade = true;

                    JOptionPane.showMessageDialog(this,
                            "Price updated successfully for " + furnitureType,
                            "Update Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update price. Please try again.",
                            "Update Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid input: " + ex.getMessage(),
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handle close button or window close
     */
    private void handleClose() {
        if (changesMade) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Close inventory management? Your changes have been saved.",
                    "Confirm Close",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }

    /**
     * Apply flat button style similar to the dashboard
     */
    private void applyFlatButtonStyle(JButton button, boolean isCancel) {
        // Set colors based on button type
        Color defaultBg = isCancel ? new Color(220, 220, 220) : new Color(102, 102, 102);
        Color hoverBg = isCancel ? new Color(200, 200, 200) : new Color(80, 80, 80);
        Color pressedBg = isCancel ? new Color(180, 180, 180) : new Color(60, 60, 60);
        Color textColor = isCancel ? new Color(68, 68, 68) : Color.WHITE;

        button.setBackground(defaultBg);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(8, 16, 8, 16));

        // Add mouse listeners for hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(defaultBg);
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(pressedBg);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    if (button.contains(evt.getPoint())) {
                        button.setBackground(hoverBg);
                    } else {
                        button.setBackground(defaultBg);
                    }
                }
            }
        });
    }
}