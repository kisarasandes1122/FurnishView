import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Floating window to display price information for the current design.
 * Can be toggled on/off and positioned anywhere on screen.
 */
public class FloatingPricePanel extends JDialog {

    private final MainAppFrame parentFrame;
    private DesignModel designModel;

    // UI Components
    private JLabel totalPriceLabel;
    private JTextArea breakdownTextArea;
    private JButton closeButton;
    private JButton minimizeButton;
    private JToggleButton pinButton;

    // State tracking
    private boolean isPinned = false;
    private Point lastPosition;

    // Color scheme matching existing UI
    private Color textColor = new Color(68, 68, 68);
    private Color backgroundColor = new Color(255, 255, 255);
    private Color accentColor = new Color(213, 204, 189);
    private Color subtleGray = new Color(240, 240, 238);
    private Color priceGreen = new Color(35, 120, 35);

    // Fonts
    private Font titleFont = new Font("Segoe UI", Font.BOLD, 16);
    private Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font priceFont = new Font("Segoe UI", Font.BOLD, 18);

    /**
     * Constructor
     * @param parentFrame The main application frame
     */
    public FloatingPricePanel(MainAppFrame parentFrame) {
        super(parentFrame, "Price Estimate", false); // false = non-modal
        this.parentFrame = parentFrame;

        // Basic dialog setup
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setResizable(true);
        setSize(320, 300);

        // Set initial position relative to parent frame
        positionRelativeToParent();

        // Create and set up the UI
        initializeUI();

        // Add dragging functionality
        addDragCapability();

        // Listen for parent window moves to maintain relative position if pinned
        parentFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (isPinned && isVisible()) {
                    positionRelativeToParent();
                }
            }
        });
    }

    /**
     * Position the dialog relative to the parent frame (right side)
     */
    private void positionRelativeToParent() {
        Point parentLocation = parentFrame.getLocation();
        Dimension parentSize = parentFrame.getSize();
        setLocation(parentLocation.x + parentSize.width - getWidth() - 20,
                parentLocation.y + 100);
        lastPosition = getLocation();
    }

    /**
     * Set up the user interface
     */
    private void initializeUI() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        // Header panel with title and controls
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Price content panel
        JPanel priceContentPanel = createPriceContentPanel();
        mainPanel.add(priceContentPanel, BorderLayout.CENTER);

        // Set the content pane
        setContentPane(mainPanel);
    }

    /**
     * Create the header panel with title and window controls
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, subtleGray),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));

        // Title label on the left
        JLabel titleLabel = new JLabel("Price Estimate");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(textColor);
        panel.add(titleLabel, BorderLayout.WEST);

        // Control buttons on the right
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlsPanel.setBackground(backgroundColor);

        // Pin button (toggle)
        pinButton = new JToggleButton("\uD83D\uDCCC"); // 📌 Unicode pin
        pinButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pinButton.setToolTipText("Pin to Main Window");
        pinButton.setFocusPainted(false);
        pinButton.setContentAreaFilled(false);
        pinButton.addActionListener(e -> {
            isPinned = pinButton.isSelected();
            if (isPinned) {
                positionRelativeToParent();
            }
        });

        // Minimize button
        minimizeButton = new JButton("_");
        minimizeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        minimizeButton.setToolTipText("Minimize");
        minimizeButton.setFocusPainted(false);
        minimizeButton.setContentAreaFilled(false);
        minimizeButton.addActionListener(e -> {
            setVisible(false);
        });

        // Close button
        closeButton = new JButton("✕");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setToolTipText("Close");
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> {
            setVisible(false);
        });

        controlsPanel.add(pinButton);
        controlsPanel.add(minimizeButton);
        controlsPanel.add(closeButton);
        panel.add(controlsPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create the panel containing price information
     */
    private JPanel createPriceContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);

        // Total price section
        JPanel totalPricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPricePanel.setBackground(backgroundColor);

        JLabel totalLabel = new JLabel("Total Estimated Cost: ");
        totalLabel.setFont(mainFont);

        totalPriceLabel = new JLabel("Rs.0.00");
        totalPriceLabel.setFont(priceFont);
        totalPriceLabel.setForeground(priceGreen);

        totalPricePanel.add(totalLabel);
        totalPricePanel.add(totalPriceLabel);
        totalPricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(totalPricePanel);
        panel.add(Box.createVerticalStrut(10));

        // Breakdown section
        JLabel breakdownLabel = new JLabel("Breakdown by Item:");
        breakdownLabel.setFont(mainFont);
        breakdownLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(breakdownLabel);
        panel.add(Box.createVerticalStrut(5));

        breakdownTextArea = new JTextArea();
        breakdownTextArea.setEditable(false);
        breakdownTextArea.setFont(mainFont);
        breakdownTextArea.setBackground(new Color(250, 250, 250));
        breakdownTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        breakdownTextArea.setLineWrap(true);
        breakdownTextArea.setWrapStyleWord(true);
        breakdownTextArea.setText("Add furniture to see price breakdown");

        JScrollPane scrollPane = new JScrollPane(breakdownTextArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Note about estimates
        JLabel noteLabel = new JLabel("<html><i>Note: Prices are estimates and may vary based on actual materials and specifications.</i></html>");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(noteLabel);

        return panel;
    }

    /**
     * Add mouse listeners to make the window draggable
     */
    private void addDragCapability() {
        // Track the initial mouse position and window position
        final Point[] dragStart = {null};

        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart[0] = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart[0] = null;
                lastPosition = getLocation();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null && !isPinned) {
                    Point currentMouse = e.getLocationOnScreen();
                    setLocation(
                            currentMouse.x - dragStart[0].x,
                            currentMouse.y - dragStart[0].y
                    );
                }
            }
        };

        // Apply the mouse listeners to the content pane
        Container contentPane = getContentPane();
        contentPane.addMouseListener(dragAdapter);
        contentPane.addMouseMotionListener(dragAdapter);

        // Make sure child components pass mouse events to the parent
        MouseAdapter childAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Pass the event to the parent
                Component component = e.getComponent();
                Point point = e.getPoint();
                SwingUtilities.convertPointToScreen(point, component);
                Point contentPoint = new Point(point);
                SwingUtilities.convertPointFromScreen(contentPoint, contentPane);
                contentPane.dispatchEvent(new MouseEvent(
                        contentPane, e.getID(), e.getWhen(), e.getModifiers(),
                        contentPoint.x, contentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton()
                ));
            }
        };

        // Apply the child adapter to relevant components
        applyChildMouseAdapter(contentPane, childAdapter);
    }

    /**
     * Recursively apply the mouse adapter to components
     * that shouldn't intercept drag events
     */
    private void applyChildMouseAdapter(Container container, MouseAdapter adapter) {
        for (Component component : container.getComponents()) {
            // Skip buttons, scroll panes, and text areas that need their own mouse events
            if (!(component instanceof JButton) &&
                    !(component instanceof JToggleButton) &&
                    !(component instanceof JScrollPane) &&
                    !(component instanceof JTextArea)) {

                component.addMouseListener(adapter);

                if (component instanceof Container) {
                    applyChildMouseAdapter((Container) component, adapter);
                }
            }
        }
    }

    /**
     * Update the UI with data from the design model
     */
    public void updatePriceData(DesignModel designModel) {
        this.designModel = designModel;

        if (designModel == null) {
            totalPriceLabel.setText("Rs.0.00");
            breakdownTextArea.setText("Add furniture to see price breakdown");
            return;
        }

        // Calculate total price
        double totalPrice = designModel.calculateTotalPrice();
        DecimalFormat df = new DecimalFormat("0.00");
        totalPriceLabel.setText("Rs." + df.format(totalPrice));

        // Get price breakdown
        Map<String, Double> priceBreakdown = designModel.getPriceBreakdown();
        Map<String, Integer> furnitureCounts = designModel.getFurnitureCounts();

        // Format breakdown text
        if (priceBreakdown.isEmpty()) {
            breakdownTextArea.setText("Add furniture to see price breakdown");
        } else {
            StringBuilder sb = new StringBuilder();

            // Sort items by price (highest first)
            priceBreakdown.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .forEach(entry -> {
                        String type = entry.getKey();
                        double price = entry.getValue();
                        int count = furnitureCounts.getOrDefault(type, 0);

                        sb.append(type)
                                .append(" (")
                                .append(count)
                                .append(count > 1 ? " items): Rs." : " item): Rs.")
                                .append(df.format(price))
                                .append("\n");
                    });

            breakdownTextArea.setText(sb.toString());
        }
    }

    /**
     * Toggle visibility of the panel
     */
    public void toggleVisibility() {
        setVisible(!isVisible());
        if (isVisible() && isPinned) {
            positionRelativeToParent();
        } else if (isVisible() && lastPosition != null) {
            setLocation(lastPosition);
        }
    }
}