import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    // Cache to store loaded textures, mapping file path to Texture object
    private Map<String, Texture> textureCache = new HashMap<>();

    public Texture getTexture(GL2 gl, String texturePath) {
        // Handle null or empty paths
        if (texturePath == null || texturePath.trim().isEmpty()) {
            return null;
        }

        // Check cache first
        if (textureCache.containsKey(texturePath)) {
            // Return cached texture (could be null if previous load failed)
            return textureCache.get(texturePath);
        }

        // Texture not in cache, attempt to load
        Texture tex = null;
        try {
            File texFile = new File(texturePath);
            // Validate file existence and readability
            if (!texFile.exists() || !texFile.isFile() || !texFile.canRead()) {
                System.err.println("Texture file not found or not readable: " + texturePath);
                textureCache.put(texturePath, null); // Cache the failure
                return null;
            }

            // Load the texture using JOGL's utility
            // The 'true' argument enables mipmap generation
            tex = TextureIO.newTexture(texFile, true);

            // Set common texture parameters (can be overridden later if needed)
            // Use linear filtering with mipmaps for minification (good quality)
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
            // Use linear filtering for magnification (smooth)
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            // Set texture wrapping mode to repeat (common for floors/walls)
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT); // Horizontal wrap
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT); // Vertical wrap

            // Optional: Enable Anisotropic Filtering if supported (improves texture quality at sharp angles)
            // float[] maxAniso = new float[1];
            // gl.glGetFloatv(GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso, 0);
            // if (maxAniso[0] > 1.0f) {
            //    // Set anisotropy level (e.g., 4.0f is a common value)
            //    tex.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(maxAniso[0], 4.0f));
            //}

            // Store the successfully loaded texture in the cache
            textureCache.put(texturePath, tex);
            System.out.println("Texture loaded successfully: " + texturePath);
            return tex;

        } catch (IOException | GLException e) {
            // Handle errors during texture loading
            System.err.println("Error loading texture '" + texturePath + "': " + e.getMessage());
            // e.printStackTrace(); // Optionally print stack trace for debugging

            // Clean up partially created texture if an error occurred
            if (tex != null) {
                try { tex.destroy(gl); } catch (GLException ignore) {}
            }
            // Cache the failure so we don't try to load it again repeatedly
            textureCache.put(texturePath, null);
            return null;
        }
    }


    public boolean isTextureCached(String texturePath) {
        return texturePath != null && textureCache.containsKey(texturePath);
    }


    public boolean isTextureLoaded(String texturePath) {
        return texturePath != null && textureCache.containsKey(texturePath) && textureCache.get(texturePath) != null;
    }


    public Texture getCachedTexture(String texturePath) {
        return textureCache.get(texturePath);
    }


    /** Clears the texture cache map. This does not destroy the textures in OpenGL. */
    public void clearCache() {
        System.out.println("Clearing texture cache.");
        textureCache.clear();
    }


    public void disposeAll(GL2 gl) {
        System.out.println("Disposing all cached textures...");
        for (Map.Entry<String, Texture> entry : textureCache.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    entry.getValue().destroy(gl);
                    System.out.println("  Destroyed texture: " + entry.getKey());
                } catch (GLException e) {
                    System.err.println("Error destroying texture '" + entry.getKey() + "': " + e.getMessage());
                }
            }
        }
        textureCache.clear(); // Clear the map after destroying
    }
}