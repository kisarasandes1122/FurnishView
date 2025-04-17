import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;

import java.awt.Color;
import java.awt.Font;

import static com.jogamp.opengl.GL.GL_TRIANGLE_FAN;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;


public class DesignRenderer implements GLEventListener {

    private GLU glu;
    private DesignModel designModel;
    private TextRenderer textRenderer; // Optional: For displaying mode, etc.

    // Managers and Helpers
    private CameraManager cameraManager;
    private TextureManager textureManager;
    private FurnitureRenderer furnitureRenderer;
    // private PickingHelper pickingHelper; // To be added later

    // State specific to this renderer
    private boolean showGrid = true;
    private int viewWidth = 1;  // Store viewport dimensions
    private int viewHeight = 1;

    // Cached matrices/viewport for picking (initialize placeholders) - To be used later
    private final double[] lastModelview = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
    private final double[] lastProjection = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
    private final int[] lastViewport = {0,0,1,1};


    public DesignRenderer(DesignModel model) {
        if (model == null) {
            throw new IllegalArgumentException("DesignModel cannot be null");
        }
        this.designModel = model;
        this.glu = new GLU();

        // Initialize managers and helpers needed in this commit
        this.cameraManager = new CameraManager();
        this.textureManager = new TextureManager();
        this.furnitureRenderer = new FurnitureRenderer(this.textureManager);
        // this.pickingHelper = new PickingHelper(); // Initialize later

        // Initialize camera based on the initial model's room
        updateCameraForModel();
        // Set initial view mode (can be changed later via UI)
        set3DMode(cameraManager.is3DMode());
    }

    // Update the model this renderer draws
    public void setDesignModel(DesignModel newModel) {
        if (newModel == null) {
            throw new IllegalArgumentException("Cannot set DesignModel to null");
        }
        this.designModel = newModel;
        // When the model changes (e.g., loading a file), clear the texture cache
        // and reset the camera view based on the new room.
        textureManager.clearCache();
        updateCameraForModel();
        set3DMode(cameraManager.is3DMode()); // Re-apply mode settings
    }

    // Reset camera target and update dimensions based on the current model's room
    public void updateCameraForModel() {
        if (cameraManager == null || designModel == null) return;

        Room room = designModel.getRoom();
        if (room != null) {
            // Pass the Room object to the camera manager
            cameraManager.resetTargetToCenter(room);
        } else {
            // Fallback if room is somehow null
            cameraManager.resetTargetToCenter(null);
        }
        // The resetTargetToCenter method now handles updating internal dims and distance
    }

    // --- Camera Interaction Delegation ---
    // These methods will be called by MainAppFrame based on mouse/keyboard input
    public void rotateCamera(float deltaX, float deltaY) {
        if (cameraManager != null) cameraManager.rotate(deltaX, deltaY);
    }

    public void zoomCamera(float delta) {
        if (cameraManager != null) cameraManager.zoom(delta);
    }

    public void panCamera(float deltaX, float deltaY) {
        if (cameraManager != null) cameraManager.pan(deltaX, deltaY, viewWidth, viewHeight);
    }

    // --- View Mode Control ---
    public void set3DMode(boolean is3D) {
        if (cameraManager != null) {
            cameraManager.setMode(is3D);
            // Optionally trigger a reshape if needed, though JOGL usually handles it
            // System.err.println("Mode changed, might need reshape");
        }
    }

    public boolean is3DMode() {
        return (cameraManager != null) && cameraManager.is3DMode();
    }

    // --- Picking Delegation (Placeholders) ---
    // To be fully implemented later when PickingHelper is added
    public Vector3f screenToWorldFloor(int screenX, int screenY) {
        // Placeholder implementation
        System.err.println("PickingHelper not yet implemented for screenToWorldFloor");
        // In a real implementation, call pickingHelper.screenToWorldOnPlane(...)
        return null;
    }

    public Furniture pickFurniture(int screenX, int screenY) {
        // Placeholder implementation
        System.err.println("PickingHelper not yet implemented for pickFurniture");
        // In a real implementation, call pickingHelper.pickFurniture(...)
        return null;
    }

    // --- Other Controls ---
    public void setShowGrid(boolean show) {
        this.showGrid = show;
    }
    public CameraManager getCameraManager() {
        return cameraManager;
    }


    // --- GLEventListener Methods ---

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        System.out.println("GL Init Called"); // Debug message

        // Basic GL setup
        gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f); // Light grey background
        gl.glEnable(GL2.GL_DEPTH_TEST);          // Enable depth testing
        gl.glDepthFunc(GL2.GL_LEQUAL);           // Type of depth test

        // Culling setup (optional but good practice)
        gl.glEnable(GL2.GL_CULL_FACE);          // Enable face culling
        gl.glCullFace(GL2.GL_BACK);             // Cull back faces
        gl.glFrontFace(GL2.GL_CCW);             // Counter-clockwise vertices are front faces (OpenGL default)

        // Lighting setup
        gl.glEnable(GL2.GL_LIGHTING);            // Enable lighting
        gl.glEnable(GL2.GL_LIGHT0);              // Enable light source 0
        gl.glShadeModel(GL2.GL_SMOOTH);         // Use smooth shading

        // Define light properties for light 0
        // Positioned somewhat up and away
        float[] lightPos = {5.0f, 10.0f, 5.0f, 1.0f}; // Positional light (w=1)
        float[] lightAmbient = {0.3f, 0.3f, 0.3f, 1.0f}; // Ambient light contribution
        float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1.0f}; // Diffuse light contribution
        float[] lightSpecular = {0.5f, 0.5f, 0.5f, 1.0f}; // Specular highlight contribution
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);

        // Material setup
        // Enable color tracking: allows glColor to set material properties (ambient/diffuse)
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        // Define default specular reflection and shininess for materials
        float[] matSpecular = { 0.2f, 0.2f, 0.2f, 1.0f };
        float[] matShininess = { 10.0f }; // Shininess exponent
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpecular, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShininess, 0);

        // Anti-aliasing hints (optional)
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        // gl.glEnable(GL2.GL_POLYGON_SMOOTH); // Often causes artifacts
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);

        // Initialize TextRenderer (optional for now)
        try {
            textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 12));
        } catch (GLException e) {
            System.err.println("TextRenderer initialization failed: " + e.getMessage());
            textRenderer = null; // Ensure it's null if failed
        }

        System.out.println("GL Init Finished");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        System.out.println("GL Dispose Called"); // Debug message

        // Dispose resources
        if (textRenderer != null) {
            textRenderer.dispose();
            textRenderer = null;
        }
        if (textureManager != null) {
            textureManager.disposeAll(gl); // Dispose textures via manager
        }
        glu = null; // Release GLU object
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Basic checks
        if (designModel == null || cameraManager == null || furnitureRenderer == null ) {
            System.err.println("Renderer components not ready, skipping display.");
            return;
        }
        GL2 gl = drawable.getGL().getGL2();

        // Clear buffers
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // --- Camera Setup ---
        // Apply projection first (based on mode and aspect ratio)
        // Note: Aspect ratio is handled in reshape, but applyProjection needs it.
        // We calculate aspect here based on stored width/height.
        float aspect = (float) viewWidth / Math.max(1.0f, viewHeight);
        cameraManager.applyProjection(gl, glu, aspect);

        // Apply camera look-at transformation (sets ModelView matrix)
        cameraManager.applyLookAt(gl, glu);

        // --- Scene Lighting ---
        // Re-position light 0 if needed (e.g., relative to camera or fixed world pos)
        // For now, keep light fixed as defined in init.
        // float[] lightPos = {5.0f, 10.0f, 5.0f, 1.0f};
        // gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);


        // --- Drawing Scene Elements ---

        // Draw the grid if enabled
        if (showGrid) {
            // Grid size and spacing can be adjusted
            DrawingUtils.drawGrid(gl, 20, 1.0f);
        }

        // Draw the room
        Room room = designModel.getRoom();
        if (room != null) {
            // Call the appropriate drawing function based on room shape
            drawRoom(gl, room);
        }

        // Draw the furniture items
        if (designModel.getFurnitureList() != null) {
            for (Furniture item : designModel.getFurnitureList()) {
                furnitureRenderer.drawFurniture(gl, item); // Delegate to FurnitureRenderer
            }
        }

        // Draw selection indicator (placeholder for later)
        Furniture selected = designModel.getSelectedFurniture();
        if (selected != null) {
            //drawSelectionIndicator(gl, selected); // Implement later
        }

        // --- Capture Matrices for Picking (placeholder) ---
        // captureMatricesForPicking(gl); // Implement later

        // Optional: Draw UI elements like current mode using textRenderer
        // drawHUD(gl);

        // Flush the GL pipeline
        gl.glFlush();
    }


    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        System.out.println("GL Reshape Called: " + width + "x" + height); // Debug

        if (height <= 0) height = 1; // Prevent division by zero

        // Store new viewport dimensions
        this.viewWidth = width;
        this.viewHeight = height;

        // Set the OpenGL viewport to cover the new window size
        gl.glViewport(0, 0, width, height);

        // --- Projection Setup ---
        // Aspect ratio is calculated here and passed to camera manager
        float aspect = (float) width / (float) height;
        if (cameraManager != null) {
            cameraManager.applyProjection(gl, glu, aspect); // Delegate projection setup
        } else {
            // Fallback projection if camera manager isn't ready (shouldn't happen after init)
            System.err.println("CameraManager is null during reshape!");
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            glu.gluPerspective(45.0f, aspect, 0.1f, 100.0f); // Default perspective
            gl.glMatrixMode(GL2.GL_MODELVIEW); // Switch back
            gl.glLoadIdentity();
        }

        // --- Capture Projection Matrix & Viewport After Setting (for picking - later) ---
        // captureMatricesForPicking(gl);

        // It's good practice to ensure ModelView is reset after changing projection
        // although applyLookAt in display() will handle this anyway.
        // gl.glMatrixMode(GL2.GL_MODELVIEW);
        // gl.glLoadIdentity();
    }

    // --- Room Drawing Logic ---

    // Central method to draw the room based on its shape
    private void drawRoom(GL2 gl, Room room) {
        // Save state - including culling, texturing, color, polygon mode etc.
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_POLYGON_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_LIGHTING_BIT);

        // Draw floor first
        boolean cullingWasEnabled = gl.glIsEnabled(GL2.GL_CULL_FACE);
        if (cullingWasEnabled) gl.glDisable(GL2.GL_CULL_FACE); // Disable culling for floor
        setupFloorMaterial(gl, room);
        drawRoomFloor(gl, room);
        cleanupFloorMaterial(gl, room);
        if (cullingWasEnabled) gl.glEnable(GL2.GL_CULL_FACE); // Re-enable culling

        // Draw walls or outline based on mode
        if (cameraManager.is3DMode()) {
            boolean textureWasUsed = setupWallMaterial(gl, room);
            drawRoomWalls(gl, room);
            cleanupWallMaterial(gl, room, textureWasUsed);
        } else { // 2D Mode Outline
            drawRoomOutline(gl, room);
        }

        gl.glPopAttrib(); // Restore attributes
    }

    // Helper to set up material/texture for the floor
    private void setupFloorMaterial(GL2 gl, Room room) {
        Texture floorTex = textureManager.getTexture(gl, room.getFloorTexturePath());
        if (floorTex != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            floorTex.enable(gl);
            floorTex.bind(gl);
            DrawingUtils.setColor(gl, Color.WHITE); // Use white for textured surfaces
            // Texture parameters (like repeat) should be set in TextureManager or here
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            DrawingUtils.setColor(gl, room.getFloorColor());
        }
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    // Helper to clean up floor material state
    private void cleanupFloorMaterial(GL2 gl, Room room) {
        // If a texture path exists, try to disable the corresponding texture
        if (room.getFloorTexturePath() != null && !room.getFloorTexturePath().trim().isEmpty()) {
            Texture floorTex = textureManager.getCachedTexture(room.getFloorTexturePath());
            if (floorTex != null) {
                floorTex.disable(gl);
            }
            // glDisable(GL.GL_TEXTURE_2D) is handled by glPopAttrib if it was enabled
        }
    }

    // Helper to set up material/texture for walls
    private boolean setupWallMaterial(GL2 gl, Room room) { // Returns true if texture used
        Texture wallTex = textureManager.getTexture(gl, room.getWallTexturePath());
        if (wallTex != null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            wallTex.enable(gl);
            wallTex.bind(gl);
            DrawingUtils.setColor(gl, Color.WHITE);
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            return true;
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            DrawingUtils.setColor(gl, room.getWallColor());
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            return false;
        }
    }

    // Helper to clean up wall material state
    private void cleanupWallMaterial(GL2 gl, Room room, boolean textureWasUsed) {
        if (textureWasUsed) {
            Texture wallTex = textureManager.getCachedTexture(room.getWallTexturePath());
            if (wallTex != null) {
                wallTex.disable(gl);
            }
        }
    }

    // --- Shape-Specific Drawing Methods ---

    // Draw floor based on shape
    private void drawRoomFloor(GL2 gl, Room room) {
        switch (room.getShape()) {
            case RECTANGULAR: drawRectangularFloor(gl, room); break;
            case CIRCULAR:    drawCircularFloor(gl, room); break;
            case L_SHAPED:    drawLShapeFloor(gl, room); break;
            case T_SHAPED:    drawTShapeFloor(gl, room); break;
        }
    }

    // Draw walls based on shape (only called in 3D mode)
    private void drawRoomWalls(GL2 gl, Room room) {
        switch (room.getShape()) {
            case RECTANGULAR: drawRectangularWalls(gl, room); break;
            case CIRCULAR:    drawCircularWall(gl, room); break;
            case L_SHAPED:    drawLShapeWalls(gl, room); break;
            case T_SHAPED:    drawTShapeWalls(gl, room); break;
        }
    }

    // Draw outline based on shape (only called in 2D mode)
    private void drawRoomOutline(GL2 gl, Room room) {
        // Common outline setup
        gl.glPushAttrib(GL2.GL_LIGHTING_BIT | GL2.GL_LINE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_TEXTURE_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_DEPTH_TEST); // Draw outline on top
        DrawingUtils.setColor(gl, Color.DARK_GRAY.darker()); // Dark outline color
        gl.glLineWidth(2.0f);
        float lineY = 0.02f; // Slightly above floor

        switch (room.getShape()) {
            case RECTANGULAR: drawRectangularOutline(gl, room, lineY); break;
            case CIRCULAR:    drawCircularOutline(gl, room, lineY); break;
            case L_SHAPED:    drawLShapeOutline(gl, room, lineY); break;
            case T_SHAPED:    drawTShapeOutline(gl, room, lineY); break;
        }
        gl.glPopAttrib(); // Restore state
    }


    // --- Rectangular Drawing ---
    private void drawRectangularFloor(GL2 gl, Room room) {
        float w = room.getWidth(); float l = room.getLength();
        boolean useTexture = textureManager.isTextureLoaded(room.getFloorTexturePath());
        float texScale = 1.0f; // Example scaling: 1 texture unit per meter

        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 1, 0); // Normal pointing up
        if(useTexture) gl.glTexCoord2f(0, 0);                 gl.glVertex3f(0, 0, 0);
        if(useTexture) gl.glTexCoord2f(w * texScale, 0);      gl.glVertex3f(w, 0, 0);
        if(useTexture) gl.glTexCoord2f(w * texScale, l * texScale); gl.glVertex3f(w, 0, l);
        if(useTexture) gl.glTexCoord2f(0, l * texScale);      gl.glVertex3f(0, 0, l);
        if(!useTexture) { gl.glVertex3f(0, 0, 0); gl.glVertex3f(w, 0, 0); gl.glVertex3f(w, 0, l); gl.glVertex3f(0, 0, l); }
        gl.glEnd();
    }

    private void drawRectangularWalls(GL2 gl, Room room) {
        float w = room.getWidth(); float l = room.getLength(); float h = room.getHeight();
        boolean useTexture = textureManager.isTextureLoaded(room.getWallTexturePath());
        float texScale = 1.0f; // Example scaling

        // Wall 1 (Back Z=0, Normal +Z)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(0,0,1);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(0,0,0); gl.glTexCoord2f(w*texScale,0); gl.glVertex3f(w,0,0); gl.glTexCoord2f(w*texScale,h*texScale); gl.glVertex3f(w,h,0); gl.glTexCoord2f(0,h*texScale); gl.glVertex3f(0,h,0); }
        else { gl.glVertex3f(0,0,0); gl.glVertex3f(w,0,0); gl.glVertex3f(w,h,0); gl.glVertex3f(0,h,0); }
        gl.glEnd();
        // Wall 2 (Left X=0, Normal +X)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(1,0,0);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(0,0,l); gl.glTexCoord2f(l*texScale,0); gl.glVertex3f(0,0,0); gl.glTexCoord2f(l*texScale,h*texScale); gl.glVertex3f(0,h,0); gl.glTexCoord2f(0,h*texScale); gl.glVertex3f(0,h,l); }
        else { gl.glVertex3f(0,0,l); gl.glVertex3f(0,0,0); gl.glVertex3f(0,h,0); gl.glVertex3f(0,h,l); }
        gl.glEnd();
        // Wall 3 (Right X=W, Normal -X)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(-1,0,0);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(w,0,0); gl.glTexCoord2f(l*texScale,0); gl.glVertex3f(w,0,l); gl.glTexCoord2f(l*texScale,h*texScale); gl.glVertex3f(w,h,l); gl.glTexCoord2f(0,h*texScale); gl.glVertex3f(w,h,0); }
        else { gl.glVertex3f(w,0,0); gl.glVertex3f(w,0,l); gl.glVertex3f(w,h,l); gl.glVertex3f(w,h,0); }
        gl.glEnd();
        // Wall 4 (Front Z=L, Normal -Z)
        gl.glBegin(GL2.GL_QUADS); gl.glNormal3f(0,0,-1);
        if(useTexture) { gl.glTexCoord2f(0,0); gl.glVertex3f(w,0,l); gl.glTexCoord2f(w*texScale,0); gl.glVertex3f(0,0,l); gl.glTexCoord2f(w*texScale,h*texScale); gl.glVertex3f(0,h,l); gl.glTexCoord2f(0,h*texScale); gl.glVertex3f(w,h,l); }
        else { gl.glVertex3f(w,0,l); gl.glVertex3f(0,0,l); gl.glVertex3f(0,h,l); gl.glVertex3f(w,h,l); }
        gl.glEnd();
    }

    private void drawRectangularOutline(GL2 gl, Room room, float lineY) {
        float w = room.getWidth(); float l = room.getLength();
        gl.glBegin(GL_LINE_LOOP);
        gl.glVertex3f(0, lineY, 0);
        gl.glVertex3f(w, lineY, 0);
        gl.glVertex3f(w, lineY, l);
        gl.glVertex3f(0, lineY, l);
        gl.glEnd();
    }

    // --- Circular Drawing ---
    private void drawCircularFloor(GL2 gl, Room room) {
        float r = room.getRadius();
        int segments = 36; // Increase for smoother circle
        boolean useTexture = textureManager.isTextureLoaded(room.getFloorTexturePath());

        gl.glBegin(GL_TRIANGLE_FAN);
        gl.glNormal3f(0, 1, 0);
        // Center vertex
        if (useTexture) gl.glTexCoord2f(0.5f, 0.5f); // Center of texture
        gl.glVertex3f(0, 0, 0); // Assume circle center is at origin (0,0,0)

        // Edge vertices
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / (float) segments * 2.0f * (float) Math.PI;
            float x = r * (float) Math.cos(angle);
            float z = r * (float) Math.sin(angle);
            if (useTexture) {
                // Simple planar mapping: map world coords (-r..r) to tex coords (0..1)
                float tx = (x / r + 1.0f) * 0.5f;
                float ty = (z / r + 1.0f) * 0.5f;
                gl.glTexCoord2f(tx, ty);
            }
            gl.glVertex3f(x, 0, z);
        }
        gl.glEnd();
    }

    private void drawCircularWall(GL2 gl, Room room) {
        float r = room.getRadius();
        float h = room.getHeight();
        int segments = 36;
        boolean useTexture = textureManager.isTextureLoaded(room.getWallTexturePath());
        float texScaleV = 1.0f; // Vertical texture scale

        gl.glBegin(GL2.GL_QUAD_STRIP); // Use Quad Strip for cylinder wall
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / (float) segments * 2.0f * (float) Math.PI;
            float x = r * (float) Math.cos(angle);
            float z = r * (float) Math.sin(angle);

            // Normal points radially outward from the center (0,0,0)
            Vector3f normal = new Vector3f(x, 0, z);
            normal.normalize(); // Ensure unit length normal
            gl.glNormal3f(normal.x, normal.y, normal.z);

            // Bottom vertex
            if (useTexture) {
                // Cylindrical mapping: U = angle progress, V = height progress
                float u = (float) i / (float) segments; // U coordinate wraps around
                gl.glTexCoord2f(u, 0);                  // V=0 at bottom
            }
            gl.glVertex3f(x, 0, z);

            // Top vertex
            if (useTexture) {
                float u = (float) i / (float) segments;
                gl.glTexCoord2f(u, h * texScaleV);      // V scales with height
            }
            gl.glVertex3f(x, h, z);
        }
        gl.glEnd();
    }

    private void drawCircularOutline(GL2 gl, Room room, float lineY) {
        float r = room.getRadius();
        int segments = 36;
        gl.glBegin(GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) { // Use < segments for loop
            float angle = (float) i / (float) segments * 2.0f * (float) Math.PI;
            float x = r * (float) Math.cos(angle);
            float z = r * (float) Math.sin(angle);
            gl.glVertex3f(x, lineY, z); // Assume center at (0,0,0)
        }
        gl.glEnd();
    }

    // --- L-Shaped Drawing ---
    private void drawLShapeFloor(GL2 gl, Room room) {
        float oW = room.getL_outerWidth(); float oL = room.getL_outerLength();
        float iW = room.getL_insetWidth(); float iL = room.getL_insetLength();
        boolean useTexture = textureManager.isTextureLoaded(room.getFloorTexturePath());
        float texScale = 1.0f; // Example scaling

        // Decompose into two rectangles
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 1, 0);

        // Rectangle 1 (Bottom part: 0,0 to oW, iL)
        if(useTexture) { gl.glTexCoord2f(0*texScale, 0*texScale); gl.glVertex3f(0, 0, 0); gl.glTexCoord2f(oW*texScale, 0*texScale); gl.glVertex3f(oW, 0, 0); gl.glTexCoord2f(oW*texScale, iL*texScale); gl.glVertex3f(oW, 0, iL); gl.glTexCoord2f(0*texScale, iL*texScale); gl.glVertex3f(0, 0, iL); }
        else { gl.glVertex3f(0, 0, 0); gl.glVertex3f(oW, 0, 0); gl.glVertex3f(oW, 0, iL); gl.glVertex3f(0, 0, iL); }

        // Rectangle 2 (Left part: 0, iL to iW, oL)
        if(useTexture) { gl.glTexCoord2f(0*texScale, iL*texScale); gl.glVertex3f(0, 0, iL); gl.glTexCoord2f(iW*texScale, iL*texScale); gl.glVertex3f(iW, 0, iL); gl.glTexCoord2f(iW*texScale, oL*texScale); gl.glVertex3f(iW, 0, oL); gl.glTexCoord2f(0*texScale, oL*texScale); gl.glVertex3f(0, 0, oL); }
        else { gl.glVertex3f(0, 0, iL); gl.glVertex3f(iW, 0, iL); gl.glVertex3f(iW, 0, oL); gl.glVertex3f(0, 0, oL); }

        gl.glEnd();
    }

    private void drawLShapeWalls(GL2 gl, Room room) {
        float oW = room.getL_outerWidth(); float oL = room.getL_outerLength();
        float iW = room.getL_insetWidth(); float iL = room.getL_insetLength();
        float h = room.getHeight();
        boolean useTexture = textureManager.isTextureLoaded(room.getWallTexturePath());
        float texScale = 1.0f;

        // Define vertices of the L-shape floor CCW from origin (0,0,0)
        Vector3f[] verts = {
                new Vector3f(0,  0, 0),  // 0 Origin
                new Vector3f(oW, 0, 0),  // 1 Bottom Right
                new Vector3f(oW, 0, iL), // 2 Inner Corner Bottom Right
                new Vector3f(iW, 0, iL), // 3 Inner Corner Top Right
                new Vector3f(iW, 0, oL), // 4 Top Left Inner
                new Vector3f(0,  0, oL)  // 5 Top Left Outer
        };
        // Define outward normals for each wall segment (v[i] -> v[i+1])
        Vector3f[] normals = {
                new Vector3f( 0, 0,  1), // 0->1 (Back Wall) Normal +Z
                new Vector3f(-1, 0,  0), // 1->2 (Right Wall Short) Normal -X
                new Vector3f( 0, 0,  1), // 2->3 (Inner Wall Back) Normal +Z
                new Vector3f(-1, 0,  0), // 3->4 (Inner Wall Right) Normal -X
                new Vector3f( 0, 0, -1), // 4->5 (Front Wall Short) Normal -Z
                new Vector3f( 1, 0,  0)  // 5->0 (Left Wall Long) Normal +X
        };
        // Define lengths for texture U coordinate scaling
        float[] lengths = { oW, iL, oW - iW, oL - iL, iW, oL };

        // Draw walls as quads
        gl.glBegin(GL2.GL_QUADS);
        for(int i=0; i < verts.length; i++) {
            Vector3f v1 = verts[i];
            Vector3f v2 = verts[(i + 1) % verts.length]; // Loop back to start
            Vector3f n = normals[i];
            float len = lengths[i];

            gl.glNormal3f(n.x, n.y, n.z);
            if(useTexture) {
                // Define quad vertices CCW: bottom-left, bottom-right, top-right, top-left
                gl.glTexCoord2f(0, 0);                 gl.glVertex3f(v1.x, 0, v1.z);
                gl.glTexCoord2f(len * texScale, 0);    gl.glVertex3f(v2.x, 0, v2.z);
                gl.glTexCoord2f(len * texScale, h * texScale); gl.glVertex3f(v2.x, h, v2.z);
                gl.glTexCoord2f(0, h * texScale);      gl.glVertex3f(v1.x, h, v1.z);
            } else {
                gl.glVertex3f(v1.x, 0, v1.z); gl.glVertex3f(v2.x, 0, v2.z);
                gl.glVertex3f(v2.x, h, v2.z); gl.glVertex3f(v1.x, h, v1.z);
            }
        }
        gl.glEnd();
    }

    private void drawLShapeOutline(GL2 gl, Room room, float lineY) {
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
    }

    // --- T-Shaped Drawing ---
    private void drawTShapeFloor(GL2 gl, Room room) {
        float bW = room.getT_barWidth(); float bL = room.getT_barLength();
        float sW = room.getT_stemWidth(); float sL = room.getT_stemLength();
        float stemStartX = (bW - sW) / 2.0f;
        float stemEndX = stemStartX + sW;
        float totalLength = bL + sL;
        boolean useTexture = textureManager.isTextureLoaded(room.getFloorTexturePath());
        float texScale = 1.0f;

        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 1, 0);

        // Rectangle 1: Top Bar (0,0 to bW, bL)
        if(useTexture) { gl.glTexCoord2f(0*texScale, 0*texScale); gl.glVertex3f(0, 0, 0); gl.glTexCoord2f(bW*texScale, 0*texScale); gl.glVertex3f(bW, 0, 0); gl.glTexCoord2f(bW*texScale, bL*texScale); gl.glVertex3f(bW, 0, bL); gl.glTexCoord2f(0*texScale, bL*texScale); gl.glVertex3f(0, 0, bL); }
        else { gl.glVertex3f(0, 0, 0); gl.glVertex3f(bW, 0, 0); gl.glVertex3f(bW, 0, bL); gl.glVertex3f(0, 0, bL); }

        // Rectangle 2: Stem (stemStartX, bL to stemEndX, totalLength)
        if(useTexture) { gl.glTexCoord2f(stemStartX*texScale, bL*texScale); gl.glVertex3f(stemStartX, 0, bL); gl.glTexCoord2f(stemEndX*texScale, bL*texScale); gl.glVertex3f(stemEndX, 0, bL); gl.glTexCoord2f(stemEndX*texScale, totalLength*texScale); gl.glVertex3f(stemEndX, 0, totalLength); gl.glTexCoord2f(stemStartX*texScale, totalLength*texScale); gl.glVertex3f(stemStartX, 0, totalLength); }
        else { gl.glVertex3f(stemStartX, 0, bL); gl.glVertex3f(stemEndX, 0, bL); gl.glVertex3f(stemEndX, 0, totalLength); gl.glVertex3f(stemStartX, 0, totalLength); }

        gl.glEnd();
    }

    private void drawTShapeWalls(GL2 gl, Room room) {
        float bW = room.getT_barWidth(); float bL = room.getT_barLength();
        float sW = room.getT_stemWidth(); float sL = room.getT_stemLength();
        float h = room.getHeight();
        float stemStartX = (bW - sW) / 2.0f;
        float stemEndX = stemStartX + sW;
        float totalLength = bL + sL;
        boolean useTexture = textureManager.isTextureLoaded(room.getWallTexturePath());
        float texScale = 1.0f;

        // Define vertices CCW from top-left (0,0,0)
        Vector3f[] verts = {
                new Vector3f(0,          0, 0),          // 0 Top-Left Back
                new Vector3f(bW,         0, 0),          // 1 Top-Right Back
                new Vector3f(bW,         0, bL),         // 2 Top-Right Front of Bar
                new Vector3f(stemEndX,   0, bL),         // 3 Stem Top Right Shoulder
                new Vector3f(stemEndX,   0, totalLength),// 4 Stem Bottom Right
                new Vector3f(stemStartX, 0, totalLength),// 5 Stem Bottom Left
                new Vector3f(stemStartX, 0, bL),         // 6 Stem Top Left Shoulder
                new Vector3f(0,          0, bL)          // 7 Top-Left Front of Bar
        };
        // Define outward normals
        Vector3f[] normals = {
                new Vector3f( 0, 0,  1), // 0->1 Back Wall (+Z)
                new Vector3f(-1, 0,  0), // 1->2 Right Wall of Bar (-X)
                new Vector3f( 0, 0, -1), // 2->3 Top right shoulder Wall (-Z)
                new Vector3f(-1, 0,  0), // 3->4 Right wall of Stem (-X)
                new Vector3f( 0, 0, -1), // 4->5 Bottom wall of Stem (-Z)
                new Vector3f( 1, 0,  0), // 5->6 Left wall of Stem (+X)
                new Vector3f( 0, 0, -1), // 6->7 Top left shoulder Wall (-Z)
                new Vector3f( 1, 0,  0)  // 7->0 Left wall of Bar (+X)
        };
        // Define wall lengths for texture U coord
        float[] lengths = { bW, bL, bW - stemEndX, sL, sW, sL, stemStartX, bL };

        gl.glBegin(GL2.GL_QUADS);
        for (int i = 0; i < verts.length; i++) {
            Vector3f v1 = verts[i];
            Vector3f v2 = verts[(i + 1) % verts.length];
            Vector3f n = normals[i];
            float len = lengths[i];

            gl.glNormal3f(n.x, n.y, n.z);
            if(useTexture) {
                gl.glTexCoord2f(0, 0);                 gl.glVertex3f(v1.x, 0, v1.z);
                gl.glTexCoord2f(len * texScale, 0);    gl.glVertex3f(v2.x, 0, v2.z);
                gl.glTexCoord2f(len * texScale, h * texScale); gl.glVertex3f(v2.x, h, v2.z);
                gl.glTexCoord2f(0, h * texScale);      gl.glVertex3f(v1.x, h, v1.z);
            } else {
                gl.glVertex3f(v1.x, 0, v1.z); gl.glVertex3f(v2.x, 0, v2.z);
                gl.glVertex3f(v2.x, h, v2.z); gl.glVertex3f(v1.x, h, v1.z);
            }
        }
        gl.glEnd();
    }

    private void drawTShapeOutline(GL2 gl, Room room, float lineY) {
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
    }

}