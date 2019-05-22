package devTest;

import IO.IOUtil;
import IO.Reader;
import IO.Writer;

import java.util.ArrayList;

/**
 * Created by donghwan on 2017-09-06.
 *
 * 1. Clearly remove developer-written tests in mutation analysis result files
 * including [killMap.csv, testMap.csv]
 *
 * 2. Remove unnecessary file [kill.csv]
 */
public class RemoveDevTest {

    static String workingDir = "c:/Users/donghwan/Dropbox/mutation_v4/defects4j.results/mutation_log";

    public static void main(String[] args) {

        Reader r = new Reader("c:/Users/donghwan/Dropbox/mutation_v4/defects4j.projects/compareTestNameResults");
        String line, p="", v="";
        ArrayList<String> removeTests = new ArrayList<>();
        while((line = r.readLine()) != null) {
            if(line.startsWith("Chart") ||
                    line.startsWith("Closure") ||
                    line.startsWith("Lang") ||
                    line.startsWith("Math") ||
                    line.startsWith("Time")) {

                // Handle the last p-v related data
                remove(p, v, removeTests);
                removeTests = new ArrayList<>();

                // Parse the new p and v
                String[] tokens = line.split("-");
                p = tokens[0];
                v = tokens[1];
            }

            if(line.startsWith("REMOVE")) {
                String[] tokens = line.split("=");
                String test = tokens[1];
                if(!inKillMap(p, v, test)) {
                    removeTests.add(test);
                }

            }
        }

        // Handle the last p-v related data
        remove(p, v, removeTests);

        r.close();

    }

    public static void remove(String p, String v, ArrayList<String> removeTests) {
        // Check whether removeTests is empty
        if(removeTests.isEmpty())
            return;

        // Check whether mutation analysis results exist for p-v
        if(!IOUtil.isFile(workingDir + "/" + p + "/dev/" + v + "f.0.testMap.csv"))
            return;

        System.out.println("remove(): "+p+"-"+v);

        // Read testMap.csv, except the test
        boolean remove = false;
        Reader r = new Reader(workingDir + "/" + p + "/dev/" + v + "f.0.testMap.csv");
        String line, testMap="";
        while ((line = r.readLine()) != null) {
            String[] tokens = line.split(",");

            // Check whether "tokens[1]" should be removed
            if(!removeTests.contains(tokens[1])) {
                testMap+=line+"\n";
            } else {
                System.out.println("REMOVE="+line);
                remove = true;
            }
        }
        r.close();

        // Update testMap.csv
        if(remove) {
            Writer w = new Writer(workingDir + "/" + p + "/dev/" + v + "f.0.testMap.csv");
            w.write(testMap.trim());
            w.close();
        }

    }

    /**
     *  Check whether there is a test in killMap.csv
     */
    public static boolean inKillMap(String p, String v, String test) {
//        System.out.println("inKillMap(): " + p + "-" + v + "\t" + test);

        // Check whether mutation analysis results exist for p-v
        if(!IOUtil.isFile(workingDir + "/" + p + "/dev/" + v + "f.0.testMap.csv"))
            return false;

        // get [testNo] from testMap.csv
        String testNo="";
        Reader r = new Reader(workingDir + "/" + p + "/dev/" + v + "f.0.testMap.csv");
        String line;
        while ((line = r.readLine()) != null) {
            String[] tokens = line.split(",");
            if (test.equals(tokens[1])) {
                testNo = tokens[0];
                break;
            }
        }
        r.close();
//        System.out.println(testNo);
        if(testNo.equals("")) {
            return false;
        }

        r = new Reader(workingDir + "/" + p + "/dev/" + v + "f.0.killMap.csv");
        while ((line = r.readLine()) != null) {
            if(line.startsWith(testNo+",")) {
                System.err.println(p+"-"+v+": TestNo "+testNo+" kills a mutant!");
                return true;
            }
        }
        r.close();

        return false;
    }

}
