import java.awt.Color;
import java.io.Serializable;

abstract class DesignItem implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Vector3f position;
    protected Vector3f rotation;
    protected Vector3f scale;
    protected Color color;
    protected String texturePath;

    public DesignItem(Vector3f position) {
        this.position = position;
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
        this.color = Color.GRAY;
    }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }
    public Vector3f getRotation() { return rotation; }
    public void setRotation(Vector3f rotation) { this.rotation = rotation; }
    public Vector3f getScale() { return scale; }
    public void setScale(Vector3f scale) { this.scale = scale; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }
}