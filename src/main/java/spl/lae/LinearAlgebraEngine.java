package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {
            ComputationNode currNode = computationRoot.findResolvable();
            if (currNode.getChildren().size() > 2) {
                currNode.associativeNesting();
            } else {
                loadAndCompute(currNode);
            }
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        if (node == null || node.getNodeType() == null)
            throw new IllegalArgumentException("Can't compute null object");
        ComputationNodeType nodeType = node.getNodeType();
        int childrenNum = node.getChildren().size();
        if (nodeType == ComputationNodeType.MATRIX)
            throw new IllegalArgumentException("Can't compute matrix");
        if (nodeType == ComputationNodeType.ADD) {
            System.out.println("---------START ADD--------");
            if (childrenNum != 2) {
                throw new IllegalArgumentException("Can't compute ADD");
            } 
            leftMatrix.loadRowMajor(node.getChildren().getFirst().getMatrix());
            rightMatrix.loadRowMajor(node.getChildren().getLast().getMatrix());
            executor.submitAll(createAddTasks());
            System.out.println("---------END ADD--------");
        }
        else if (nodeType == ComputationNodeType.MULTIPLY) {
            System.out.println("---------START MUL--------");
            if (childrenNum != 2) {
                throw new IllegalArgumentException("Can't compute MULTIPLY");
            } 
            leftMatrix.loadRowMajor(node.getChildren().getFirst().getMatrix());
            rightMatrix.loadRowMajor(node.getChildren().getLast().getMatrix());
            executor.submitAll(createMultiplyTasks());
            System.out.println("---------END MUL--------");
        }
        else if (nodeType == ComputationNodeType.NEGATE) {
            System.out.println("---------START NEG--------");
            if (childrenNum != 1) {
                throw new IllegalArgumentException("Can't compute NEGATE");
            } 
            leftMatrix.loadRowMajor(node.getChildren().getFirst().getMatrix());
            executor.submitAll(createNegateTasks());
            System.out.println("---------END NEG--------");
        }
        else {
            System.out.println("---------START TRAN--------");
            if (childrenNum != 1) {
                throw new IllegalArgumentException("Can't compute TRANSPOSE");
            } 
            leftMatrix.loadRowMajor(node.getChildren().getFirst().getMatrix());
            executor.submitAll(createTransposeTasks());
            System.out.println("---------END TRAN--------");
        }
        node.resolve(leftMatrix.readRowMajor());
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        if (leftMatrix.length() != rightMatrix.length())
            throw new IllegalArgumentException("Invalid Matrices sizes");
        List<Runnable> tasks = new LinkedList<>();
        for(int i=0 ; i< leftMatrix.length(); i++){
            final int index = i;
            tasks.add(() -> {
                leftMatrix.get(index).add(rightMatrix.get(index));
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        if (leftMatrix.get(0).length() != rightMatrix.length())
            throw new IllegalArgumentException("Invalid Matrices sizes");
        List<Runnable> tasks = new LinkedList<>();
        for(int i=0 ; i< leftMatrix.length(); i++){
            final int index = i;
            tasks.add(() -> {
                leftMatrix.get(index).vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new LinkedList<>();
        for(int i=0 ; i< leftMatrix.length(); i++){
            final int index = i;
            tasks.add(() -> {
                leftMatrix.get(index).negate();
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> tasks = new LinkedList<>();
        for(int i=0 ; i< leftMatrix.length(); i++){
            final int index = i;
            tasks.add(() -> {
                leftMatrix.get(index).transpose();
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        if (executor == null)
            throw new IllegalArgumentException("Executor is null");
        String report = executor.getWorkerReport();
        try{
            executor.shutdown();
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        return report;
    }
}
