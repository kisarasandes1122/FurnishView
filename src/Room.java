import java.awt.Color;
import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RoomShape {
        RECTANGULAR("Rectangular"),
        CIRCULAR("Circular"),
        L_SHAPED("L-Shaped"),
        T_SHAPED("T-Shaped");

        private final String displayName;
        RoomShape(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // Common properties
    private RoomShape shape;
    private float height;
    private Color wallColor;
    private Color floorColor;
    private String wallTexturePath;
    private String floorTexturePath;

    // Shape-specific dimensions
    // Rectangular
    private float width;
    private float length;
    // Circular
    private float radius;
    // L-Shaped
    private float l_outerWidth;
    private float l_outerLength;
    private float l_insetWidth;
    private float l_insetLength;
    // T-Shaped
    private float t_barWidth;
    private float t_barLength;
    private float t_stemWidth;
    private float t_stemLength;

    // --- Constructors ---

    // Default constructor (creates a default rectangular room)
    public Room() {
        this(5.0f, 5.0f, 3.0f); // Default to a 5x5x3 rectangular room
    }

    // Constructor for Rectangular Room
    public Room(float width, float length, float height) {
        setShape(RoomShape.RECTANGULAR);
        setDimensionsRectangular(width, length, height);
        setDefaultAppearance();
    }

    // --- Setup Methods ---

    private void setDefaultAppearance() {
        this.wallColor = new Color(230, 230, 210); // Light Beige
        this.floorColor = new Color(190, 160, 130); // Wood Brown
        this.wallTexturePath = null;
        this.floorTexturePath = null;
    }

    // Central method to set shape and ensure only relevant dimensions are kept (optional)
    public void setShape(RoomShape newShape) {
        this.shape = newShape;
    }


    // --- Dimension Setting Methods ---

    // Set dimensions for a RECTANGULAR room (also sets shape)
    public void setDimensionsRectangular(float width, float length, float height) {
        if (width <= 0 || length <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room dimensions must be positive.");
        }
        setShape(RoomShape.RECTANGULAR);
        this.width = width;
        this.length = length;
        this.height = height;
        // Clear/default other shape dimensions if necessary
        this.radius = 0;
    }

    // Set dimensions for a CIRCULAR room (also sets shape)
    public void setDimensionsCircular(float radius, float height) {
        if (radius <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room dimensions must be positive.");
        }
        setShape(RoomShape.CIRCULAR);
        this.radius = radius;
        this.height = height;
        // Clear/default other shape dimensions
        this.width = 0;
        this.length = 0;
    }

    // Set dimensions for an L-SHAPED room (also sets shape)
    public void setDimensionsLShape(float outerWidth, float outerLength, float insetWidth, float insetLength, float height) {
        if (outerWidth <= 0 || outerLength <= 0 || insetWidth <= 0 || insetLength <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room dimensions must be positive.");
        }
        if (insetWidth >= outerWidth || insetLength >= outerLength) {
            throw new IllegalArgumentException("L-Shape inset dimensions must be smaller than outer dimensions.");
        }
        setShape(RoomShape.L_SHAPED);
        this.l_outerWidth = outerWidth;
        this.l_outerLength = outerLength;
        this.l_insetWidth = insetWidth;
        this.l_insetLength = insetLength;
        this.height = height;
        // Clear/default others
        this.width = 0; this.length = 0; this.radius = 0;
    }

    // Set dimensions for a T-SHAPED room (also sets shape)
    public void setDimensionsTShape(float barWidth, float barLength, float stemWidth, float stemLength, float height) {
        if (barWidth <= 0 || barLength <= 0 || stemWidth <= 0 || stemLength <= 0 || height <= 0) {
            throw new IllegalArgumentException("Room dimensions must be positive.");
        }
        if (stemWidth > barWidth) {
            throw new IllegalArgumentException("T-Shape stem width cannot be greater than bar width.");
        }
        setShape(RoomShape.T_SHAPED);
        this.t_barWidth = barWidth;
        this.t_barLength = barLength;
        this.t_stemWidth = stemWidth;
        this.t_stemLength = stemLength;
        this.height = height;
        // Clear/default others
        this.width = 0; this.length = 0; this.radius = 0;
    }


    // --- Getters ---

    public RoomShape getShape() { return shape; }
    public float getHeight() { return height; }
    public Color getWallColor() { return wallColor; }
    public Color getFloorColor() { return floorColor; }
    public String getWallTexturePath() { return wallTexturePath; }
    public String getFloorTexturePath() { return floorTexturePath; }

    // Rectangular dimensions (return 0 or default if not rectangular)
    public float getWidth() { return (shape == RoomShape.RECTANGULAR) ? width : 0; }
    public float getLength() { return (shape == RoomShape.RECTANGULAR) ? length : 0; }

    // Circular dimensions
    public float getRadius() { return (shape == RoomShape.CIRCULAR) ? radius : 0; }

    // L-Shaped dimensions
    public float getL_outerWidth() { return (shape == RoomShape.L_SHAPED) ? l_outerWidth : 0; }
    public float getL_outerLength() { return (shape == RoomShape.L_SHAPED) ? l_outerLength : 0; }
    public float getL_insetWidth() { return (shape == RoomShape.L_SHAPED) ? l_insetWidth : 0; }
    public float getL_insetLength() { return (shape == RoomShape.L_SHAPED) ? l_insetLength : 0; }

    // T-Shaped dimensions
    public float getT_barWidth() { return (shape == RoomShape.T_SHAPED) ? t_barWidth : 0; }
    public float getT_barLength() { return (shape == RoomShape.T_SHAPED) ? t_barLength : 0; }
    public float getT_stemWidth() { return (shape == RoomShape.T_SHAPED) ? t_stemWidth : 0; }
    public float getT_stemLength() { return (shape == RoomShape.T_SHAPED) ? t_stemLength : 0; }


    // --- Setters ---

    // Setters for common properties
    public void setHeight(float height) {
        if (height <= 0) throw new IllegalArgumentException("Height must be positive.");
        this.height = height;
    }
    public void setWallColor(Color wallColor) { this.wallColor = wallColor; }
    public void setFloorColor(Color floorColor) { this.floorColor = floorColor; }
    public void setWallTexturePath(String wallTexturePath) { this.wallTexturePath = wallTexturePath; }
    public void setFloorTexturePath(String floorTexturePath) { this.floorTexturePath = floorTexturePath; }

    public void setWidth(float width) {
        if (shape == RoomShape.RECTANGULAR) {
            if (width <= 0) throw new IllegalArgumentException("Width must be positive."); this.width = width;
        } // else: ignore or throw error? Silently ignoring is simpler for now.
    }
    public void setLength(float length) {
        if (shape == RoomShape.RECTANGULAR) {
            if (length <= 0) throw new IllegalArgumentException("Length must be positive."); this.length = length;
        }
    }
    public void setRadius(float radius) {
        if (shape == RoomShape.CIRCULAR) {
            if (radius <= 0) throw new IllegalArgumentException("Radius must be positive."); this.radius = radius;
        }
    }

    // Add setters for L-shape and T-shape individual dimensions if needed, following the pattern above.
    public void setL_outerWidth(float w) { if(shape == RoomShape.L_SHAPED && w > 0 && w > l_insetWidth) this.l_outerWidth = w; }
    public void setL_outerLength(float l) { if(shape == RoomShape.L_SHAPED && l > 0 && l > l_insetLength) this.l_outerLength = l; }
    public void setL_insetWidth(float w) { if(shape == RoomShape.L_SHAPED && w > 0 && w < l_outerWidth) this.l_insetWidth = w; }
    public void setL_insetLength(float l) { if(shape == RoomShape.L_SHAPED && l > 0 && l < l_outerLength) this.l_insetLength = l; }
    public void setT_barWidth(float w) { if(shape == RoomShape.T_SHAPED && w > 0 && w >= t_stemWidth) this.t_barWidth = w; }
    public void setT_barLength(float l) { if(shape == RoomShape.T_SHAPED && l > 0) this.t_barLength = l; }
    public void setT_stemWidth(float w) { if(shape == RoomShape.T_SHAPED && w > 0 && w <= t_barWidth) this.t_stemWidth = w; }
    public void setT_stemLength(float l) { if(shape == RoomShape.T_SHAPED && l > 0) this.t_stemLength = l; }


    // --- Utility Methods ---

    /** Calculates the center of the room's floor plan. */
    public Vector3f calculateCenter() {
        switch (shape) {
            case RECTANGULAR:
                return new Vector3f(width / 2.0f, 0, length / 2.0f);
            case CIRCULAR:
                // Assuming the circle is centered at the world origin for rendering
                return new Vector3f(0, 0, 0);
            case L_SHAPED:
                // Approximate center (geometric center can be complex)
                // Using the center of the bounding box as a reasonable approximation
                return new Vector3f(l_outerWidth / 2.0f, 0, l_outerLength / 2.0f);
            case T_SHAPED:
                // Approximate center: center of the bar width, weighted average of length
                float totalLength = t_barLength + t_stemLength;
                // Weighted average Z based on area (approx)
                float barArea = t_barWidth * t_barLength;
                float stemArea = t_stemWidth * t_stemLength;
                float barCenterZ = t_barLength / 2.0f;
                float stemCenterZ = t_barLength + (t_stemLength / 2.0f);
                float centerZ = (barArea * barCenterZ + stemArea * stemCenterZ) / (barArea + stemArea);
                return new Vector3f(t_barWidth / 2.0f, 0, centerZ);
            default:
                return new Vector3f(0, 0, 0); // Fallback
        }
    }
}
