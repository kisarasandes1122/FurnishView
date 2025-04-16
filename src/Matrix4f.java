public class Matrix4f {
    public float[] m = new float[16];

    public Matrix4f() { identity(); }

    public void identity() {
        m[0]=1; m[4]=0; m[8]=0; m[12]=0;
        m[1]=0; m[5]=1; m[9]=0; m[13]=0;
        m[2]=0; m[6]=0; m[10]=1; m[14]=0;
        m[3]=0; m[7]=0; m[11]=0; m[15]=1;
    }

    public void rotateY(float angle) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        m[0] = cos; m[2] = sin;
        m[8] = -sin; m[10] = cos;
    }

    public void transform(Vector3f vec) {
        float x = m[0]*vec.x + m[4]*vec.y + m[8]*vec.z;
        float y = m[1]*vec.x + m[5]*vec.y + m[9]*vec.z;
        float z = m[2]*vec.x + m[6]*vec.y + m[10]*vec.z;
        vec.x = x;
        vec.y = y;
        vec.z = z;
    }
}