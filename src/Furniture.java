import java.awt.Color;

class Furniture extends DesignItem {
    private static final long serialVersionUID = 1L;
    private String type;
    private float width, depth, height;

    public Furniture(String type, Vector3f position, float width, float depth, float height) {
        super(position);
        this.type = type;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.color = getDefaultColor(type);
    }

    public String getType() { return type; }
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    public float getDepth() { return depth; }
    public void setDepth(float depth) { this.depth = depth; }
    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    private Color getDefaultColor(String type) {
        switch (type.toLowerCase()) {
            // Original
            case "chair":           return new Color(160, 82, 45); // Saddle Brown
            case "sofa":            return new Color(100, 100, 180); // Muted Blue/Purple
            case "dining table":
            case "table":           return new Color(139, 69, 19); // Dark Wood Brown
            case "side table":
            case "end table":       return new Color(210, 180, 140); // Tan / Light Wood
            case "bed":             // Default bed color if type isn't specific
            case "queen bed":       return new Color(222, 184, 135); // Burlywood
            case "bookshelf":       return new Color(188, 143, 143); // Rosy Brown

            // Seating
            case "armchair":        return new Color(184, 134, 11);  // Dark Goldenrod
            case "dining chair":    return new Color(205, 133, 63);  // Peru
            case "office chair":    return Color.DARK_GRAY;
            case "stool":           return new Color(112, 128, 144); // Slate Gray
            case "bench":           return new Color(193, 154, 107); // Khaki-ish
            case "recliner":        return new Color(47, 79, 79);    // Dark Slate Gray
            case "ottoman":         return new Color(176, 196, 222); // Light Steel Blue

            // Tables
            case "coffee table":    return new Color(92, 64, 51);    // Dark Brown Wood
            case "desk":            return new Color(160, 120, 80);  // Lighter Wood Desk
            case "console table":   return new Color(101, 67, 33);   // Dark Oak

            // Storage
            case "wardrobe":        return new Color(119, 136, 153); // Light Slate Gray Wood
            case "dresser":
            case "chest of drawers": return new Color(178, 138, 103); // Medium Wood Tone
            case "filing cabinet":  return Color.LIGHT_GRAY;
            case "tv stand":        return new Color(60, 60, 60);    // Dark Grey/Black Stand

            // Beds (Specific types)
            case "twin bed":        return new Color(245, 222, 179); // Wheat
            case "king bed":        return new Color(218, 165, 32);  // Goldenrod (larger bed frame?)
            case "bunk bed":        return new Color(155, 118, 83);  // Pine Wood Color
            case "murphy bed":      return new Color(200, 200, 200); // Wall Color-ish

            // Miscellaneous
            case "headboard":       return new Color(123, 104, 238); // Medium Slate Blue (Fabric?)
            case "crib":            return Color.WHITE;
            case "chaise lounge":   return new Color(216, 191, 216); // Thistle (Fabric)
            case "futon":           return new Color(70, 130, 180);  // Steel Blue

            default:                return Color.GRAY; // Fallback
        }
    }
}