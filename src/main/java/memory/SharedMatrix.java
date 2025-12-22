package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        vectors = null;
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        vectors = new SharedVector[matrix.length];
        for (int i=0; i < matrix.length; i++) {
            double[] curr_vector = new double[matrix[i].length];
            for (int j=0 ; j < matrix[i].length; j++){
                curr_vector[j] = matrix[i][j];
            }
            vectors[i] = new SharedVector(curr_vector, VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        if (matrix == null)
            throw new IllegalArgumentException("Matrix is null");
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i=0; i<matrix.length; i++) {
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        vectors = newVectors;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        if (matrix == null)
            throw new IllegalArgumentException("Matrix is null");
        SharedVector[] newVectors = new SharedVector[matrix[0].length];
        for (int i=0; i < matrix[0].length; i++) {
            double[] curr_vector = new double[matrix.length];
            for (int j=0 ; j < matrix.length; j++){
                curr_vector[j] = matrix[j][i];
            }
            newVectors[i] = new SharedVector(curr_vector, VectorOrientation.COLUMN_MAJOR);
        }
        vectors = newVectors;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (vectors!=null){
            acquireAllVectorReadLocks(vectors);
            double[][] matrix;
            if (getOrientation() == VectorOrientation.ROW_MAJOR) {
                matrix = new double[vectors.length][vectors[0].length()];
                for  (int i=0; i<vectors.length; i++) {
                    for (int j=0; j<vectors[i].length(); j++){
                        matrix[i][j] = vectors[i].get(j);
                    }
                }
            } else {
                matrix = new double[vectors[0].length()][vectors.length];
                for  (int i=0; i<vectors.length; i++) {
                    for (int j=0; j<vectors[0].length(); j++){
                        matrix[j][i] = vectors[i].get(j);
                    }
                }
            }
            releaseAllVectorReadLocks(vectors);
            return matrix;
        }
        return null;
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        if (vectors != null) {
            return vectors[index];
        }
        return null;
    }

    public int length() {
        // TODO: return number of stored vectors
        if (vectors != null) {
            return vectors.length;
        }
        return 0;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (vectors != null && vectors.length>0) {
            return vectors[0].getOrientation();
        }
        return null;
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (int i=0; i<vecs.length; i++){
            vecs[i].readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for (int i=0; i<vecs.length; i++){
            vecs[i].readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (int i=0; i<vecs.length; i++){
            vecs[i].writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (int i=0; i<vecs.length; i++){
            vecs[i].writeUnlock();
        }
    }
}
