import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.Color;


public class FurnitureRenderer {

    private TextureManager textureManager;

    public FurnitureRenderer(TextureManager textureManager) {
        if (textureManager == null) {
            throw new IllegalArgumentException("TextureManager cannot be null");
        }
        this.textureManager = textureManager;
    }

    public void drawFurniture(GL2 gl, Furniture furniture) {
        if (furniture == null) {
            return; // Don't draw if furniture is null
        }

        gl.glPushMatrix(); // Save the current modelview matrix state

        // Apply furniture transformations
        Vector3f pos = furniture.getPosition();
        Vector3f rot = furniture.getRotation();
        // Note: Order of operations matters (Translate then Rotate is common)
        gl.glTranslatef(pos.x, pos.y, pos.z); // Move to furniture's position (base center)
        // Apply Y-axis rotation first (most common for furniture placement)
        gl.glRotatef(rot.y, 0, 1, 0);
        // Apply X and Z rotations if needed (e.g., tilted items)
        gl.glRotatef(rot.x, 1, 0, 0);
        gl.glRotatef(rot.z, 0, 0, 1);

        // Save GL state related to appearance
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT);

        // Handle Texture
        Texture furnitureTex = textureManager.getTexture(gl, furniture.getTexturePath());
        boolean hasTexture = (furnitureTex != null);

        if (hasTexture) {
            gl.glEnable(GL.GL_TEXTURE_2D);      // Enable texturing
            furnitureTex.enable(gl);            // Enable this specific texture object
            furnitureTex.bind(gl);              // Bind the texture to be active
            // When texturing, set vertex color to white to avoid tinting the texture
            DrawingUtils.setColor(gl, Color.WHITE);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);     // Disable texturing
            // Set color using the utility function which handles material properties
            DrawingUtils.setColor(gl, furniture.getColor());
        }

        // Set drawing mode (solid fill)
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        // --- Actual Drawing ---
        // Placeholder: Draw a simple box representing the furniture's dimensions.
        // The origin (0,0,0) in furniture's local space is at the center of its base.
        // We need to translate *up* by half the height before drawing the box.
        gl.glPushMatrix(); // Isolate the translation for the box drawing
        gl.glTranslatef(0, furniture.getHeight() / 2.0f, 0); // Move origin to center of the box volume
        DrawingUtils.drawBox(gl, furniture.getWidth(), furniture.getHeight(), furniture.getDepth(), hasTexture);
        gl.glPopMatrix(); // Restore origin to the base center

        // Clean up texture state if texture was used
        if (hasTexture) {
            furnitureTex.disable(gl); // Disable this specific texture object
            // glDisable(GL.GL_TEXTURE_2D) will be handled by glPopAttrib if it was enabled
        }

        // Restore GL state
        gl.glPopAttrib();

        gl.glPopMatrix(); // Restore the modelview matrix to its state before drawing this furniture
    }

}