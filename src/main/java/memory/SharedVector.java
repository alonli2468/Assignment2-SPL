package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try{
            if (vector == null)
                throw new NullPointerException();
            if (vector.length<=index) {
                throw new IndexOutOfBoundsException();
            }
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        // TODO: return vector length
        readLock();
        try {
            if (vector!=null) {
                return vector.length;
            }
            return 0;
        } finally {
            readUnlock();
        }
        
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        readLock();
        try {
            if (vector!=null) {
                return orientation;
            }
            return null;
        } finally {
            readUnlock();
        }
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector
        writeLock();
        try {
            if (orientation == VectorOrientation.ROW_MAJOR)
                orientation = VectorOrientation.COLUMN_MAJOR;
            else
                orientation = VectorOrientation.ROW_MAJOR;
        } finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        if (System.identityHashCode(this)<System.identityHashCode(other)){
            writeLock();
            other.readLock();
            try {
                if (vector == null)
                    throw new NullPointerException();
                if (other.length()!= vector.length)
                    throw new IllegalArgumentException("invalid vector size");
                for (int i=0; i< vector.length; i++) {
                    vector[i] = vector[i]+other.get(i);
                }  
            } finally {
                other.readUnlock();
                writeUnlock();
            }
        } else{
            other.readLock();
            writeLock();
            try {
                if (vector == null)
                    throw new NullPointerException();
                if (other.length()!= vector.length)
                    throw new IllegalArgumentException("invalid vector size");
                for (int i=0; i< vector.length; i++) {
                    vector[i] = vector[i]+other.get(i);
                }  
            } finally {
                writeUnlock();
                other.readUnlock();
            }
        }
        
    }

    public void negate() {
        // TODO: negate vector
        writeLock();
        try {
            if (vector == null)
                throw new NullPointerException();
            for (int i=0; i< vector.length; i++) {
                vector[i] = vector[i]*(-1);
            }
        } finally {
            writeUnlock();
        }
        
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        if (System.identityHashCode(this)<System.identityHashCode(other)){
            readLock();
            other.readLock();
            try{
                if (orientation == other.orientation)
                    throw new IllegalArgumentException("Both vectors with same orientation");
                if (other.length()!= vector.length)
                    throw new IllegalArgumentException("invalid vectors size");
                double sum = 0;
                for(int i=0; i<vector.length; i++){
                    sum = sum + (vector[i]*other.get(i));
                }
                return sum;
            } finally {
                other.readUnlock();
                readUnlock();
            }
        } else {
            other.readLock();
            readLock();
            try{
                if (orientation == other.orientation)
                    throw new IllegalArgumentException("Both vectors with same orientation");
                if (other.length()!= vector.length)
                    throw new IllegalArgumentException("invalid vectors size");
                double sum = 0;
                for(int i=0; i<vector.length; i++){
                    sum = sum + (vector[i]*other.get(i));
                }
                return sum;
            } finally {
                readUnlock();
                other.readUnlock();
            }
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        //לבצע איכשהו נעילה על המטריצה כדי שלא תשתנה במהלך הפעולה
        writeLock();
        try {
            if (orientation != VectorOrientation.ROW_MAJOR)
                throw new IllegalArgumentException("Invalid operation, vector isn't row major");
            double[] temp_vector;
            if (matrix.getOrientation() == VectorOrientation.ROW_MAJOR) {
                if (this.length() != matrix.length()) {
                    throw new IllegalArgumentException("invalid sizes");
                }
                temp_vector = new double[matrix.get(0).length()];
                for(int i=0; i<matrix.get(0).length(); i++){
                    double[] curr_vector = new double[matrix.length()];
                    for(int j=0; j<matrix.length(); j++){
                        curr_vector[j] = matrix.get(j).get(i);
                    }
                    temp_vector[i] = dot(new SharedVector(curr_vector, VectorOrientation.COLUMN_MAJOR));
                }
            }else {
                if (this.length() != matrix.get(0).length()) {
                    throw new IllegalArgumentException("invalid sizes");
                }
                temp_vector = new double[matrix.length()];
                for(int i=0; i<matrix.length(); i++){
                    temp_vector[i]= dot(matrix.get(i));
                }
            }
            vector = temp_vector;
        } finally {
            writeUnlock();
        }
    }
}
