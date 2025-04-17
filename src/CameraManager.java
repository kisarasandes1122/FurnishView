import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GL2;

public class CameraManager {

    // Camera state
    private float angleX = 30.0f;   // Pitch (degrees)
    private float angleY = -45.0f;  // Yaw (degrees)
    private float distance = 10.0f; // Distance from the target
    private Vector3f target = new Vector3f(2.5f, 0.1f, 2.5f); // Initial target guess (center of default 5x5 room)

    // View mode
    private boolean is3DMode = true;

    // Store room dimensions for calculations (like default distance)
    private float roomWidthForCalc = 5.0f;
    private float roomLengthForCalc = 5.0f;
    private float roomRadiusForCalc = 0.0f; // For circular rooms
    // Add fields for L/T shapes if needed for distance calc later

    public CameraManager() {
        // Constructor - initial state is set above
    }

    // Method to update internal dimensions based on the Room object
    // Called by DesignRenderer or MainAppFrame when the room changes
    public void updateRoomDimensions(Room room) {
        if (room == null) return;

        switch (room.getShape()) {
            case RECTANGULAR:
                this.roomWidthForCalc = room.getWidth();
                this.roomLengthForCalc = room.getLength();
                this.roomRadiusForCalc = 0;
                break;
            case CIRCULAR:
                this.roomRadiusForCalc = room.getRadius();
                // Use diameter for general size estimation
                this.roomWidthForCalc = room.getRadius() * 2.0f;
                this.roomLengthForCalc = room.getRadius() * 2.0f;
                break;
            case L_SHAPED:
                // Use outer dimensions for general size
                this.roomWidthForCalc = room.getL_outerWidth();
                this.roomLengthForCalc = room.getL_outerLength();
                this.roomRadiusForCalc = 0;
                break;
            case T_SHAPED:
                // Use bar width and total length for general size
                this.roomWidthForCalc = room.getT_barWidth();
                this.roomLengthForCalc = room.getT_barLength() + room.getT_stemLength();
                this.roomRadiusForCalc = 0;
                break;
            default:
                this.roomWidthForCalc = 5.0f; // Fallback
                this.roomLengthForCalc = 5.0f;
                this.roomRadiusForCalc = 0;
                break;
        }
        // Update distance based on new dimensions and current mode
        updateDistanceForMode(this.is3DMode);
    }


    // Reset the camera target to the center of the given room
    public void resetTargetToCenter(Room room) {
        if (room != null) {
            Vector3f center = room.calculateCenter();
            this.target = center; // Use the calculated center
            // Adjust Y slightly depending on mode
            this.target.y = is3DMode ? 0.1f : 0.0f;
            // Update internal dimensions used for distance calculation
            updateRoomDimensions(room); // This will also call updateDistanceForMode
        } else {
            // Fallback if room is null
            this.target = new Vector3f(2.5f, 0.1f, 2.5f);
            this.roomWidthForCalc = 5.0f;
            this.roomLengthForCalc = 5.0f;
            this.roomRadiusForCalc = 0;
            updateDistanceForMode(this.is3DMode); // Update distance with defaults
            System.err.println("CameraManager: resetTargetToCenter called with null Room. Using defaults.");
        }
    }

    // Set the view mode (3D perspective or 2D orthographic)
    public void setMode(boolean is3D) {
        boolean changed = this.is3DMode != is3D;
        this.is3DMode = is3D;

        if (changed) {
            // Reset angles and update distance/target Y based on mode
            if (is3D) {
                angleX = 30.0f;
                angleY = -45.0f;
                target.y = 0.1f; // Slightly above floor for 3D target
            } else { // 2D
                angleX = 90.0f; // Look straight down
                angleY = 0.0f;  // Align with Z axis
                target.y = 0.0f; // Target directly on floor plane for 2D ortho
            }
            updateDistanceForMode(is3D); // Recalculate appropriate distance
        }
    }

    // Helper to calculate a suitable distance based on room size and mode
    private void updateDistanceForMode(boolean is3D) {
        // Estimate room extent based on available dimensions
        float extent = Math.max(1.0f, Math.max(roomWidthForCalc, roomLengthForCalc));
        if (roomRadiusForCalc > 0) {
            extent = Math.max(extent, roomRadiusForCalc * 2.0f);
        }
        float roomDiagonal = (float) Math.sqrt(extent * extent + extent * extent);

        if (is3D) {
            // In 3D, distance is based on diagonal for a good overview
            distance = Math.max(10.0f, roomDiagonal * 1.2f);
        } else { // 2D
            // In 2D ortho, distance effectively controls the ortho view scale (height/width)
            // Base it on the maximum dimension to ensure the whole room fits initially
            distance = Math.max(5.0f, extent * 1.1f); // Ensure a minimum ortho size
        }
        distance = Math.max(1.0f, distance); // Global minimum distance
    }


    // Rotate the camera based on mouse delta
    public void rotate(float deltaX, float deltaY) {
        if (!is3DMode) return; // No rotation in 2D top-down view

        // Sensitivity factor for rotation
        float sensitivity = 0.5f;
        angleY += deltaX * sensitivity; // Yaw
        angleX += deltaY * sensitivity; // Pitch

        // Clamp pitch to avoid flipping upside down
        angleX = Math.max(-89.9f, Math.min(89.9f, angleX));

        // Keep yaw within 0-360 or -180 to 180 if desired (optional)
        // angleY %= 360.0f;
    }

    // Zoom the camera based on mouse wheel or drag delta
    public void zoom(float delta) {
        // Sensitivity factor - adjust as needed
        // For ortho, zoom should be relative to current distance/scale
        float zoomFactor = is3DMode ? 0.5f : distance * 0.1f;
        distance -= delta * zoomFactor; // Subtract because delta > 0 usually means zoom in (decrease distance)

        // Clamp distance to prevent going too close or too far
        distance = Math.max(1.0f, distance); // Minimum distance
        // distance = Math.min(100.0f, distance); // Optional maximum distance
    }

    // Pan the camera target based on mouse delta
    public void pan(float deltaX, float deltaY, int viewWidth, int viewHeight) {
        float sensitivity;

        if (is3DMode) {
            // Panning sensitivity scales with distance in perspective view
            sensitivity = 0.005f * distance;

            // Calculate camera's local right and up vectors based on current angles
            float camXRad = (float)Math.toRadians(angleX);
            float camYRad = (float)Math.toRadians(angleY);
            float cosPitch = (float)Math.cos(camXRad);
            float sinPitch = (float)Math.sin(camXRad);
            float cosYaw = (float)Math.cos(camYRad);
            float sinYaw = (float)Math.sin(camYRad);

            // Calculate Right vector (simplified, assumes world up is (0,1,0))
            Vector3f right = new Vector3f((float)Math.cos(camYRad), 0, -(float)Math.sin(camYRad));
            right.normalize(); // Ensure unit vector

            // Calculate Up vector (derived from cross product or angles)
            // Using angles: Careful calculation needed near poles.
            // Simpler approach: Assume world up unless near pole, or use cross product reliably.
            // Let's try the angle-derived Up vector:
            Vector3f up = new Vector3f( sinPitch * sinYaw, cosPitch, sinPitch * cosYaw);
            up.normalize();

            // Adjust target based on panning delta projected onto camera's right and up
            // Moving mouse right (positive deltaX) should move target left relative to camera's right vector
            target.x -= (deltaX * right.x) * sensitivity;
            target.z -= (deltaX * right.z) * sensitivity; // Apply Z component of right vector

            // Moving mouse down (positive deltaY) should move target down relative to camera's up vector
            // (Screen Y is inverted, so positive deltaY is down)
            target.x -= (deltaY * up.x) * sensitivity;
            target.y -= (deltaY * up.y) * sensitivity;
            target.z -= (deltaY * up.z) * sensitivity;

        } else { // 2D Ortho Panning
            // Calculate how much the view covers in world units
            // Distance is effectively related to the ortho view height/width scale
            float orthoHeightView = distance; // Or however ortho scale is set up
            float aspect = (float)viewWidth / Math.max(1, viewHeight);
            float orthoWidthView = orthoHeightView * aspect;

            // Convert screen pixel delta to world unit delta
            float worldDeltaX = deltaX * (orthoWidthView / Math.max(1, viewWidth));
            // Screen Y down is world Z positive in typical top-down view
            float worldDeltaZ = deltaY * (orthoHeightView / Math.max(1, viewHeight));

            // Adjust target. Moving mouse right (positive dx) should move the target left.
            target.x -= worldDeltaX;
            // Moving mouse down (positive dy) should move target "up" (positive Z).
            target.z += worldDeltaZ;
        }
    }


    // Apply projection matrix (Perspective or Orthographic)
    public void applyProjection(GL2 gl, GLU glu, float aspect) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        if (is3DMode) {
            // Perspective view
            glu.gluPerspective(45.0f, aspect, 0.1f, 200.0f); // Field of view, aspect, near, far
        } else {
            // Orthographic view (top-down)
            // Distance controls the zoom level (how much area is visible)
            float orthoHeightView = distance; // Use distance as height scale
            float orthoWidthView = orthoHeightView * aspect;
            // Center the view on the target coordinates in the XZ plane
            gl.glOrtho(target.x - orthoWidthView / 2.0f, target.x + orthoWidthView / 2.0f, // left, right
                    target.z - orthoHeightView / 2.0f, target.z + orthoHeightView / 2.0f, // bottom, top (corresponds to -Z/+Z)
                    -100.0, 100.0);                     // near, far clipping planes for ortho
        }
        gl.glMatrixMode(GL2.GL_MODELVIEW); // Switch back for LookAt
    }

    // Apply modelview matrix (LookAt)
    public void applyLookAt(GL2 gl, GLU glu) {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        float eyeX, eyeY, eyeZ;
        float upX, upY, upZ;

        if (is3DMode) {
            // Calculate eye position based on target, distance, and angles
            float camXRad = (float) Math.toRadians(angleX);
            float camYRad = (float) Math.toRadians(angleY);

            eyeX = target.x + distance * (float)(Math.cos(camXRad) * Math.sin(camYRad));
            eyeY = target.y + distance * (float)(Math.sin(camXRad));
            eyeZ = target.z + distance * (float)(Math.cos(camXRad) * Math.cos(camYRad));

            // Determine the Up vector - typically (0, 1, 0)
            // Handle looking nearly straight up or down (gimbal lock approximation)
            if (Math.abs(angleX) > 89.5f) {
                // If looking straight down (angleX near -90), Up should point along positive Z axis for angleY=0
                // If looking straight up (angleX near +90), Up should point along negative Z axis for angleY=0
                // General case: Use the direction the camera would face horizontally as Up vector
                upX = 0; // Simplified: avoid complex roll calculation for now
                upY = 0;
                upZ = (angleX > 0) ? -1.0f : 1.0f; // Simplistic flip based on pitch pole
                // A more robust approach involves calculating the camera's right vector and crossing it
                // with the forward vector, but (0,1,0) works for most cases.
                // Let's stick to the standard (0,1,0) unless very close to pole, where gluLookAt handles it reasonably.
                upX = 0; upY = 1; upZ = 0;
            } else {
                upX = 0;
                upY = 1; // Standard world up vector
                upZ = 0;
            }
        } else { // 2D Ortho Top-Down
            // Eye is directly above the target
            eyeX = target.x;
            eyeY = distance; // Height above the floor plane (using distance as height)
            eyeZ = target.z;

            // Up vector for top-down view:
            // Which direction in world space should correspond to "up" on the screen?
            // Usually, we want positive Z in world to be "up" on screen.
            upX = 0;
            upY = 0;
            upZ = 1; // Points towards positive Z (which is often 'up' on a 2D map)
            // If screen Y should map to negative Z, use -1. Let's assume Z+ is screen Y+.
        }

        // Apply the look-at transformation
        glu.gluLookAt(eyeX, eyeY, eyeZ,           // Eye position
                target.x, target.y, target.z, // Target position
                upX, upY, upZ);              // Up vector direction
    }

    // --- Getters ---
    public float getDistance() { return distance; }
    public Vector3f getTarget() { return target; }
    public boolean is3DMode() { return is3DMode; }
    public float getAngleX() { return angleX; }
    public float getAngleY() { return angleY; }

}