package devTest;

import IO.IOUtil;
import IO.Reader;
import IO.Writer;
import TerminalCore.Terminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is for measuring the execution time for each developer-written test.
 * It collects all dev-tests to make a large list of tests.
 * Then, for each test, it measures the execution time.
 * The result is saved as a csv file with [test,time] pairs.
 */
public class MeasureTime {

    static String D4J = "/home/donghwan/defects4j/framework/bin/defects4j";
    static String workingDir = "/home/donghwan/defects4j.projects";

    public static void main(String[] args) {
        // Read 'allTestSet' file

        // For each line,
        // (1) checkout the program version
        // (2) execute the test
        // (3) measure the execution time
        // (4) write the result (test,time) as a file


        // Read
        ArrayList<String> testsWithVersions = new ArrayList<>();
        Reader r = new Reader(workingDir+"/allTestSet.csv");
        String line;
        while((line = r.readLine()) != null) {
            if(line.trim().equals("")) continue;
            testsWithVersions.add(line);
        }
        r.close();

        HashMap<String,String> checkedOut = new HashMap<>();
        for(String testWithVersion: testsWithVersions) {
            String[] tokens = testWithVersion.split(",");

            // Checkout
            String tmpDir;
            String key = tokens[1]+"-"+tokens[2];
            if(checkedOut.keySet().contains(key)) {
                tmpDir = checkedOut.get(key);
            } else {
                tmpDir = Terminal.checkout(D4J, tokens[1], tokens[2], null);
                checkedOut.put(key, tmpDir);

                // For compile all tests
                String[] cmd = {D4J, "test", "-r"};
                Terminal.bash(cmd, tmpDir, null);
            }

            // Execute
            String[] cmd = {D4J, "test", "-t", tokens[0]};
            System.out.println("\n"+tokens[0]);
            long startTime, time;
            startTime = System.currentTimeMillis();
            for(int i=0; i<3; i++) {
                Terminal.bash(cmd, tmpDir, null);
            }
            time = System.currentTimeMillis() - startTime;
            System.out.println("Average Time(ms): "+time/5);

            // Write
            String testAndTime = tokens[0]+","+time+"\n";
            try {
                Files.write(Paths.get(workingDir+"/allTestTimes"), testAndTime.getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    /**
     * Run only once.
     */
    static void makeAllTestSet() {
        HashMap<String,String> testSet = new HashMap<>();
        for(int i=1; i<=26; i++)
            getTests("Chart", i, testSet);
        for(int i=1; i<=133; i++)
            getTests("Closure", i, testSet);
        for(int i=1; i<=65; i++)
            getTests("Lang", i, testSet);
        for(int i=1; i<=106; i++)
            getTests("Math", i, testSet);
        for(int i=1; i<=27; i++)
            getTests("Time", i, testSet);

        Writer w = new Writer(workingDir+"/allTestSet");
        for(String test: testSet.keySet())
            w.writeln(test+","+testSet.get(test));
        w.close();
    }

    private static void getTests(String p, int v, HashMap<String,String> testSet) {
        System.out.println(p+"-"+v);

        Reader r = new Reader(workingDir+"/"+p+"/relevant_tests/"+v+".tests");
        String line;
        while((line = r.readLine()) != null) {
            if(line.trim().equals("")) continue;
            testSet.put(line.trim(),p+","+v+"f");
        }
        r.close();
    }
}
