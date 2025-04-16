import java.text.DecimalFormat;

public class MatrixUtil {

    // Multiply two 4x4 matrices (a * b)
    public static double[] multiply(double[] a, double[] b) {
        double[] result = new double[16];
        for (int i = 0; i < 4; i++) { // row
            for (int j = 0; j < 4; j++) { // column
                double sum = 0.0;
                for (int k = 0; k < 4; k++) {
                    sum += a[i + k * 4] * b[k + j * 4]; // OpenGL is column-major: index = row + col*4
                }
                result[i + j * 4] = sum;
            }
        }
        return result;
    }

    // Multiply matrix by vector (m * v) - Treats v as (vx, vy, vz, 1)
    public static double[] multiplyMV(double[] m, double[] v) {
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) { // row
            double sum = 0.0;
            for (int j = 0; j < 4; j++) { // column
                sum += m[i + j * 4] * v[j];
            }
            result[i] = sum;
        }
        return result;
    }


    // Invert a 4x4 matrix using Gaussian elimination
    // Source: Adapted from various sources, e.g., MESA GLU implementation notes
    // WARNING: Not highly robust, may fail for singular matrices.
    public static double[] invert(double[] src) {
        double[] inv = new double[16];
        double[] tmp = new double[16]; // Use double for precision
        int i, j, k;
        double det;

        // Make a copy and use doubles
        for (i = 0; i < 16; ++i) {
            tmp[i] = src[i];
        }

        // Initialize inverse to identity
        for (i = 0; i < 16; ++i) {
            inv[i] = 0.0;
        }
        for (i = 0; i < 4; ++i) {
            inv[i * 4 + i] = 1.0;
        }

        // Gaussian elimination
        for (i = 0; i < 4; ++i) {
            // Look for largest element in column
            int max_row = i;
            for (j = i + 1; j < 4; ++j) {
                if (Math.abs(tmp[j * 4 + i]) > Math.abs(tmp[max_row * 4 + i])) {
                    max_row = j;
                }
            }

            // Swap rows if needed
            if (max_row != i) {
                for (k = 0; k < 4; ++k) {
                    double swap = tmp[i * 4 + k];
                    tmp[i * 4 + k] = tmp[max_row * 4 + k];
                    tmp[max_row * 4 + k] = swap;

                    swap = inv[i * 4 + k];
                    inv[i * 4 + k] = inv[max_row * 4 + k];
                    inv[max_row * 4 + k] = swap;
                }
            }

            // Check for singularity
            if (Math.abs(tmp[i * 4 + i]) < 1e-10) {
                System.err.println("MatrixUtil.invert: Matrix is singular or near-singular.");
                return null; // Cannot invert
            }

            // Scale row
            double pivot = tmp[i * 4 + i];
            for (k = 0; k < 4; ++k) {
                tmp[i * 4 + k] /= pivot;
                inv[i * 4 + k] /= pivot;
            }

            // Eliminate other rows
            for (j = 0; j < 4; ++j) {
                if (j != i) {
                    double factor = tmp[j * 4 + i];
                    for (k = 0; k < 4; ++k) {
                        tmp[j * 4 + k] -= factor * tmp[i * 4 + k];
                        inv[j * 4 + k] -= factor * inv[i * 4 + k];
                    }
                }
            }
        }
        return inv;
    }

    // Helper to print matrix (column-major)
    public static void printMatrix(double[] matrix, String name) {
        System.out.println("Matrix: " + name);
        if (matrix == null || matrix.length != 16) {
            System.out.println("  [Invalid Matrix]");
            return;
        }
        DecimalFormat df = new DecimalFormat("+#,##0.000;-#");
        System.out.format("  [%s %s %s %s]\n", df.format(matrix[0]), df.format(matrix[4]), df.format(matrix[8]), df.format(matrix[12]));
        System.out.format("  [%s %s %s %s]\n", df.format(matrix[1]), df.format(matrix[5]), df.format(matrix[9]), df.format(matrix[13]));
        System.out.format("  [%s %s %s %s]\n", df.format(matrix[2]), df.format(matrix[6]), df.format(matrix[10]), df.format(matrix[14]));
        System.out.format("  [%s %s %s %s]\n", df.format(matrix[3]), df.format(matrix[7]), df.format(matrix[11]), df.format(matrix[15]));
    }
}