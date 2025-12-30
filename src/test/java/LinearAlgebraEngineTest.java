import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import parser.ComputationNode;
import parser.ComputationNodeType;
import spl.lae.LinearAlgebraEngine;

class LinearAlgebraEngineTest {

    @Test
    void testConstructor_ZeroThreads_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinearAlgebraEngine(0);
        });
    }

    @Test
    void testConstructor_NegativeThreads_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinearAlgebraEngine(-5);
        });
    }

    @Test
    void testRun_NullComputationRoot_ThrowsException() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        assertThrows(IllegalArgumentException.class, () -> {
            engine.run(null);
        });
    }



    @Test
    void testNegateAndTranspose() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        double[][] A = {
            {1, -2, 3},
            {0, 4, -5}
        };
        ComputationNode negNode = new ComputationNode("-", new ArrayList<>(List.of(new ComputationNode(A))));
        engine.run(negNode);
        double[][] expectedA = {
            {-1, 2, -3},
            {0, -4, 5}
        };
        assertArrayEquals(expectedA[0], negNode.getMatrix()[0], 1e-9);
        assertArrayEquals(expectedA[1], negNode.getMatrix()[1], 1e-9);
        ComputationNode transNode = new ComputationNode("T", new ArrayList<>(List.of(new ComputationNode(A))));
        engine = new LinearAlgebraEngine(2);
        engine.run(transNode);
        assertEquals(3, transNode.getMatrix().length); 
        assertEquals(2, transNode.getMatrix()[0].length); 
    }

    @Test
    void testNestedOperationsOfAll() {
        // T( (A + B) * -C )
        LinearAlgebraEngine engine = new LinearAlgebraEngine(4);
        double[][] A = {{1, 1}, {1, 1}};
        double[][] B = {{2, 2}, {3, 3}};
        double[][] C = {{1, 0}, {0, 1}};
        ComputationNode add = new ComputationNode("+", new ArrayList<>(List.of(
                new ComputationNode(A), new ComputationNode(B))));
        ComputationNode neg = new ComputationNode("-", new ArrayList<>(List.of(
                new ComputationNode(C))));
        ComputationNode mul = new ComputationNode("*", new ArrayList<>(List.of(add, neg)));
        ComputationNode root = new ComputationNode("T", new ArrayList<>(List.of(mul)));
        engine.run(root);
        double[][] expectedMat = {{-3, -4}, {-3, -4}};
        assertEquals(ComputationNodeType.MATRIX, root.getNodeType());
        assertMatrixEquals(expectedMat, root.getMatrix());
    }

    @Test
    void testBigMatrixPerformance() {
        int size = 100;
        double[][] A = new double[size][size];
        double[][] B = new double[size][size];
        
        for (int i = 0; i < size; i++) {
            Arrays.fill(A[i], 1.0);
            Arrays.fill(B[i], 2.0);
        }

        LinearAlgebraEngine engine = new LinearAlgebraEngine(8); 
        
        ComputationNode addNode = new ComputationNode("+", new ArrayList<>(List.of(
                new ComputationNode(A),
                new ComputationNode(B)
        )));
        engine.run(addNode);        
        assertEquals(3.0, addNode.getMatrix()[50][50]);
        String report = engine.getWorkerReport();
        assertFalse(report.contains("Fatigue: 0.0"), "All workers need to work");
    }

    @Test
    void testAddAndMultipleChildrenTree() {
        double[][] A = {{1, 1}, {1, 1}};
        double[][] B = {{2, 2}, {2, 2}};
        double[][] C = {{3, 3}, {3, 3}};
        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);
        ComputationNode nodeC = new ComputationNode(C);
        List<ComputationNode> children = new ArrayList<>();
        children.add(nodeA);
        children.add(nodeB);
        children.add(nodeC);
        ComputationNode root = new ComputationNode("+", children);
        LinearAlgebraEngine engine = new LinearAlgebraEngine(4);
        ComputationNode resultNode = engine.run(root);
        double[][] expectedMat = {{6, 6}, {6, 6}};
        assertEquals(ComputationNodeType.MATRIX, resultNode.getNodeType());
        assertMatrixEquals(expectedMat, resultNode.getMatrix());
    }

    @Test
    void testNestedAddAndMulOperations() {
        double[][] A = {{1, 0}, {0, 1}};
        double[][] B = {{1, 0}, {0, 1}};
        double[][] I = {{1, 0}, {0, 1}};
        ComputationNode addNode = new ComputationNode("+", new ArrayList<>(List.of(
                new ComputationNode(A),
                new ComputationNode(B)
        )));
        ComputationNode mulNode = new ComputationNode("*", new ArrayList<>(List.of(
                addNode,
                new ComputationNode(I)
        )));
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        engine.run(mulNode);
        double[][] expectedMat = {{2, 0}, {0, 2}};
        assertEquals(ComputationNodeType.MATRIX, mulNode.getNodeType());
        assertMatrixEquals(expectedMat, mulNode.getMatrix());
    }

    @Test
    void testGetWorkerReportIsEmpty() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        String report = engine.getWorkerReport();
        assertNotNull(report);
        assertFalse(report.isEmpty());
    }

    @Test
    void testGetWorkerReportContainsInfo() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        double[][] A = {{1, 2}, {3, 4}};
        double[][] B = {{1, 1}, {1, 1}};
        ComputationNode left = new ComputationNode(A);
        ComputationNode right = new ComputationNode(B);
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, List.of(left, right));
        engine.run(root);
        String report = engine.getWorkerReport();
        assertTrue(report.contains("Worker"));
        assertTrue(report.contains("Fatigue"));
    }

    @Test
    void testRunShutsDownExecutorAfterCompletion() {
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        double[][] A = {{1, 2}, {3, 4}};
        double[][] B = {{1, 1}, {1, 1}};
        ComputationNode left1 = new ComputationNode(A);
        ComputationNode right1 = new ComputationNode(B);
        ComputationNode root1 = new ComputationNode(ComputationNodeType.ADD, List.of(left1, right1));
        engine.run(root1);
        double[][] expected1 = {{2, 3}, {4, 5}};
        assertMatrixEquals(expected1, root1.getMatrix());
        ComputationNode left2 = new ComputationNode(A);
        ComputationNode right2 = new ComputationNode(B);
        ComputationNode root2 = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(left2, right2));
        assertThrows(RuntimeException.class, () -> {engine.run(root2);});
    }

    //helping method for comparing matricies
    private void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertNotNull(actual, "Matrix is null");
        assertEquals(expected.length, actual.length, "Unmatched sizes");
        if (expected.length > 0) {
            assertEquals(expected[0].length, actual[0].length, "Unmatched sizes");
        }

        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                assertEquals(expected[i][j], actual[i][j], 1e-9,"[" + i + "][" + j + "] index not equal");
            }
        }
    }
}
