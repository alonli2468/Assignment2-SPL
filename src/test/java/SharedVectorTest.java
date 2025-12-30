import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import memory.SharedMatrix;
import memory.SharedVector;
import memory.VectorOrientation;

class SharedVectorTest {

    private SharedVector vec1;
    private SharedVector vec2;

    @BeforeEach
    void setUp() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0, 6.0};
        vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        vec2 = new SharedVector(data2, VectorOrientation.COLUMN_MAJOR);
    }

    @Test
    void testGetAndLengthAndOrientaion() {
        assertEquals(3, vec1.length());
        assertEquals(1.0, vec1.get(0));
        assertEquals(3.0, vec1.get(2));
        assertEquals(VectorOrientation.ROW_MAJOR, vec1.getOrientation());
    }

    @Test
    void testTranspose() {
        assertEquals(VectorOrientation.ROW_MAJOR, vec1.getOrientation());
        vec1.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, vec1.getOrientation());
        vec1.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, vec1.getOrientation());
    }

    @Test
    void testAdd() {
        vec1.add(vec2); 
        assertEquals(5.0, vec1.get(0), "1.0 + 4.0 isn't 5.0");
        assertEquals(7.0, vec1.get(1), "2.0 + 5.0 isn't 7.0");
        assertEquals(9.0, vec1.get(2), "3.0 + 6.0 isn't 9.0");
    }

    @Test
    void testNegate() {
        vec1.negate();
        assertEquals(-1.0, vec1.get(0));
        assertEquals(-2.0, vec1.get(1));
        assertEquals(-3.0, vec1.get(2));
    }

    @Test
    void testDot() {
        double result = vec1.dot(vec2);
        assertEquals(32.0, result, "Dot result isn't 32.0");
    }

    @Test
    void testDotExceptionThrow() {
        vec2.transpose();
        assertThrows(IllegalArgumentException.class, () -> {
            vec1.dot(vec2);
        }, "Dot of same orientation doesn't throw IllegalArgumentException");
    }

    @Test
    void testInvalidIndexThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            vec1.get(5);
        });
    }

    @Test
    void testVecMatMul() {
        double[][] doubleMat = {
            {1.0, 3.0},
            {2.0, 4.0},
            {1.0, 0.0}
        };
        SharedMatrix matrix = new SharedMatrix(doubleMat);
        vec1.vecMatMul(matrix);
        assertEquals(2, vec1.length(), "Vector length isn't 2");
        assertEquals(8.0, vec1.get(0), "1*1 + 2*2 + 3*1 isn't 5");
        assertEquals(11.0, vec1.get(1), "1*3 + 2*4 + 3*0 isn't 11");
        assertEquals(VectorOrientation.ROW_MAJOR, vec1.getOrientation(), "Orientation isn't ROW_MAJOR");
    }

    @Test
    void testVecMatMulInvalidOrientationThrows() {
        SharedMatrix matrix = new SharedMatrix(new double[][]{{1,0},{0,1}});
        assertThrows(IllegalArgumentException.class, () -> {
            vec2.vecMatMul(matrix);
        }, "doesn't throw exception if vector is not ROW_MAJOR");
    }
}