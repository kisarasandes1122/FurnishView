import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU; // GLU is needed for unProject
import java.util.List;

// Assume Vector3f, MatrixUtil, Furniture, DesignModel classes are available

public class PickingHelper {

    private final GLU glu = new GLU(); // GLU instance for unProject

    public PickingHelper() {}

    /**
     * Unprojects 2D screen coordinates (like mouse position) into 3D world coordinates
     * on a specified horizontal plane (Y = planeY).
     *
     * @param screenX The x-coordinate on the screen (e.g., mouse X).
     * @param screenY The y-coordinate on the screen (e.g., mouse Y).
     * @param planeY The Y value of the horizontal plane in world coordinates to project onto.
     * @param modelviewMatrix The current ModelView matrix (double[16]).
     * @param projectionMatrix The current Projection matrix (double[16]).
     * @param viewport The current viewport (int[4]: x, y, width, height).
     * @return A Vector3f representing the intersection point in world coordinates, or null if error/no intersection.
     */
    public Vector3f screenToWorldOnPlane(int screenX, int screenY, float planeY,
                                         double[] modelviewMatrix, double[] projectionMatrix, int[] viewport)
    {
        // Viewport coordinates need to be adjusted for OpenGL's origin (bottom-left)
        int V_x = viewport[0];
        int V_y = viewport[1]; // Often 0
        int V_w = viewport[2];
        int V_h = viewport[3];

        // Check for invalid viewport
        if (V_w <= 0 || V_h <= 0) {
            System.err.println("ScreenToWorld: Invalid viewport dimensions.");
            return null;
        }

        // Convert screen Y to OpenGL's coordinate system (origin at bottom)
        float glScreenY = (float) (V_h - screenY); // Invert Y coordinate

        // Unproject two points: one on the near plane, one on the far plane
        double[] nearWorldCoords = new double[4]; // Stores {x, y, z, w}
        double[] farWorldCoords = new double[4];  // Stores {x, y, z, w}

        // Use gluUnProject to get world coordinates corresponding to the screen point
        // at the near (z=0) and far (z=1) clipping planes.
        boolean nearOK = glu.gluUnProject(
                (double) screenX, (double) glScreenY, 0.0, // screen x, y, z (near plane)
                modelviewMatrix, 0,
                projectionMatrix, 0,
                viewport, 0,
                nearWorldCoords, 0);

        boolean farOK = glu.gluUnProject(
                (double) screenX, (double) glScreenY, 1.0, // screen x, y, z (far plane)
                modelviewMatrix, 0,
                projectionMatrix, 0,
                viewport, 0,
                farWorldCoords, 0);

        if (!nearOK || !farOK) {
            System.err.println("ScreenToWorld: gluUnProject failed.");
            return null;
        }

        // Perspective divide (divide by w) to get Cartesian coordinates
        Vector3f worldNear = new Vector3f(
                (float)(nearWorldCoords[0] / nearWorldCoords[3]),
                (float)(nearWorldCoords[1] / nearWorldCoords[3]),
                (float)(nearWorldCoords[2] / nearWorldCoords[3])
        );
        Vector3f worldFar = new Vector3f(
                (float)(farWorldCoords[0] / farWorldCoords[3]),
                (float)(farWorldCoords[1] / farWorldCoords[3]),
                (float)(farWorldCoords[2] / farWorldCoords[3])
        );

        // Create the ray direction vector
        Vector3f rayDir = worldFar.subtract(worldNear); // Use Vector3f subtract method
        rayDir.normalize(); // Normalize the direction vector

        // Now, find the intersection of this ray with the horizontal plane Y = planeY
        // Ray equation: P = worldNear + t * rayDir
        // Plane equation: P.y = planeY
        // Substitute: worldNear.y + t * rayDir.y = planeY
        // Solve for t: t = (planeY - worldNear.y) / rayDir.y

        // Check if the ray is parallel to the plane (or very close)
        if (Math.abs(rayDir.y) < 1e-7) {
            // Ray is parallel, no intersection (or infinite intersections if ray is *in* the plane)
            return null;
        }

        // Calculate intersection parameter t
        float t = (planeY - worldNear.y) / rayDir.y;

        // Calculate the intersection point P
        float intersectX = worldNear.x + t * rayDir.x;
        float intersectY = worldNear.y + t * rayDir.y; // Should be == planeY
        float intersectZ = worldNear.z + t * rayDir.z;

        return new Vector3f(intersectX, intersectY, intersectZ);
    }


    /**
     * Performs picking to find the furniture item under the given screen coordinates.
     * Uses ray casting and checks against furniture bounding boxes on the floor plane.
     *
     * @param screenX The x-coordinate on the screen.
     * @param screenY The y-coordinate on the screen.
     * @param model The DesignModel containing the furniture list.
     * @param modelviewMatrix The current ModelView matrix.
     * @param projectionMatrix The current Projection matrix.
     * @param viewport The current viewport.
     * @return The picked Furniture object, or null if no furniture is hit.
     */
    public Furniture pickFurniture(int screenX, int screenY, DesignModel model,
                                   double[] modelviewMatrix, double[] projectionMatrix, int[] viewport)
    {
        if (model == null || model.getFurnitureList() == null) {
            return null;
        }

        // 1. Find where the click intersects the floor plane (Y=0)
        Vector3f floorPos = screenToWorldOnPlane(screenX, screenY, 0.0f, modelviewMatrix, projectionMatrix, viewport);
        if (floorPos == null) {
            // Ray might be parallel to floor or an error occurred
            // System.err.println("pickFurniture: Could not get floor intersection point.");
            return null;
        }

        // 2. Iterate through furniture and check for intersection
        Furniture closestMatch = null;
        float minDistanceSq = Float.MAX_VALUE;

        for (Furniture f : model.getFurnitureList()) {
            Vector3f fPos = f.getPosition(); // Furniture's base center position
            float halfW = f.getWidth() / 2.0f;
            float halfD = f.getDepth() / 2.0f;

            // Optimization: Quick distance check (optional, but can help in large scenes)
            // float dx_center = fPos.x - floorPos.x;
            // float dz_center = fPos.z - floorPos.z;
            // float max_extent_sq = (halfW*halfW + halfD*halfD) * 1.1f; // Slightly larger radius check
            // if (dx_center * dx_center + dz_center * dz_center > max_extent_sq) {
            //    continue; // Skip if click is too far from the furniture center
            // }

            // 3. Check if the floor intersection point lies within the furniture's
            //    rotated bounding box on the XZ plane.

            // Calculate the click position relative to the furniture's origin
            Vector3f clickRelativeToFurn = new Vector3f(floorPos.x - fPos.x, 0, floorPos.z - fPos.z);

            // Rotate the relative click point by the *inverse* of the furniture's Y rotation
            // to align the click point with the furniture's local axes.
            float rotYRad = (float) Math.toRadians(-f.getRotation().y); // Negative angle for inverse rotation
            clickRelativeToFurn.rotateY(rotYRad); // Assumes rotateY exists in Vector3f

            // 4. Perform Axis-Aligned Bounding Box (AABB) check in furniture's local space
            boolean withinBounds = (clickRelativeToFurn.x >= -halfW && clickRelativeToFurn.x <= halfW &&
                    clickRelativeToFurn.z >= -halfD && clickRelativeToFurn.z <= halfD);

            if (withinBounds) {
                // Point is within the footprint. If multiple furniture overlap,
                // pick the one whose center is closest to the click point.
                float dx = fPos.x - floorPos.x;
                float dz = fPos.z - floorPos.z;
                float distSq = dx * dx + dz * dz; // Distance squared from click to furniture center

                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestMatch = f;
                }
            }
        }
        return closestMatch;
    }
}