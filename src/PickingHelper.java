import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import java.util.List;

public class PickingHelper {

    // This class might not need state if matrices/viewport are passed in each time.
    // Or, DesignRenderer could update cached matrices here. Let's pass them in.

    public PickingHelper() {}

    /** Unprojects screen coordinates onto a world-space plane (Y = planeY). */
    public Vector3f screenToWorldOnPlane(int screenX, int screenY, float planeY,
                                         double[] modelviewMatrix, double[] projectionMatrix, int[] viewport, boolean is3D)
    {
        int V_x = viewport[0];
        int V_y = viewport[1];
        int V_w = viewport[2];
        int V_h = viewport[3];
        if (V_w <= 0 || V_h <= 0) {
            System.err.println("ScreenToWorld: Invalid viewport dimensions.");
            return null;
        }

        float ndcX = (float)(screenX - V_x) / V_w * 2.0f - 1.0f;
        float ndcY = (float)(V_h - screenY - V_y) / V_h * 2.0f - 1.0f; // Invert Y
        float ndcZ_near = -1.0f;
        float ndcZ_far = 1.0f;

        double[] P = projectionMatrix;
        double[] MV = modelviewMatrix;
        double[] MVP = MatrixUtil.multiply(P, MV);
        if (MVP == null) return null;
        double[] invMVP = MatrixUtil.invert(MVP);
        if (invMVP == null) {
            System.err.println("ScreenToWorld: Matrix inversion failed.");
            // MatrixUtil.printMatrix(MVP, "Singular MVP?"); // Debugging
            return null;
        }


        double[] ndcNear = {ndcX, ndcY, ndcZ_near, 1.0};
        double[] ndcFar = {ndcX, ndcY, ndcZ_far, 1.0};
        double[] worldNearH = MatrixUtil.multiplyMV(invMVP, ndcNear);
        double[] worldFarH = MatrixUtil.multiplyMV(invMVP, ndcFar);

        if (Math.abs(worldNearH[3]) < 1e-7 || Math.abs(worldFarH[3]) < 1e-7) {
            System.err.println("ScreenToWorld: Perspective divide by near-zero W.");
            return null;
        }
        Vector3f worldNear = new Vector3f((float)(worldNearH[0]/worldNearH[3]), (float)(worldNearH[1]/worldNearH[3]), (float)(worldNearH[2]/worldNearH[3]));
        Vector3f worldFar = new Vector3f((float)(worldFarH[0]/worldFarH[3]), (float)(worldFarH[1]/worldFarH[3]), (float)(worldFarH[2]/worldFarH[3]));

        Vector3f rayDir = new Vector3f(worldFar.x - worldNear.x, worldFar.y - worldNear.y, worldFar.z - worldNear.z);

        if (Math.abs(rayDir.y) < 1e-7) {
            // Ray is parallel to the plane
            return null;
        }

        float t = (planeY - worldNear.y) / rayDir.y;

        // Allow slightly negative t for perspective, stricter for ortho
        if (t < -1e-3 && is3D) { return null; }
        if (t < -50.0 && !is3D) { return null; }


        float intersectX = worldNear.x + t * rayDir.x;
        float intersectY = planeY;
        float intersectZ = worldNear.z + t * rayDir.z;

        return new Vector3f(intersectX, intersectY, intersectZ);
    }

    /** Performs picking to find the furniture under the screen coordinates. */
    public Furniture pickFurniture(int screenX, int screenY, DesignModel model,
                                   double[] modelviewMatrix, double[] projectionMatrix, int[] viewport, boolean is3D)
    {
        if (model == null || model.getFurnitureList() == null) {
            return null;
        }

        // Get click position on the floor plane (Y=0)
        Vector3f floorPos = screenToWorldOnPlane(screenX, screenY, 0.0f, modelviewMatrix, projectionMatrix, viewport, is3D);
        if (floorPos == null) {
            // System.err.println("pickFurniture: Could not get floor position.");
            return null;
        }

        Furniture closestMatch = null;
        float minDistanceSq = Float.MAX_VALUE;

        for (Furniture f : model.getFurnitureList()) {
            Vector3f fPos = f.getPosition();
            float halfW = f.getWidth() / 2.0f;
            float halfD = f.getDepth() / 2.0f;

            // Transform click point relative to furniture's origin
            Vector3f clickRelativeToFurn = new Vector3f(floorPos.x - fPos.x, 0, floorPos.z - fPos.z);

            // Rotate the relative click point by the *inverse* of the furniture's Y rotation
            float rotYRad = (float) Math.toRadians(-f.getRotation().y);
            clickRelativeToFurn.rotateY(rotYRad); // Assumes rotateY exists in Vector3f

            // Check if the transformed point is within the AABB (on XZ plane)
            boolean withinBounds = (clickRelativeToFurn.x >= -halfW && clickRelativeToFurn.x <= halfW &&
                    clickRelativeToFurn.z >= -halfD && clickRelativeToFurn.z <= halfD);

            if (withinBounds) {
                // Point is within the footprint, check distance to center
                float dx = fPos.x - floorPos.x;
                float dz = fPos.z - floorPos.z;
                float distSq = dx * dx + dz * dz;

                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestMatch = f;
                }
            }
        }
        return closestMatch;
    }
}