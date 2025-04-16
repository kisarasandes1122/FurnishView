import java.io.Serializable;

class Vector3f implements Serializable {
    private static final long serialVersionUID = 1L;
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Helper for rotation in picking
    public void rotateY(float angleRad) {
        float cosA = (float) Math.cos(angleRad);
        float sinA = (float) Math.sin(angleRad);
        float newX = x * cosA + z * sinA;
        float newZ = -x * sinA + z * cosA;
        this.x = newX;
        this.z = newZ;
    }

    // ADDED: Clone method
    @Override
    public Vector3f clone() {
        return new Vector3f(this.x, this.y, this.z);
    }

    // Optional: equals method for comparisons
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3f other = (Vector3f) obj;
        final float EPSILON = 1e-6f;
        return Math.abs(other.x - x) < EPSILON &&
                Math.abs(other.y - y) < EPSILON &&
                Math.abs(other.z - z) < EPSILON;
    }

    @Override
    public int hashCode() {
        // Simple hashcode
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);
        return result;
    }

    public void normalize() {
        float magSq = x * x + y * y + z * z;
        if (magSq > 1e-12f) {
            float mag = (float) Math.sqrt(magSq);
            x /= mag;
            y /= mag;
            z /= mag;
        }
    }

    public Vector3f cross(Vector3f v) {
        if (v == null) return new Vector3f(0, 0, 0);
        return new Vector3f(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }

    public Vector3f subtract(Vector3f v) {
        if (v == null) return this.clone();
        return new Vector3f(this.x - v.x, this.y - v.y, this.z - v.z);
    }

}