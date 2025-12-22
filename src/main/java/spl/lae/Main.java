package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      // TODO: main
      
    String inputPath = "example6.json";
    String outputPath = "output.json";

    // 2. יצירת המנוע (LAE) - ודאו שהקונסטרקטור מקבל מספר עובדים
    // נשתמש ב-4 עובדים לצורך הבדיקה
    LinearAlgebraEngine lae = new LinearAlgebraEngine(4);

    System.out.println("Starting computation for: " + inputPath);

    try {
        // 3. קריאת הקובץ ויצירת עץ החישוב
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse(inputPath);

        // 4. הרצת המנוע על העץ
        // הערה: ודאו שהמתודה run מחזירה את ה-Node הסופי שנפתר
        ComputationNode resultNode = lae.run(root);

        // 5. כתיבת התוצאה לקובץ
        if (resultNode != null && resultNode.getMatrix() != null) {
            OutputWriter.write(resultNode.getMatrix(), outputPath);
            System.out.println("Success! Result written to: " + outputPath);
        }

        

    } catch (Exception e) {
        System.err.println("Error during execution: " + e.getMessage());
        try {
            // כתיבת הודעת השגיאה ל-JSON כפי שנדרש
            OutputWriter.write(e.getMessage(), outputPath);
        } catch (Exception ioE) {
            ioE.printStackTrace();
        }
    } finally {
      // 6. הדפסת הדו"ח של העובדים (בשבילכם, לראות שזה עבד)
        System.out.println("--- Worker Report ---");
        System.out.println(lae.getWorkerReport());
    }

    }
}