import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.awt.Color;

// Assume Vector3f class is available (defined in CameraManager or separately)

public class DrawingUtils {

    /** Sets the current OpenGL color and material properties. */
    public static void setColor(GL2 gl, Color c) {
        if (c == null) c = Color.GRAY; // Default color if null
        float[] colorComponents = c.getRGBComponents(null);

        // Set current draw color (used if lighting disabled or ColorMaterial enabled)
        gl.glColor4f(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);

        // Set material properties for lighting
        float[] materialColor = {colorComponents[0], colorComponents[1], colorComponents[2], 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, materialColor, 0);
        // Consider setting default specular/shininess here if desired
        // float[] matSpecular = { 0.2f, 0.2f, 0.2f, 1.0f };
        // float[] matShininess = { 10.0f };
        // gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpecular, 0);
        // gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShininess, 0);
    }

    /** Draws a solid box centered at the current origin. */
    public static void drawBox(GL2 gl, float sizeX, float sizeY, float sizeZ, boolean hasTexture) {
        float halfX = sizeX / 2.0f;
        float halfY = sizeY / 2.0f;
        float halfZ = sizeZ / 2.0f;

        gl.glBegin(GL2.GL_QUADS);

        // Bottom face (-Y)
        gl.glNormal3f(0, -1, 0);
        if(hasTexture) gl.glTexCoord2f(0,0); gl.glVertex3f(-halfX, -halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(1,0); gl.glVertex3f( halfX, -halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(1,1); gl.glVertex3f( halfX, -halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(0,1); gl.glVertex3f(-halfX, -halfY,  halfZ);
        if(!hasTexture) { gl.glVertex3f(-halfX, -halfY, -halfZ); gl.glVertex3f( halfX, -halfY, -halfZ); gl.glVertex3f( halfX, -halfY,  halfZ); gl.glVertex3f(-halfX, -halfY,  halfZ); }

        // Top face (+Y)
        gl.glNormal3f(0, 1, 0);
        if(hasTexture) gl.glTexCoord2f(0,1); gl.glVertex3f(-halfX,  halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(1,1); gl.glVertex3f( halfX,  halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(1,0); gl.glVertex3f( halfX,  halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(0,0); gl.glVertex3f(-halfX,  halfY, -halfZ);
        if(!hasTexture) { gl.glVertex3f(-halfX,  halfY,  halfZ); gl.glVertex3f( halfX,  halfY,  halfZ); gl.glVertex3f( halfX,  halfY, -halfZ); gl.glVertex3f(-halfX,  halfY, -halfZ); }

        // Front face (+Z)
        gl.glNormal3f(0, 0, 1);
        if(hasTexture) gl.glTexCoord2f(0,0); gl.glVertex3f(-halfX, -halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(1,0); gl.glVertex3f( halfX, -halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(1,1); gl.glVertex3f( halfX,  halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(0,1); gl.glVertex3f(-halfX,  halfY,  halfZ);
        if(!hasTexture) { gl.glVertex3f(-halfX, -halfY,  halfZ); gl.glVertex3f( halfX, -halfY,  halfZ); gl.glVertex3f( halfX,  halfY,  halfZ); gl.glVertex3f(-halfX,  halfY,  halfZ); }

        // Back face (-Z)
        gl.glNormal3f(0, 0, -1);
        if(hasTexture) gl.glTexCoord2f(1,0); gl.glVertex3f(-halfX, -halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(1,1); gl.glVertex3f(-halfX,  halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(0,1); gl.glVertex3f( halfX,  halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(0,0); gl.glVertex3f( halfX, -halfY, -halfZ);
        if(!hasTexture) { gl.glVertex3f(-halfX, -halfY, -halfZ); gl.glVertex3f(-halfX,  halfY, -halfZ); gl.glVertex3f( halfX,  halfY, -halfZ); gl.glVertex3f( halfX, -halfY, -halfZ); }

        // Left face (-X)
        gl.glNormal3f(-1, 0, 0);
        if(hasTexture) gl.glTexCoord2f(1,0); gl.glVertex3f(-halfX, -halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(1,1); gl.glVertex3f(-halfX,  halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(0,1); gl.glVertex3f(-halfX,  halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(0,0); gl.glVertex3f(-halfX, -halfY,  halfZ);
        if(!hasTexture) { gl.glVertex3f(-halfX, -halfY, -halfZ); gl.glVertex3f(-halfX,  halfY, -halfZ); gl.glVertex3f(-halfX,  halfY,  halfZ); gl.glVertex3f(-halfX, -halfY,  halfZ); }

        // Right face (+X)
        gl.glNormal3f(1, 0, 0);
        if(hasTexture) gl.glTexCoord2f(0,0); gl.glVertex3f( halfX, -halfY, -halfZ);
        if(hasTexture) gl.glTexCoord2f(1,0); gl.glVertex3f( halfX, -halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(1,1); gl.glVertex3f( halfX,  halfY,  halfZ);
        if(hasTexture) gl.glTexCoord2f(0,1); gl.glVertex3f( halfX,  halfY, -halfZ);
        if(!hasTexture) { gl.glVertex3f( halfX, -halfY, -halfZ); gl.glVertex3f( halfX, -halfY,  halfZ); gl.glVertex3f( halfX,  halfY,  halfZ); gl.glVertex3f( halfX,  halfY, -halfZ); }

        gl.glEnd();
    }

    /** Draws a wireframe box centered at the current origin. */
    public static void drawWireBox(GL2 gl, float sizeX, float sizeY, float sizeZ) {
        float halfX = sizeX / 2.0f; float halfY = sizeY / 2.0f; float halfZ = sizeZ / 2.0f;
        Vector3f[] v = {
                new Vector3f(-halfX, -halfY, -halfZ), new Vector3f( halfX, -halfY, -halfZ),
                new Vector3f( halfX,  halfY, -halfZ), new Vector3f(-halfX,  halfY, -halfZ),
                new Vector3f(-halfX, -halfY,  halfZ), new Vector3f( halfX, -halfY,  halfZ),
                new Vector3f( halfX,  halfY,  halfZ), new Vector3f(-halfX,  halfY,  halfZ)
        };
        int[][] edges = {
                {0,1}, {1,2}, {2,3}, {3,0}, {4,5}, {5,6}, {6,7}, {7,4},
                {0,4}, {1,5}, {2,6}, {3,7}
        };

        gl.glBegin(GL.GL_LINES);
        for (int[] edge : edges) {
            gl.glVertex3f(v[edge[0]].x, v[edge[0]].y, v[edge[0]].z);
            gl.glVertex3f(v[edge[1]].x, v[edge[1]].y, v[edge[1]].z);
        }
        gl.glEnd();
    }

    /** Draws a grid on the XZ plane. */
    public static void drawGrid(GL2 gl, int lines, float spacing) {
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT | GL2.GL_ENABLE_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        setColor(gl, new Color(200, 200, 200)); // Use the utility setColor
        gl.glLineWidth(1.0f);

        gl.glBegin(GL2.GL_LINES);
        float extent = lines * spacing / 2.0f;
        for (int i = -lines / 2; i <= lines / 2; i++) {
            // Lines parallel to Z axis
            gl.glVertex3f(i * spacing, 0.01f, -extent); // Y slightly above 0
            gl.glVertex3f(i * spacing, 0.01f,  extent);
            // Lines parallel to X axis
            gl.glVertex3f(-extent, 0.01f, i * spacing);
            gl.glVertex3f( extent, 0.01f, i * spacing);
        }
        gl.glEnd();
        gl.glPopAttrib(); // Restore attributes
    }
}