import java.awt.Color;
import java.io.Serializable;

class Room implements Serializable {
    private static final long serialVersionUID = 2L; // Increment version ID

    // --- NEW: Enum for Shape Type ---
    public enum RoomShape {
        RECTANGULAR("Rectangular/Square"), // Display name
        CIRCULAR("Circular"),
        L_SHAPED("L-Shaped"),
        T_SHAPED("T-Shaped");

        private final String displayName;
        RoomShape(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // --- Shape and Dimensions ---
    private RoomShape shape = RoomShape.RECTANGULAR; // Default shape
    private float height;

    // Rectangular (and Square) Parameters
    private float width, length;

    // Circular Parameters
    private float radius;

    // L-Shape Parameters (One way to define it)
    // Imagine top-left corner is origin (0,0)
    // Outer rectangle goes from (0,0) to (outerWidth, outerLength)
    // Inset is removed from top-right corner
    private float l_outerWidth;  // Total width
    private float l_outerLength; // Total length (depth)
    private float l_insetWidth;  // Width of the part sticking out (horizontal leg)
    private float l_insetLength; // Length of the part sticking out (vertical leg)

    // T-Shape Parameters (One way to define it)
    // Imagine main horizontal bar along X, stem going down in Z
    private float t_barWidth;    // Width of the top bar
    private float t_barLength;   // Length (depth) of the top bar
    private float t_stemWidth;   // Width of the vertical stem
    private float t_stemLength;  // Length (depth) of the vertical stem (added to bar length)

    // --- Appearance ---
    private Color wallColor;
    private Color floorColor;
    private String wallTexturePath;
    private String floorTexturePath;

    // --- Constructor (Example: Default Rectangular) ---
    public Room(float width, float length, float height) {
        this.shape = RoomShape.RECTANGULAR;
        this.width = width;
        this.length = length;
        this.height = height;
        // Set defaults for other shapes too? Or require explicit setting.
        this.radius = Math.min(width, length) / 2.0f;
        this.l_outerWidth = width;
        this.l_outerLength = length;
        this.l_insetWidth = width * 0.4f;
        this.l_insetLength = length * 0.4f;
        this.t_barWidth = width;
        this.t_barLength = length * 0.3f;
        this.t_stemWidth = width * 0.3f;
        this.t_stemLength = length * 0.7f;

        this.wallColor = Color.WHITE;
        this.floorColor = Color.LIGHT_GRAY;
    }

    // --- Getters ---
    public RoomShape getShape() { return shape; }
    public float getHeight() { return height; }

    // Rectangular getters
    public float getWidth() { return width; } // Use specific getters below for clarity
    public float getLength() { return length; }

    // Circular getters
    public float getRadius() { return radius; }

    // L-Shape getters
    public float getL_outerWidth() { return l_outerWidth; }
    public float getL_outerLength() { return l_outerLength; }
    public float getL_insetWidth() { return l_insetWidth; }
    public float getL_insetLength() { return l_insetLength; }

    // T-Shape getters
    public float getT_barWidth() { return t_barWidth; }
    public float getT_barLength() { return t_barLength; }
    public float getT_stemWidth() { return t_stemWidth; }
    public float getT_stemLength() { return t_stemLength; }

    // Appearance getters
    public Color getWallColor() { return wallColor; }
    public Color getFloorColor() { return floorColor; }
    public String getWallTexturePath() { return wallTexturePath; }
    public String getFloorTexturePath() { return floorTexturePath; }


    // --- Setters ---
    public void setShape(RoomShape shape) { this.shape = shape; }
    public void setHeight(float height) { this.height = height; }

    // Rectangular setters
    public void setWidth(float width) { this.width = width; }
    public void setLength(float length) { this.length = length; }

    // Circular setters
    public void setRadius(float radius) { this.radius = radius; }

    // L-Shape setters
    public void setL_outerWidth(float l_outerWidth) { this.l_outerWidth = l_outerWidth; }
    public void setL_outerLength(float l_outerLength) { this.l_outerLength = l_outerLength; }
    public void setL_insetWidth(float l_insetWidth) { this.l_insetWidth = l_insetWidth; }
    public void setL_insetLength(float l_insetLength) { this.l_insetLength = l_insetLength; }

    // T-Shape setters
    public void setT_barWidth(float t_barWidth) { this.t_barWidth = t_barWidth; }
    public void setT_barLength(float t_barLength) { this.t_barLength = t_barLength; }
    public void setT_stemWidth(float t_stemWidth) { this.t_stemWidth = t_stemWidth; }
    public void setT_stemLength(float t_stemLength) { this.t_stemLength = t_stemLength; }


    // Appearance setters
    public void setWallColor(Color wallColor) { this.wallColor = wallColor; }
    public void setFloorColor(Color floorColor) { this.floorColor = floorColor; }
    public void setWallTexturePath(String wallTexturePath) { this.wallTexturePath = wallTexturePath; }
    public void setFloorTexturePath(String floorTexturePath) { this.floorTexturePath = floorTexturePath; }

    // --- Convenience Method to get Center (Approximate for L/T) ---
    // Needed for CameraManager.resetTargetToCenter
    public Vector3f calculateCenter() {
        switch (shape) {
            case RECTANGULAR:
                return new Vector3f(width / 2.0f, 0, length / 2.0f);
            case CIRCULAR:
                // Assuming circle center is at (0,0) in its local space for now
                return new Vector3f(0, 0, 0); // Adjust if room origin != circle center
            case L_SHAPED:
                // Simple bounding box center - inaccurate but works for now
                return new Vector3f(l_outerWidth / 2.0f, 0, l_outerLength / 2.0f);
            case T_SHAPED:
                // Simple bounding box center - inaccurate but works for now
                float totalLength = t_barLength + t_stemLength;
                return new Vector3f(t_barWidth / 2.0f, 0, totalLength / 2.0f);
            default:
                return new Vector3f(0, 0, 0);
        }
    }
}
