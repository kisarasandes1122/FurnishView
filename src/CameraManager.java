import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GL2;

public class CameraManager {

    private float angleX = 30.0f;
    private float angleY = -45.0f;
    private float distance = 10.0f;
    private Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f); // Default target

    private boolean is3DMode = true;
    // Store room dimensions if needed for calculations other than centering
    private float roomWidthForCalc = 5.0f;
    private float roomLengthForCalc = 5.0f;


    public CameraManager() {
        // DO NOT call resetTargetToCenter here. It will be called later
        // when the Room object is available.
        // resetTargetToCenter(); // REMOVE THIS
    }

    // Keep this method if other calculations need dimensions
    public void setRoomDimensions(float width, float length) {
        this.roomWidthForCalc = width;
        this.roomLengthForCalc = length;
    }

    // Modified resetTargetToCenter - requires Room object
    public void resetTargetToCenter(Room room) {
        if (room == null) {
            this.target.x = 2.5f;
            this.target.y = 0.1f;
            this.target.z = 2.5f;
            this.distance = 10.0f; // Reset distance too
            System.err.println("CameraManager: resetTargetToCenter called with null Room. Using defaults.");
            return;
        }
        Vector3f center = room.calculateCenter();
        this.target.x = center.x;
        this.target.y = 0.1f;
        this.target.z = center.z;

        // Adjust default distance based on overall size (using Room methods)
        float extent = 5.0f;
        switch(room.getShape()){
            case RECTANGULAR: extent = Math.max(room.getWidth(), room.getLength()); break;
            case CIRCULAR: extent = room.getRadius() * 2.0f; break;
            case L_SHAPED: extent = Math.max(room.getL_outerWidth(), room.getL_outerLength()); break;
            case T_SHAPED: extent = Math.max(room.getT_barWidth(), room.getT_barLength() + room.getT_stemLength()); break;
        }
        // Use a minimum extent to prevent issues with very small rooms
        extent = Math.max(1.0f, extent);
        float roomDiagonal = (float) Math.sqrt(extent * extent + extent * extent);
        // Adjust distance based on mode AFTER resetting target
        updateDistanceForMode(is3DMode, roomDiagonal); // Call helper
    }


    public void setMode(boolean is3D) {
        boolean changed = this.is3DMode != is3D;
        this.is3DMode = is3D;

        if (changed) {
            // DO NOT call resetTargetToCenter here. It will be called by
            // the calling code (e.g., MainAppFrame or DesignRenderer) which has the Room object.
            // resetTargetToCenter(); // REMOVE THIS

            // Reset angles and update distance based on current room size (if available)
            float currentDiagonal = (float) Math.sqrt(roomWidthForCalc * roomWidthForCalc + roomLengthForCalc * roomLengthForCalc);
            if (is3D) {
                angleX = 30.0f;
                angleY = -45.0f;
                target.y = 0.1f; // Slightly above floor for 3D target
                updateDistanceForMode(true, currentDiagonal);
            } else { // 2D
                angleX = 90.0f;
                angleY = 0.0f;
                target.y = 0.0f; // Target directly on floor plane for 2D ortho
                updateDistanceForMode(false, currentDiagonal);
            }
        }
    }

    // Helper to consolidate distance logic
    private void updateDistanceForMode(boolean is3D, float roomDiagonal) {
        if (is3D) {
            distance = Math.max(10.0f, roomDiagonal * 1.2f);
        } else { // 2D
            // Use max dimension for ortho distance base
            float maxDim = Math.max(roomWidthForCalc, roomLengthForCalc);
            distance = Math.max(5.0f, maxDim * 1.1f); // Ensure a minimum ortho size
        }
        distance = Math.max(1.0f, distance); // Global minimum distance
    }


    public void rotate(float deltaX, float deltaY) {
        // ... (no changes needed)
        if (!is3DMode) return;
        angleY += deltaX * 0.5f;
        angleX += deltaY * 0.5f;
        angleX = Math.max(-89.9f, Math.min(89.9f, angleX));
    }

    public void zoom(float delta) {
        // ... (no changes needed)
        float zoomFactor = is3DMode ? 0.5f : distance * 0.1f; // Ortho zoom is relative
        distance += delta * zoomFactor;
        distance = Math.max(1.0f, distance);
    }

    public void pan(float deltaX, float deltaY, int viewWidth, int viewHeight) {
        // ... (no changes needed)
        float sensitivity;
        if (is3DMode) {
            sensitivity = 0.005f * distance;

            float camXRad = (float)Math.toRadians(angleX);
            float camYRad = (float)Math.toRadians(angleY);
            float cosPitch = (float)Math.cos(camXRad);
            float sinPitch = (float)Math.sin(camXRad);
            float cosYaw = (float)Math.cos(camYRad);
            float sinYaw = (float)Math.sin(camYRad);

            // Forward vector (towards target from eye)
            float forwardX = -cosPitch * sinYaw;
            float forwardY = -sinPitch;
            float forwardZ = -cosPitch * cosYaw;

            // Recalculate right and up vectors based on angles
            // Right vector (cross product of view direction and world up) - simplified
            Vector3f worldUp = new Vector3f(0, 1, 0);
            Vector3f viewDir = new Vector3f(forwardX, forwardY, forwardZ); // Actually points eye -> target

            // Need direction from eye TO target for panning calc? Let's test.
            // No, use the camera's orientation vectors directly.

            // Right vector = cross(Front, WorldUp) - careful with direction
            // Front is direction camera is looking AT.
            // If eye is target + dist * offset, then direction is -offset
            // Let's use the standard cross product method based on Yaw/Pitch

            Vector3f right = new Vector3f((float)Math.cos(camYRad), 0, -(float)Math.sin(camYRad));
            right.normalize(); // Ensure unit vector

            // Up vector = cross(Right, Forward) - Front = -Forward
            // Need the actual camera's up vector derived from angles
            Vector3f up = new Vector3f( sinPitch * sinYaw, cosPitch, sinPitch * cosYaw);
            up.normalize();

            // Adjust target based on panning delta projected onto camera's right and up
            target.x -= (deltaX * right.x) * sensitivity;
            target.z -= (deltaX * right.z) * sensitivity;
            target.x -= (deltaY * up.x) * sensitivity; // Subtract because screen Y is inverted
            target.y -= (deltaY * up.y) * sensitivity;
            target.z -= (deltaY * up.z) * sensitivity;


        } else { // 2D Ortho Panning
            // Calculate how much the view covers in world units
            // Distance is effectively the ortho height scale here
            float orthoHeightView = distance;
            float aspect = (float)viewWidth / Math.max(1, viewHeight);
            float orthoWidthView = orthoHeightView * aspect;

            // Convert screen pixel delta to world unit delta
            float worldDeltaX = deltaX * (orthoWidthView / Math.max(1, viewWidth));
            float worldDeltaZ = deltaY * (orthoHeightView / Math.max(1, viewHeight)); // Screen Y up is World Z negative (usually)

            // Adjust target. In ortho top-down, moving mouse right (positive dx)
            // should move the target left (negative target.x change) to make the world
            // appear to move right under the mouse. Moving mouse down (positive dy)
            // should move target up (positive target.z change).
            target.x -= worldDeltaX;
            target.z += worldDeltaZ; // Add because screen Y down is world Z positive
        }
    }


    public void applyProjection(GL2 gl, GLU glu, float aspect) {
        // ... (no changes needed)
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        if (is3DMode) {
            glu.gluPerspective(45.0f, aspect, 0.1f, 200.0f); // Increased far plane
        } else {
            // Ortho view: distance controls the zoom level (view height)
            float orthoHeightView = distance; // Use distance as height scale
            float orthoWidthView = orthoHeightView * aspect;
            // Center the view on the target coordinates
            gl.glOrtho(target.x - orthoWidthView / 2.0f, target.x + orthoWidthView / 2.0f,
                    target.z - orthoHeightView / 2.0f, target.z + orthoHeightView / 2.0f, // Bottom, Top corresponds to -Z/+Z in top-down
                    -100.0, 100.0); // Near, Far planes for ortho
        }
        gl.glMatrixMode(GL2.GL_MODELVIEW); // Switch back
    }

    public void applyLookAt(GL2 gl, GLU glu) {
        // ... (logic for calculating eye position based on angles/distance/target is fine)
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        float eyeX, eyeY, eyeZ;
        float upX, upY, upZ;

        if (is3DMode) {
            float camXRad = (float) Math.toRadians(angleX);
            float camYRad = (float) Math.toRadians(angleY);

            // Calculate eye position relative to target
            eyeX = target.x + distance * (float)(Math.cos(camXRad) * Math.sin(camYRad));
            eyeY = target.y + distance * (float)(Math.sin(camXRad));
            eyeZ = target.z + distance * (float)(Math.cos(camXRad) * Math.cos(camYRad));

            // Determine the Up vector
            // Standard up is (0, 1, 0) unless looking nearly straight up/down
            if (Math.abs(angleX) > 89.5f) {
                // Looking straight down/up, Up vector needs to flip based on Yaw
                // Let Up be aligned with the direction the camera *would* be facing horizontally
                upX = (float) Math.sin(camYRad); // Should be 0 if aligned with Z
                upY = 0;
                upZ = (float) Math.cos(camYRad); // Should be -1 or 1 if aligned with Z
                // Make sure the sign points "forward" relative to camera roll (which we don't have)
                // A simpler approximation for near-vertical: Use Z axis slightly tilted by yaw
                // If looking straight down (angleX ~ -90), up should be towards +Z based on yaw=0
                // If angleX is positive (looking up), up should be Z based on yaw=180?
                // Let's stick to world up unless near pole.
                // The previous logic was actually better for pole handling:
                float upSign = Math.signum(angleX); // Use angle sign
                upX = 0; // Assume no roll - this might be the issue near poles. Let's revert slightly.
                // upX = upSign * (float)Math.sin(camXRad) * (float)Math.sin(camYRad); // Original 'up' derived up vector
                // upY = upSign * (float)Math.cos(camXRad);
                // upZ = upSign * (float)Math.sin(camXRad) * (float)Math.cos(camYRad);
                // If angleX is near 90, cos(camXRad) is near 0. sin(camXRad) is near 1.
                // Let's try standard up unless EXTREMELY close to pole.
                upX = 0;
                upY = 1;
                upZ = 0;

            } else {
                upX = 0;
                upY = 1;
                upZ = 0;
            }
        } else { // 2D Ortho Top-Down
            eyeX = target.x;
            eyeY = distance; // Eye is directly above target at 'distance' height
            eyeZ = target.z;
            upX = 0;
            upY = 0;  // For top-down view, "up" direction on screen...
            upZ = -1; // ...corresponds to negative Z axis in world space (forward)
        }

        glu.gluLookAt(eyeX, eyeY, eyeZ,      // Eye position
                target.x, target.y, target.z, // Target position
                upX, upY, upZ);               // Up vector
    }

    public float getDistance() { return distance; }
    public Vector3f getTarget() { return target; }
    public boolean is3DMode() { return is3DMode; }



}
