import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import memory.SharedMatrix;
import memory.SharedVector;
import memory.VectorOrientation;

class SharedMatrixTest {

    SharedMatrix matrix;
    double[][] originMat;

    @BeforeEach
    void setUp() {
        originMat = new double[][]{
            {1.0, 2.0},
            {3.0, 4.0}
        };
        matrix = new SharedMatrix();
    }

    @Test
    void testConstructorAndLength() {
        SharedMatrix matrixWithData = new SharedMatrix(originMat);
        assertEquals(2, matrixWithData.length(), "Matrix length doesn't match number of rows");
    }

    @Test
    void testGetOrientation() {
        assertNull(matrix.getOrientation(), "Orientation is Null");
        matrix.loadRowMajor(originMat);
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation(), "Orientation isn't ROW_MAJOR");
        matrix.loadColumnMajor(originMat);
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation(), "Orientation isn't COLUMN_MAJOR");
    }

    @Test
    void testLoadRowMajor() {
        matrix.loadRowMajor(originMat);
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        double[][] result = matrix.readRowMajor();
        assertArrayEquals(originMat[0], result[0], "First row doesn't match input");
        assertArrayEquals(originMat[1], result[1], "Second row doesn't match input");
    }

    @Test
    void testLoadColumnMajor() {
        matrix.loadColumnMajor(originMat);
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
        assertEquals(1.0, matrix.get(0).get(0));
        assertEquals(3.0, matrix.get(0).get(1));
    }

    @Test
    void testReadRowMajorFromColumnMajor() {
        matrix.loadColumnMajor(originMat);
        double[][] result = matrix.readRowMajor();
        assertArrayEquals(originMat[0], result[0], "The new matrix rows doesn't match original input");
        assertArrayEquals(originMat[1], result[1]);
    }

    @Test
    void testGetVectorByIndex() {
        matrix.loadRowMajor(originMat);
        SharedVector firstVector = matrix.get(0);
        assertNotNull(firstVector, "Vector at index 0 is null");
        assertEquals(originMat[0][0], firstVector.get(0), "First element of first vector isn't match sample data");
        assertEquals(originMat[0][1], firstVector.get(1), "Second element of first vector isn't match sample data");
        SharedVector lastVector = matrix.get(matrix.length() - 1);
        assertNotNull(lastVector, "Vector at index 1 is null");
        assertEquals(originMat[1][0], lastVector.get(0), "First element of last vector isn't match sample data");
        assertEquals(originMat[1][1], lastVector.get(1), "Second element of last vector isn't match sample data");
    }

    @Test
    void testLoadNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            matrix.loadRowMajor(null);
        }, "Loading null doesn't throw IllegalArgumentException");
    }
}
