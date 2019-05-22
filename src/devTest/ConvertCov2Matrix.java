package devTest;

import IO.IOUtil;
import IO.Reader;


/**
 * NOT YET IMPLEMENTED!
 * convert coverage map into matrix form.
 */
public class ConvertCov2Matrix {

    static String workindDir = "";

    public static void main(String[] args) {
        for(int i=1; i<=1; i++)
            convertCov2Matrix("Chart", i);
//        for(int i=1; i<=40; i++)
//            convertCov2Matrix("Closure", i);
//        for(int i=1; i<=65; i++)
//            convertCov2Matrix("Lang", i);
//        for(int i=1; i<=106; i++)
//            convertCov2Matrix("Math", i);
//        for(int i=1; i<=27; i++)
//            convertCov2Matrix("Time", i);
    }

    static void convertCov2Matrix(String p, int v) {
        String covMapFile = workindDir+"/defects4j.results/coverage_log/"+p+"/dev/"+v+"f.covMap.csv";

        // Check whether there is a compete file - if true, skip
        if(!IOUtil.isFile(covMapFile)) {
            System.err.println("No file: "+ covMapFile);
            return;
        }

        Reader r = new Reader(covMapFile);
        String line;
        while((line = r.readLine()) != null) {
            String[] tokens = line.split(",");
            String testName = tokens[0];
            String lineNo = tokens[1];

            // TODO: save test names.
            // TODO: get the full list of tests from ...
        }
        r.close();
    }
}
