// START OF FILE DesignRenderer.java

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel; // If used
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;

import java.awt.Color;
import java.awt.Font;

// Import constants for drawing primitives
import static com.jogamp.opengl.GL.GL_TRIANGLE_FAN;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;

// Assume DesignModel, Furniture, Room, Vector3f, RoomShape classes are available
// Assume DrawingUtils, CameraManager, TextureManager, FurnitureRenderer, PickingHelper are available

public class DesignRenderer implements GLEventListener {

    private GLU glu;
    private DesignModel designModel;
    private TextRenderer textRenderer;

    // Managers and Helpers
    private CameraManager cameraManager;
    private TextureManager textureManager;
    private FurnitureRenderer furnitureRenderer;
    private PickingHelper pickingHelper;

    // State specific to this renderer
    private boolean showGrid = true;
    private int viewWidth = 1;
    private int viewHeight = 1;

    // Cached matrices/viewport for picking
    private final double[] lastModelview = new double[16];
    private final double[] lastProjection = new double[16];
    private final int[] lastViewport = new int[4];

    public DesignRenderer(DesignModel model) {
        if (model == null) throw new IllegalArgumentException("DesignModel cannot be null");
        this.designModel = model;
        this.glu = new GLU();

        // Initialize managers
        this.cameraManager = new CameraManager();
        this.textureManager = new TextureManager();
        this.furnitureRenderer = new FurnitureRenderer(this.textureManager);
        this.pickingHelper = new PickingHelper();

        // Initialize camera based on model AFTER managers are ready
        updateCameraForModel(); // Initial setup based on model's default room
        // Set initial mode based on camera manager's default
        set3DMode(cameraManager.is3DMode());

        // Initialize matrix cache
        Matrix4f identity = new Matrix4f();
        int len = Math.min(identity.m.length, 16);
        for (int i = 0; i < len; i++) {
            lastModelview[i] = (double) identity.m[i];
            lastProjection[i] = (double) identity.m[i];
        }
        if (len < 16) { // Ensure diagonal is 1 if array was shorter (shouldn't happen)
            lastModelview[0] = lastModelview[5] = lastModelview[10] = lastModelview[15] = 1.0;
            lastProjection[0] = lastProjection[5] = lastProjection[10] = lastProjection[15] = 1.0;
        }
    }

    public void setDesignModel(DesignModel newModel) {
        if (newModel == null) throw new IllegalArgumentException("Cannot set DesignModel to null");
        this.designModel = newModel;
        textureManager.clearCache(); // Clear textures for the new model
        updateCameraForModel();      // Reset camera view based on new model's room
        set3DMode(cameraManager.is3DMode()); // Re-apply mode settings if needed
    }

    // --- UPDATED: updateCameraForModel to use Room object ---
    public void updateCameraForModel() {
        if (cameraManager == null) return; // Should not happen after constructor

        if (designModel != null && designModel.getRoom() != null) {
            Room room = designModel.getRoom();
            // Pass the actual Room object to reset target and potentially internal dims
            cameraManager.resetTargetToCenter(room);
            // Let CameraManager handle dimensions if it still needs them
            // cameraManager.setRoomDimensions(...); // Maybe remove if only used for center
        } else {
            // Fallback if room is null during initialization or load error
            cameraManager.resetTargetToCenter(null);
        }
        // Re-apply mode to adjust distance etc. after setting dimensions/target
        cameraManager.setMode(cameraManager.is3DMode());
    }

    // --- Camera Interaction Delegation ---
    public void rotateCamera(float deltaX, float deltaY) {
        if (cameraManager != null) cameraManager.rotate(deltaX, deltaY);
    }

    public void zoomCamera(float delta) {
        if (cameraManager != null) {
            cameraManager.zoom(delta);
            if (!cameraManager.is3DMode()) {
                System.err.println("Warning: Ortho zoom may require reshape trigger if aspect changes.");
            }
        }
    }

    public void panCamera(float deltaX, float deltaY) {
        if (cameraManager != null) cameraManager.pan(deltaX, deltaY, viewWidth, viewHeight);
    }

    public void set3DMode(boolean is3D) {
        if (cameraManager != null) {
            cameraManager.setMode(is3D);
            // Mode change *might* require reshape if projection parameters change drastically
            System.err.println("Warning: Mode change may require reshape trigger if aspect changes.");
        }
    }

    public boolean is3DMode() {
        return (cameraManager != null) && cameraManager.is3DMode();
    }

    // --- Picking Delegation ---
    public Vector3f screenToWorldOnPlane(int screenX, int screenY, float planeY) {
        if (pickingHelper == null || cameraManager == null) return null;
        return pickingHelper.screenToWorldOnPlane(screenX, screenY, planeY,
                lastModelview, lastProjection, lastViewport,
                cameraManager.is3DMode());
    }

    public Vector3f screenToWorldFloor(int screenX, int screenY) {
        return screenToWorldOnPlane(screenX, screenY, 0.0f);
    }

    public Furniture pickFurniture(int screenX, int screenY) {
        if (pickingHelper == null || cameraManager == null || designModel == null) return null;
        // TODO: Picking logic might need adjustment for non-rectangular rooms if origin changes
        return pickingHelper.pickFurniture(screenX, screenY, designModel,
                lastModelview, lastProjection, lastViewport,
                cameraManager.is3DMode());
    }

    // --- Other Controls ---
    public void setShowGrid(boolean show) { this.showGrid = show; }
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    // --- GLEventListener Methods ---

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU(); // Re-init GLU just in case

        gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glEnable(GL2.GL_CULL_FACE); // Enable Culling
        gl.glCullFace(GL2.GL_BACK);    // Cull back faces
        gl.glFrontFace(GL2.GL_CCW);    // Counter-clockwise vertices are front faces (OpenGL default)
        gl.glShadeModel(GL2.GL_SMOOTH);

        // Lighting setup
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        // Keep light slightly away from origin, high up
        float[] lightPos = {10.0f, 15.0f, 10.0f, 1.0f}; // Positional light
        float[] lightAmbient = {0.3f, 0.3f, 0.3f, 1.0f};
        float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
        float[] lightSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);

        // Material setup (can be overridden by setColor)
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        float[] matSpecular = { 0.2f, 0.2f, 0.2f, 1.0f };
        float[] matShininess = { 10.0f };
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpecular, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShininess, 0);

        // Anti-aliasing hints
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        // gl.glEnable(GL2.GL_POLYGON_SMOOTH); // Can cause artifacts with depth testing/alpha
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        // gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);

        // Texture default parameters (can be overridden by TextureManager)
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

        textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 12));

        // Initial camera state already set in constructor via updateCameraForModel
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (textRenderer != null) {
            textRenderer.dispose();
            textRenderer = null;
        }
        if (textureManager != null) {
            textureManager.disposeAll(gl); // Dispose textures via manager
        }
        glu = null;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (designModel == null || cameraManager == null || furnitureRenderer == null || pickingHelper == null) {
            return; // Don't draw if core components are missing
        }
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // --- Camera Setup ---
        cameraManager.applyLookAt(gl, glu); // Sets ModelView matrix

        // --- Drawing Scene Elements ---
        if (showGrid) {
            // TODO: Adjust grid size/origin based on room shape/center?
            DrawingUtils.drawGrid(gl, 20, 1.0f);
        }

        // --- MODIFIED: Call the main drawRoom method ---
        if (designModel.getRoom() != null) {
            // Note: Room origin might change depending on shape (e.g., circle center at 0,0)
            // Consider adding a gl.glPushMatrix/Translate/PopMatrix around room drawing
            // if the room's local origin isn't the world origin (0,0,0).
            // For now, assume all shapes are drawn relative to world origin.
            drawRoom(gl, designModel.getRoom()); // This now switches internally
        }

        if (designModel.getFurnitureList() != null) {
            for (Furniture item : designModel.getFurnitureList()) {
                furnitureRenderer.drawFurniture(gl, item); // Delegate to FurnitureRenderer
            }
        }

        Furniture selected = designModel.getSelectedFurniture();
        if (selected != null) {
            drawSelectionIndicator(gl, selected); // Keep selection drawing here
        }

        // --- Capture Matrices for Picking ---
        captureMatricesForPicking(gl);

        // Optional: Draw UI elements using textRenderer here
        // drawHUD(gl);

        gl.glFlush();
    }

    private void captureMatricesForPicking(GL2 gl) {
        try {
            gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, lastModelview, 0);
            gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, lastProjection, 0);
            gl.glGetIntegerv(GL2.GL_VIEWPORT, lastViewport, 0);
        } catch (GLException e) {
            System.err.println("Error capturing matrices/viewport: " + e.getMessage());
        }
    }

    // Optional HUD drawing method
    // private void drawHUD(GL2 gl) {
    //     textRenderer.beginRendering(viewWidth, viewHeight);
    //     textRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f); // Black text
    //     textRenderer.draw("Mode: " + (is3DMode() ? "3D" : "2D"), 10, viewHeight - 20);
    //     // Add more info if needed
    //     textRenderer.endRendering();
    // }


    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) height = 1;
        float aspect = (float) width / (float) height;

        this.viewWidth = width;
        this.viewHeight = height;

        gl.glViewport(0, 0, width, height);

        // --- Projection Setup ---
        if (cameraManager != null) {
            cameraManager.applyProjection(gl, glu, aspect); // Delegate projection setup
        } else {
            // Fallback projection if camera manager isn't ready
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            glu.gluPerspective(45.0f, aspect, 0.1f, 100.0f);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        }


        // --- Capture Projection Matrix & Viewport After Setting ---
        captureMatricesForPicking(gl); // Capture again after projection change

        // Ensure ModelView is reset after changing projection
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity(); // Start fresh for the next display's lookAt
    }

    // --- Room Drawing Logic ---

    // --- MODIFIED: Main drawRoom switches based on shape ---
    private void drawRoom(GL2 gl, Room room) {
        // Save state - Importantly saves GL_ENABLE_BIT which includes GL_CULL_FACE state
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Common setup: Disable culling for floor drawing to ensure top face is visible
        boolean cullingWasEnabled = gl.glIsEnabled(GL2.GL_CULL_FACE);
        if (cullingWasEnabled) {
            gl.glDisable(GL2.GL_CULL_FACE);
        }

        // --- Draw Floor based on Shape ---
        switch (room.getShape()) {
            case RECTANGULAR: drawRectangularFloor(gl, room); break;
            case CIRCULAR:    drawCircularFloor(gl, room); break;
            case L_SHAPED:    drawLShapeFloor(gl, room); break;
            case T_SHAPED:    drawTShapeFloor(gl, room); break;
        }

        // --- Restore Culling BEFORE drawing walls ---
        // Crucial so that wall backfaces are culled correctly
        if (cullingWasEnabled) {
            gl.glEnable(GL2.GL_CULL_FACE);
        }

        // --- Draw Walls or Outline based on Shape ---
        if (cameraManager.is3DMode()) {
            switch (room.getShape()) {
                case RECTANGULAR: drawRectangularWalls(gl, room); break;
                case CIRCULAR:    drawCircularWall(gl, room); break;
                case L_SHAPED:    drawLShapeWalls(gl, room); break;
                case T_SHAPED:    drawTShapeWalls(gl, room); break;
            }
        } else { // 2D Mode Outline
            switch (room.getShape()) {
                case RECTANGULAR: drawRectangularOutline(gl, room); break;
                case CIRCULAR:    drawCircularOutline(gl, room); break;
                case L_SHAPED:    drawLShapeOutline(gl, room); break;
                case T_SHAPED:    drawTShapeOutline(gl, room); break;
            }
        }

        // Restore all attributes saved by glPushAttrib, including original cull state
        gl.glPopAttrib();    }

    // --- Helper methods for drawing each shape ---

    private void setupFloorMaterial(GL2 gl, Room room) {
        Texture floorTex = textureManager.getTexture(gl, room.getFloorTexturePath());
        if (floorTex != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            floorTex.enable(gl);
            floorTex.bind(gl);
            DrawingUtils.setColor(gl, Color.WHITE); // Use white base color for textures
            // Tex params (like repeat) should be set by TextureManager or here
            // floorTex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            // floorTex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            return; // Return true indicating texture is bound
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D); // Ensure texturing is off if no texture
            DrawingUtils.setColor(gl, room.getFloorColor());
            return; // Return false indicating solid color
        }
    }

    private void cleanupFloorMaterial(GL2 gl, Room room) {
        // Check if texture *path* exists, then try to get texture from manager to disable
        if (room.getFloorTexturePath() != null && !room.getFloorTexturePath().trim().isEmpty()) {
            // We don't need to *get* it again unless we need the Texture object itself.
            // Disabling the texture unit might be enough if only one texture is used at a time.
            // However, explicit disable is safer.
            // Texture floorTex = textureManager.getTexture(gl, room.getFloorTexturePath()); // Get potentially cached texture
            Texture floorTex = textureManager.getCachedTexture(room.getFloorTexturePath()); // Assumes TextureManager has getCachedTexture
            if (floorTex != null) {
                floorTex.disable(gl);
            }

        }
    }

    private boolean setupWallMaterial(GL2 gl, Room room) { // Return true if texture is used
        Texture wallTex = textureManager.getTexture(gl, room.getWallTexturePath());
        if (wallTex != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            wallTex.enable(gl);
            wallTex.bind(gl);
            DrawingUtils.setColor(gl, Color.WHITE);
            // wallTex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            // wallTex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            return true;
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            DrawingUtils.setColor(gl, room.getWallColor());
            return false;
        }
    }

    private void cleanupWallMaterial(GL2 gl, Room room, boolean textureWasUsed) {
        if (textureWasUsed) {
            // Texture wallTex = textureManager.getTexture(gl, room.getWallTexturePath());
            Texture wallTex = textureManager.getCachedTexture(room.getWallTexturePath()); // Assumes TextureManager has getCachedTexture
            if (wallTex != null) {
                wallTex.disable(gl);
            }
        }
    }


    // --- Rectangular ---
    private void drawRectangularFloor(GL2 gl, Room room) {
        float w = room.getWidth(); float l = room.getLength();
        setupFloorMaterial(gl, room); // Setup color/texture

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 1, 0);
        float floorTexScale = 1.0f / Math.max(w,l); // Example scaling: fit once across max dim
        if (textureManager.isTextureLoaded(room.getFloorTexturePath())) {
            gl.glTexCoord2f(0, 0); gl.glVertex3f(0, 0, 0);
            gl.glTexCoord2f(w * floorTexScale, 0); gl.glVertex3f(w, 0, 0);
            gl.glTexCoord2f(w * floorTexScale, l * floorTexScale); gl.glVertex3f(w, 0, l);
            gl.glTexCoord2f(0, l * floorTexScale); gl.glVertex3f(0, 0, l);
        } else {
            gl.glVertex3f(0, 0, 0); gl.glVertex3f(w, 0, 0);
            gl.glVertex3f(w, 0, l); gl.glVertex3f(0, 0, l);
        }
        gl.glEnd();
        cleanupFloorMaterial(gl, room); // Disable texture if used
    }

    private void drawRectangularWalls(GL2 gl, Room room) {
        float w = room.getWidth(); float l = room.getLength(); float h = room.getHeight();
        boolean useTexture = setupWallMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        float wallTexScale = 1.0f; // Example: 1 texture unit per meter

        // Wall 1 (Back Z=0, Normal +Z)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(0,0,1);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(0,0,0); gl.glTexCoord2f(w*wallTexScale,0); gl.glVertex3f(w,0,0); gl.glTexCoord2f(w*wallTexScale,h*wallTexScale); gl.glVertex3f(w,h,0); gl.glTexCoord2f(0,h*wallTexScale); gl.glVertex3f(0,h,0); }
        else { gl.glVertex3f(0,0,0); gl.glVertex3f(w,0,0); gl.glVertex3f(w,h,0); gl.glVertex3f(0,h,0); }
        gl.glEnd();
        // Wall 2 (Left X=0, Normal +X)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(1,0,0);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(0,0,l); gl.glTexCoord2f(l*wallTexScale,0); gl.glVertex3f(0,0,0); gl.glTexCoord2f(l*wallTexScale,h*wallTexScale); gl.glVertex3f(0,h,0); gl.glTexCoord2f(0,h*wallTexScale); gl.glVertex3f(0,h,l); }
        else { gl.glVertex3f(0,0,l); gl.glVertex3f(0,0,0); gl.glVertex3f(0,h,0); gl.glVertex3f(0,h,l); }
        gl.glEnd();
        // Wall 3 (Right X=W, Normal -X)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(-1,0,0);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(w,0,0); gl.glTexCoord2f(l*wallTexScale,0); gl.glVertex3f(w,0,l); gl.glTexCoord2f(l*wallTexScale,h*wallTexScale); gl.glVertex3f(w,h,l); gl.glTexCoord2f(0,h*wallTexScale); gl.glVertex3f(w,h,0); }
        else { gl.glVertex3f(w,0,0); gl.glVertex3f(w,0,l); gl.glVertex3f(w,h,l); gl.glVertex3f(w,h,0); }
        gl.glEnd();
        // Wall 4 (Front Z=L, Normal -Z)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(0,0,-1);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(w,0,l); gl.glTexCoord2f(w*wallTexScale,0); gl.glVertex3f(0,0,l); gl.glTexCoord2f(w*wallTexScale,h*wallTexScale); gl.glVertex3f(0,h,l); gl.glTexCoord2f(0,h*wallTexScale); gl.glVertex3f(w,h,l); }
        else { gl.glVertex3f(w,0,l); gl.glVertex3f(0,0,l); gl.glVertex3f(0,h,l); gl.glVertex3f(w,h,l); }
        gl.glEnd();

        cleanupWallMaterial(gl, room, useTexture);
    }

    private void drawRectangularOutline(GL2 gl, Room room) {
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_LIGHTING); gl.glDisable(GL.GL_TEXTURE_2D); gl.glDisable(GL.GL_DEPTH_TEST);
        DrawingUtils.setColor(gl, Color.DARK_GRAY); gl.glLineWidth(2.0f);
        float lineY = 0.02f; float w = room.getWidth(); float l = room.getLength();
        gl.glBegin(GL_LINE_LOOP);
        gl.glVertex3f(0, lineY, 0); gl.glVertex3f(w, lineY, 0); gl.glVertex3f(w, lineY, l); gl.glVertex3f(0, lineY, l);
        gl.glEnd();
        gl.glPopAttrib();
    }


    // --- Circular ---
    private void drawCircularFloor(GL2 gl, Room room) {
        float r = room.getRadius();
        int segments = 36; // More segments for smoother circle
        setupFloorMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glNormal3f(0, 1, 0);
        // Center vertex
        if (textureManager.isTextureLoaded(room.getFloorTexturePath())) gl.glTexCoord2f(0.5f, 0.5f);
        gl.glVertex3f(0, 0, 0); // Assume center is at origin for simplicity
        // Edge vertices
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / (float) segments * 2.0f * (float) Math.PI;
            float x = r * (float) Math.cos(angle);
            float z = r * (float) Math.sin(angle);
            if (textureManager.isTextureLoaded(room.getFloorTexturePath())) {
                // Simple planar mapping from (-1..1) to (0..1)
                float tx = (x / r + 1.0f) * 0.5f;
                float ty = (z / r + 1.0f) * 0.5f;
                gl.glTexCoord2f(tx, ty);
            }
            gl.glVertex3f(x, 0, z);
        }
        gl.glEnd();
        cleanupFloorMaterial(gl, room);
    }

    private void drawCircularWall(GL2 gl, Room room) {
        float r = room.getRadius();
        float h = room.getHeight();
        int segments = 36;
        boolean useTexture = setupWallMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glBegin(GL2.GL_QUAD_STRIP); // Use Quad Strip for cylinder wall
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / (float) segments * 2.0f * (float) Math.PI;
            float x = r * (float) Math.cos(angle);
            float z = r * (float) Math.sin(angle);
            // Normal points radially outward
            float nx = x / r; // Normalize
            float nz = z / r; // Normalize
            gl.glNormal3f(nx, 0, nz);

            if (useTexture) {
                // Cylindrical mapping: U = angle/2pi, V = height/h
                float u = (float) i / (float) segments;
                gl.glTexCoord2f(u, 1.0f); // V=1 at top (New order: top vertex first)
            }
            gl.glVertex3f(x, h, z); // Top vertex (Draw top vertex first)

            if (useTexture) {
                float u = (float) i / (float) segments;
                gl.glTexCoord2f(u, 0); // V=0 at bottom
            }
            gl.glVertex3f(x, 0, z); // Bottom vertex (Then bottom vertex)
        }
        gl.glEnd();
        cleanupWallMaterial(gl, room, useTexture);
    }

    private void drawCircularOutline(GL2 gl, Room room) {
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_LIGHTING); gl.glDisable(GL.GL_TEXTURE_2D); gl.glDisable(GL.GL_DEPTH_TEST);
        DrawingUtils.setColor(gl, Color.DARK_GRAY); gl.glLineWidth(2.0f);
        float lineY = 0.02f; float r = room.getRadius(); int segments = 36;

        gl.glBegin(GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) { // Use < segments for loop
            float angle = (float) i / (float) segments * 2.0f * (float) Math.PI;
            float x = r * (float) Math.cos(angle);
            float z = r * (float) Math.sin(angle);
            gl.glVertex3f(x, lineY, z);
        }
        gl.glEnd();
        gl.glPopAttrib();
    }

    // --- L-Shaped ---
    private void drawLShapeFloor(GL2 gl, Room room) {
        float oW = room.getL_outerWidth(); float oL = room.getL_outerLength();
        float iW = room.getL_insetWidth(); float iL = room.getL_insetLength();
        setupFloorMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        // Tex coord scaling (simple world space mapping)
        float maxDim = Math.max(oW, oL);
        float uScale = 1.0f / maxDim;
        float vScale = 1.0f / maxDim;


        // Decompose into two rectangles
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 1, 0);

        // Rectangle 1 (Bottom part: 0,0 to oW, iL)
        if(textureManager.isTextureLoaded(room.getFloorTexturePath())){
            gl.glTexCoord2f(0 * uScale, 0 * vScale); gl.glVertex3f(0, 0, 0);
            gl.glTexCoord2f(oW * uScale, 0 * vScale); gl.glVertex3f(oW, 0, 0);
            gl.glTexCoord2f(oW * uScale, iL * vScale); gl.glVertex3f(oW, 0, iL);
            gl.glTexCoord2f(0 * uScale, iL * vScale); gl.glVertex3f(0, 0, iL);
        } else { gl.glVertex3f(0, 0, 0); gl.glVertex3f(oW, 0, 0); gl.glVertex3f(oW, 0, iL); gl.glVertex3f(0, 0, iL); }

        // Rectangle 2 (Left part: 0, iL to iW, oL)
        if(textureManager.isTextureLoaded(room.getFloorTexturePath())){
            gl.glTexCoord2f(0 * uScale, iL * vScale); gl.glVertex3f(0, 0, iL);
            gl.glTexCoord2f(iW * uScale, iL * vScale); gl.glVertex3f(iW, 0, iL);
            gl.glTexCoord2f(iW * uScale, oL * vScale); gl.glVertex3f(iW, 0, oL);
            gl.glTexCoord2f(0 * uScale, oL * vScale); gl.glVertex3f(0, 0, oL);
        } else { gl.glVertex3f(0, 0, iL); gl.glVertex3f(iW, 0, iL); gl.glVertex3f(iW, 0, oL); gl.glVertex3f(0, 0, oL); }

        gl.glEnd();
        cleanupFloorMaterial(gl, room);
    }

    private void drawLShapeWalls(GL2 gl, Room room) {
        float oW = room.getL_outerWidth(); float oL = room.getL_outerLength();
        float iW = room.getL_insetWidth(); float iL = room.getL_insetLength();
        float h = room.getHeight();
        boolean useTexture = setupWallMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        float wallTexScale = 1.0f; // Texture units per meter

        // Define vertices of the L-shape floor CCW from origin
        Vector3f[] verts = {
                new Vector3f(0,  0, 0),  // 0 Origin
                new Vector3f(oW, 0, 0),  // 1 Bottom Right
                new Vector3f(oW, 0, iL), // 2 Inner Corner Bottom
                new Vector3f(iW, 0, iL), // 3 Inner Corner Left
                new Vector3f(iW, 0, oL), // 4 Top Left Inner
                new Vector3f(0,  0, oL)  // 5 Top Left Outer
        };
        // Define normals pointing outwards (relative to inside the L)
        Vector3f[] normals = {
                new Vector3f( 0, 0, 1), // 0->1 Back wall (Normal +Z)
                new Vector3f(-1, 0, 0), // 1->2 Right wall (short) (Normal -X)
                new Vector3f( 0, 0, 1), // 2->3 Inner corner wall (back) (Normal +Z)
                new Vector3f(-1, 0, 0), // 3->4 Inner corner wall (right) (Normal -X)
                new Vector3f( 0, 0,-1), // 4->5 Front wall (short) (Normal -Z)
                new Vector3f( 1, 0, 0)  // 5->0 Left wall (Normal +X)
        };
        // Define texture lengths for walls (perimeter segments)
        float[] lengths = { oW, iL, oW - iW, oL - iL, iW, oL };

        // Draw walls
        gl.glBegin(GL2.GL_QUADS);
        for(int i=0; i < verts.length; i++) {
            Vector3f v1 = verts[i];
            Vector3f v2 = verts[(i + 1) % verts.length]; // Loop back to start
            Vector3f n = normals[i];
            float len = lengths[i]; // Use precalculated length for texture coord U

            gl.glNormal3f(n.x, n.y, n.z);
            if(useTexture) {
                gl.glTexCoord2f(0, 0); gl.glVertex3f(v1.x, 0, v1.z);                 // Bottom Left (U=0, V=0)
                gl.glTexCoord2f(len * wallTexScale, 0); gl.glVertex3f(v2.x, 0, v2.z); // Bottom Right(U=len*scale, V=0)
                gl.glTexCoord2f(len * wallTexScale, h * wallTexScale); gl.glVertex3f(v2.x, h, v2.z); // Top Right   (U=len*scale, V=h*scale)
                gl.glTexCoord2f(0, h * wallTexScale); gl.glVertex3f(v1.x, h, v1.z);   // Top Left    (U=0, V=h*scale)
            } else {
                gl.glVertex3f(v1.x, 0, v1.z); gl.glVertex3f(v2.x, 0, v2.z);
                gl.glVertex3f(v2.x, h, v2.z); gl.glVertex3f(v1.x, h, v1.z);
            }
        }
        gl.glEnd();
        cleanupWallMaterial(gl, room, useTexture);
    }

    private void drawLShapeOutline(GL2 gl, Room room) {
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_LIGHTING); gl.glDisable(GL.GL_TEXTURE_2D); gl.glDisable(GL.GL_DEPTH_TEST);
        DrawingUtils.setColor(gl, Color.DARK_GRAY); gl.glLineWidth(2.0f);
        float lineY = 0.02f;
        float oW = room.getL_outerWidth(); float oL = room.getL_outerLength();
        float iW = room.getL_insetWidth(); float iL = room.getL_insetLength();

        gl.glBegin(GL_LINE_LOOP);
        gl.glVertex3f(0,  lineY, 0);
        gl.glVertex3f(oW, lineY, 0);
        gl.glVertex3f(oW, lineY, iL);
        gl.glVertex3f(iW, lineY, iL);
        gl.glVertex3f(iW, lineY, oL);
        gl.glVertex3f(0,  lineY, oL);
        gl.glEnd();
        gl.glPopAttrib();
    }


    // --- T-Shaped ---
    private void drawTShapeFloor(GL2 gl, Room room) {
        float bW = room.getT_barWidth(); float bL = room.getT_barLength();
        float sW = room.getT_stemWidth(); float sL = room.getT_stemLength();
        float stemStartX = (bW - sW) / 2.0f;
        float stemEndX = stemStartX + sW;
        float totalLength = bL + sL;

        setupFloorMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        // Tex coord scaling
        float maxDim = Math.max(bW, totalLength);
        float uScale = 1.0f / maxDim;
        float vScale = 1.0f / maxDim;

        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 1, 0);

        // Rectangle 1: Top Bar (0,0 to bW, bL)
        if(textureManager.isTextureLoaded(room.getFloorTexturePath())){
            gl.glTexCoord2f(0 * uScale, 0 * vScale); gl.glVertex3f(0, 0, 0);
            gl.glTexCoord2f(bW * uScale, 0 * vScale); gl.glVertex3f(bW, 0, 0);
            gl.glTexCoord2f(bW * uScale, bL * vScale); gl.glVertex3f(bW, 0, bL);
            gl.glTexCoord2f(0 * uScale, bL * vScale); gl.glVertex3f(0, 0, bL);
        } else { gl.glVertex3f(0, 0, 0); gl.glVertex3f(bW, 0, 0); gl.glVertex3f(bW, 0, bL); gl.glVertex3f(0, 0, bL); }

        // Rectangle 2: Stem (stemStartX, bL to stemEndX, totalLength)
        if(textureManager.isTextureLoaded(room.getFloorTexturePath())){
            gl.glTexCoord2f(stemStartX * uScale, bL * vScale); gl.glVertex3f(stemStartX, 0, bL);
            gl.glTexCoord2f(stemEndX * uScale, bL * vScale); gl.glVertex3f(stemEndX, 0, bL);
            gl.glTexCoord2f(stemEndX * uScale, totalLength * vScale); gl.glVertex3f(stemEndX, 0, totalLength);
            gl.glTexCoord2f(stemStartX * uScale, totalLength * vScale); gl.glVertex3f(stemStartX, 0, totalLength);
        } else { gl.glVertex3f(stemStartX, 0, bL); gl.glVertex3f(stemEndX, 0, bL); gl.glVertex3f(stemEndX, 0, totalLength); gl.glVertex3f(stemStartX, 0, totalLength); }

        gl.glEnd();
        cleanupFloorMaterial(gl, room);
    }

    private void drawTShapeWalls(GL2 gl, Room room) {
        float bW = room.getT_barWidth(); float bL = room.getT_barLength();
        float sW = room.getT_stemWidth(); float sL = room.getT_stemLength();
        float h = room.getHeight();
        float stemStartX = (bW - sW) / 2.0f;
        float stemEndX = stemStartX + sW;
        float totalLength = bL + sL;

        boolean useTexture = setupWallMaterial(gl, room);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        float wallTexScale = 1.0f; // Texture units per meter

        // Define vertices CCW
        Vector3f[] verts = {
                new Vector3f(0,          0, 0),          // 0 Top-Left Back
                new Vector3f(bW,         0, 0),          // 1 Top-Right Back
                new Vector3f(bW,         0, bL),         // 2 Top-Right Front of Bar
                new Vector3f(stemEndX,   0, bL),         // 3 Stem Top Right
                new Vector3f(stemEndX,   0, totalLength),// 4 Stem Bottom Right
                new Vector3f(stemStartX, 0, totalLength),// 5 Stem Bottom Left
                new Vector3f(stemStartX, 0, bL),         // 6 Stem Top Left
                new Vector3f(0,          0, bL)          // 7 Top-Left Front of Bar
        };
        // Define outward normals
        Vector3f[] normals = {
                new Vector3f( 0, 0, 1), // 0->1 Back Wall (+Z)
                new Vector3f(-1, 0, 0), // 1->2 Right Wall of Bar (-X)
                new Vector3f( 0, 0,-1), // 2->3 Top right shoulder (-Z)
                new Vector3f(-1, 0, 0), // 3->4 Right wall of Stem (-X)
                new Vector3f( 0, 0,-1), // 4->5 Bottom wall of Stem (-Z)
                new Vector3f( 1, 0, 0), // 5->6 Left wall of Stem (+X)
                new Vector3f( 0, 0,-1), // 6->7 Top left shoulder (-Z)
                new Vector3f( 1, 0, 0)  // 7->0 Left wall of Bar (+X)
        };
        // Define wall lengths
        float[] lengths = { bW, bL, bW - stemEndX, sL, sW, sL, stemStartX, bL };

        gl.glBegin(GL2.GL_QUADS);
        for (int i = 0; i < verts.length; i++) {
            Vector3f v1 = verts[i];
            Vector3f v2 = verts[(i + 1) % verts.length];
            Vector3f n = normals[i];
            float len = lengths[i];

            gl.glNormal3f(n.x, n.y, n.z);
            if(useTexture) {
                gl.glTexCoord2f(0, 0); gl.glVertex3f(v1.x, 0, v1.z);
                gl.glTexCoord2f(len * wallTexScale, 0); gl.glVertex3f(v2.x, 0, v2.z);
                gl.glTexCoord2f(len * wallTexScale, h * wallTexScale); gl.glVertex3f(v2.x, h, v2.z);
                gl.glTexCoord2f(0, h * wallTexScale); gl.glVertex3f(v1.x, h, v1.z);
            } else {
                gl.glVertex3f(v1.x, 0, v1.z); gl.glVertex3f(v2.x, 0, v2.z);
                gl.glVertex3f(v2.x, h, v2.z); gl.glVertex3f(v1.x, h, v1.z);
            }
        }
        gl.glEnd();
        cleanupWallMaterial(gl, room, useTexture);
    }

    private void drawTShapeOutline(GL2 gl, Room room) {
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_LIGHTING); gl.glDisable(GL.GL_TEXTURE_2D); gl.glDisable(GL.GL_DEPTH_TEST);
        DrawingUtils.setColor(gl, Color.DARK_GRAY); gl.glLineWidth(2.0f);
        float lineY = 0.02f;
        float bW = room.getT_barWidth(); float bL = room.getT_barLength();
        float sW = room.getT_stemWidth(); float sL = room.getT_stemLength();
        float stemStartX = (bW - sW) / 2.0f;
        float stemEndX = stemStartX + sW;
        float totalLength = bL + sL;

        gl.glBegin(GL_LINE_LOOP);
        gl.glVertex3f(0,          lineY, 0);
        gl.glVertex3f(bW,         lineY, 0);
        gl.glVertex3f(bW,         lineY, bL);
        gl.glVertex3f(stemEndX,   lineY, bL);
        gl.glVertex3f(stemEndX,   lineY, totalLength);
        gl.glVertex3f(stemStartX, lineY, totalLength);
        gl.glVertex3f(stemStartX, lineY, bL);
        gl.glVertex3f(0,          lineY, bL);
        gl.glEnd();
        gl.glPopAttrib();
    }


    // --- Selection Indicator (Remains the same) ---
    private void drawSelectionIndicator(GL2 gl, Furniture furniture) {
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_ENABLE_BIT | GL2.GL_POLYGON_BIT | GL2.GL_LINE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glDepthMask(false); // Don't write to depth buffer
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE); // Wireframe
        gl.glLineWidth(2.5f);
        DrawingUtils.setColor(gl, Color.GREEN.darker()); // Use utility

        gl.glPushMatrix();
        // Apply same transformations as the furniture object itself
        Vector3f pos = furniture.getPosition();
        Vector3f rot = furniture.getRotation();
        gl.glTranslatef(pos.x, pos.y, pos.z);
        gl.glRotatef(rot.y, 0, 1, 0);
        gl.glRotatef(rot.x, 1, 0, 0);
        gl.glRotatef(rot.z, 0, 0, 1);

        // Draw wire box centered vertically around furniture height, slightly larger
        float margin = 0.03f;
        // Center indicator vertically based on furniture's object space origin (which is base)
        gl.glTranslatef(0, furniture.getHeight() / 2f, 0);
        // The DrawingUtils.drawBox method requires a boolean 'hasTexture' argument.
        // Since this is a wireframe selection box, it does not use textures.
        // Pass 'false' for the hasTexture argument.
        DrawingUtils.drawBox(gl, furniture.getWidth() + margin, furniture.getHeight() + margin, furniture.getDepth() + margin, false);
        gl.glPopMatrix();

        gl.glPopAttrib(); // Restore states
    }

    // Helper to check if texture was loaded (avoids redundant checks)
    // Needs TextureManager instance
    // This field was potentially confusing; the textureManager field IS the cache manager
    // private TextureManager textureCache;

    // Constructor modification needed if not already done:
    // this.textureCache = this.textureManager; // Assign texture manager - This is not needed if we just use this.textureManager


    // Or, make TextureManager provide this check:
    // if (textureManager.isTextureLoaded(path)) ...
    // Example implementation within DesignRenderer:
    private boolean isTextureLoaded(String path) {
        return textureManager != null && textureManager.isTextureLoaded(path);
        // Requires isTextureLoaded method in TextureManager:
        // public boolean isTextureLoaded(String path) {
        //     return textureCache.containsKey(path) && textureCache.get(path) != null;
        // }
    }
}