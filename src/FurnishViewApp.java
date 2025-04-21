import javax.swing.*;

class FurnishViewApp {
    public static void main(String[] args) {
        // Set look and feel (optional, but often improves appearance)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel.");
            // e.printStackTrace(); // Optional: print stack trace for debugging
        }

        // Run the application on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Start with the Login Frame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
