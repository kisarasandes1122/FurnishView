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

    private Map<String, Texture> textureCache = new HashMap<>();

    /** Gets a texture, loading and caching it if necessary. */
    public Texture getTexture(GL2 gl, String texturePath) {
        if (texturePath == null || texturePath.trim().isEmpty()) {
            return null;
        }
        if (textureCache.containsKey(texturePath)) {
            return textureCache.get(texturePath); // Can return null if previously failed
        }

        Texture tex = null;
        try {
            File texFile = new File(texturePath);
            if (!texFile.exists() || !texFile.isFile() || !texFile.canRead()) {
                System.err.println("Texture file not found or not readable: " + texturePath);
                textureCache.put(texturePath, null); // Cache failure
                return null;
            }

            tex = TextureIO.newTexture(texFile, true); // Generate mipmaps

            // Default texture parameters
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);

            // Anisotropic Filtering
            float[] maxAniso = new float[1];
            gl.glGetFloatv(GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso, 0);
            if (maxAniso[0] > 1.0f) {
                tex.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(maxAniso[0], 4.0f));
            }

            textureCache.put(texturePath, tex);
            return tex;

        } catch (IOException | GLException e) {
            System.err.println("Error loading texture '" + texturePath + "': " + e.getMessage());
            // Don't print stack trace in production? Or use a logger.
            if (tex != null) {
                try { tex.destroy(gl); } catch (GLException ignore) {}
            }
            textureCache.put(texturePath, null); // Cache the failure
            return null;
        }
    }

    /** Clears the texture cache (useful when loading a new model). */
    public void clearCache() {
        // Note: This doesn't destroy the textures in OpenGL context yet.
        textureCache.clear();
    }

    public boolean isTextureLoaded(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        // Check if entry exists AND if the value is not null (i.e., didn't fail to load)
        return textureCache.containsKey(path) && textureCache.get(path) != null;
    }

    // Add helper to get texture without trying to load again if check needed elsewhere
    public Texture getCachedTexture(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        return textureCache.getOrDefault(path, null); // Return null if not found or if load failed
    }

    /** Destroys all cached textures in the OpenGL context. Call during dispose. */
    public void disposeAll(GL2 gl) {
        for (Texture tex : textureCache.values()) {
            if (tex != null) {
                try {
                    tex.destroy(gl);
                } catch (GLException e) {
                    System.err.println("Error destroying texture: " + e.getMessage());
                }
            }
        }
        textureCache.clear();
    }
}