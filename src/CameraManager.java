import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.GL2;

public class CameraManager {

    private float angleX = 30.0f;
    private float angleY = -45.0f;
    private float distance = 10.0f;
    private Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f); // Default target

    private boolean is3DMode = true;
    private float roomWidthForCalc = 5.0f;
    private float roomLengthForCalc = 5.0f;

    public CameraManager() {
        // Constructor no longer calls resetTargetToCenter directly
    }

    public void setRoomDimensions(float width, float length) {
        this.roomWidthForCalc = width;
        this.roomLengthForCalc = length;
    }

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
        this.target.y = 0.1f;  // Slightly above floor level
        this.target.z = center.z;

        // Calculate appropriate distance based on room shape and dimensions
        float extent = 5.0f;
        switch(room.getShape()) {
            case RECTANGULAR:
                extent = Math.max(room.getWidth(), room.getLength());
                break;
            case CIRCULAR:
                extent = room.getRadius() * 2.0f;
                break;
            case L_SHAPED:
                extent = Math.max(room.getL_outerWidth(), room.getL_outerLength());
                break;
            case T_SHAPED:
                extent = Math.max(room.getT_barWidth(), room.getT_barLength() + room.getT_stemLength());
                break;
        }

        // Use a minimum extent to prevent issues with very small rooms
        extent = Math.max(1.0f, extent);
        float roomDiagonal = (float) Math.sqrt(extent * extent + extent * extent);

        // Adjust distance based on current mode
        updateDistanceForMode(is3DMode, roomDiagonal);
    }

    public void setMode(boolean is3D) {
        boolean changed = this.is3DMode != is3D;
        this.is3DMode = is3D;

        if (changed) {
            // Reset angles and update distance based on current room size (if available)
            float currentDiagonal = (float) Math.sqrt(roomWidthForCalc * roomWidthForCalc + roomLengthForCalc * roomLengthForCalc);

            if (is3D) {
                angleX = 30.0f;
                angleY = -45.0f;
                target.y = 0.1f; // Slightly above floor for 3D target
            } else { // 2D
                angleX = 90.0f;
                angleY = 0.0f;
                target.y = 0.0f; // Target directly on floor plane for 2D ortho
            }

            updateDistanceForMode(is3D, currentDiagonal);
        }
    }

    private void updateDistanceForMode(boolean is3D, float roomDiagonal) {
        if (is3D) {
            // For 3D, use a distance that ensures the room fits well in view
            distance = Math.max(10.0f, roomDiagonal * 1.2f);
        } else {
            // For 2D, use the maximum dimension for ortho distance calculation
            float maxDim = Math.max(roomWidthForCalc, roomLengthForCalc);
            distance = Math.max(5.0f, maxDim * 1.1f);
        }

        // Ensure a global minimum distance
        distance = Math.max(1.0f, distance);
    }

    public void rotate(float deltaX, float deltaY) {
        if (!is3DMode) return;

        // Update angles based on mouse movement
        angleY += deltaX * 0.5f;
        angleX += deltaY * 0.5f;

        // Clamp vertical angle to prevent gimbal lock and flipping
        angleX = Math.max(-89.0f, Math.min(89.0f, angleX));
    }

    public void zoom(float delta) {
        // Adjust zoom sensitivity based on view mode
        float zoomFactor = is3DMode ? 0.5f : distance * 0.1f;
        distance += delta * zoomFactor;

        // Enforce minimum distance
        distance = Math.max(1.0f, distance);
    }

    public void pan(float deltaX, float deltaY, int viewWidth, int viewHeight) {
        if (is3DMode) {
            // 3D panning - calculate camera-relative vectors
            float sensitivity = 0.005f * distance;

            // Convert camera angles to radians
            float camXRad = (float)Math.toRadians(angleX);
            float camYRad = (float)Math.toRadians(angleY);

            // Calculate right vector (perpendicular to camera direction in XZ plane)
            Vector3f right = new Vector3f((float)Math.cos(camYRad), 0, -(float)Math.sin(camYRad));
            right.normalize();

            // Calculate up vector based on right and view direction
            // This ensures a stable up vector even at extreme angles
            float sinPitch = (float)Math.sin(camXRad);
            float cosPitch = (float)Math.cos(camXRad);
            Vector3f up = new Vector3f(
                    sinPitch * (float)Math.sin(camYRad),
                    cosPitch,
                    sinPitch * (float)Math.cos(camYRad)
            );
            up.normalize();

            // Apply panning deltas to target position
            target.x -= (deltaX * right.x + deltaY * up.x) * sensitivity;
            target.y -= (deltaY * up.y) * sensitivity;
            target.z -= (deltaX * right.z + deltaY * up.z) * sensitivity;

        } else {
            // 2D orthographic panning
            float orthoHeightView = distance;
            float aspect = (float)viewWidth / Math.max(1, viewHeight);
            float orthoWidthView = orthoHeightView * aspect;

            // Convert screen deltas to world units
            float worldDeltaX = deltaX * (orthoWidthView / Math.max(1, viewWidth));
            float worldDeltaZ = deltaY * (orthoHeightView / Math.max(1, viewHeight));

            // Apply pan with direction adjustments for intuitive movement
            target.x -= worldDeltaX;
            target.z += worldDeltaZ; // Add because screen Y down is world Z positive
        }
    }

    public void applyProjection(GL2 gl, GLU glu, float aspect) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        if (is3DMode) {
            // Use perspective projection for 3D
            glu.gluPerspective(45.0f, aspect, 0.1f, 200.0f);
        } else {
            // Use orthographic projection for 2D
            float orthoHeightView = distance;
            float orthoWidthView = orthoHeightView * aspect;

            gl.glOrtho(
                    target.x - orthoWidthView / 2.0f, target.x + orthoWidthView / 2.0f,
                    target.z - orthoHeightView / 2.0f, target.z + orthoHeightView / 2.0f,
                    -100.0, 100.0
            );
        }

        gl.glMatrixMode(GL2.GL_MODELVIEW); // Switch back to modelview matrix
    }

    public void applyLookAt(GL2 gl, GLU glu) {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        if (is3DMode) {
            // Convert angles to radians
            float camXRad = (float) Math.toRadians(angleX);
            float camYRad = (float) Math.toRadians(angleY);

            // Calculate eye position relative to target
            float eyeX = target.x + distance * (float)(Math.cos(camXRad) * Math.sin(camYRad));
            float eyeY = target.y + distance * (float)(Math.sin(camXRad));
            float eyeZ = target.z + distance * (float)(Math.cos(camXRad) * Math.cos(camYRad));

            // Calculate up vector with special handling for extreme angles
            float upX = 0, upY = 1, upZ = 0; // Default up is +Y

            // If we're looking straight up or down, adjust the up vector
            if (Math.abs(angleX) > 85.0f) {
                // Use a different up vector when looking near vertical
                float sign = Math.signum(angleX);
                upY = 0; // No Y component for up when looking straight up/down
                upX = -sign * (float)Math.sin(camYRad);
                upZ = -sign * (float)Math.cos(camYRad);
            }

            // Apply the camera transformation
            glu.gluLookAt(
                    eyeX, eyeY, eyeZ,          // Eye position
                    target.x, target.y, target.z, // Target position
                    upX, upY, upZ               // Up vector
            );

        } else {
            // 2D orthographic view (top-down)
            glu.gluLookAt(
                    target.x, distance, target.z, // Eye above target
                    target.x, 0, target.z,       // Looking straight down
                    0, 0, -1                    // Up is along -Z (forward in screen)
            );
        }
    }

    // Getters
    public float getDistance() { return distance; }
    public Vector3f getTarget() { return target; }
    public boolean is3DMode() { return is3DMode; }
}