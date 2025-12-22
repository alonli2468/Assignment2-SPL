package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      // TODO: main
      
    String inputPath = "test5.json";
    String outputPath = "output.json";

    LinearAlgebraEngine lae = new LinearAlgebraEngine(8);

    System.out.println("Starting computation for: " + inputPath);

    try {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse(inputPath);

        ComputationNode resultNode = lae.run(root);

        if (resultNode != null && resultNode.getMatrix() != null) {
            OutputWriter.write(resultNode.getMatrix(), outputPath);
            System.out.println("Success! Result written to: " + outputPath);
        }

    } catch (Exception e) {
        System.err.println("Error during execution: " + e.getMessage());
        try {
            OutputWriter.write(e.getMessage(), outputPath);
        } catch (Exception ioE) {
            ioE.printStackTrace();
        }
    } finally {
        System.out.println(lae.getWorkerReport());
    }

    }
}