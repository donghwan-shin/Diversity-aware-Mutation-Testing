package devTest;

import IO.IOUtil;
import IO.Reader;
import TerminalCore.Terminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Created by donghwan on 2017-09-12.
 *
 * 1. Perform mutation analysis for tests that are missed compared to $v.tests
 * 2. Add mutation analysis results into killMap.csv
 * 3. Add tests into testMap.csv
 *
 */
public class AddDevTest {

    static String D4J = "/home/donghwan/defects4j/framework/bin/defects4j";
    static String workingDir = "/home/donghwan/Dropbox/mutation_v5";

    public static void main(String[] args) {

        for(int i=1; i<=26; i++)
            run("Chart", i);
//        for(int i=1; i<=133; i++)
//            run("Closure", i);
        for(int i=1; i<=65; i++)
            run("Lang", i);
        for(int i=1; i<=106; i++)
            run("Math", i);
        for(int i=1; i<=27; i++)
            run("Time", i);
    }

    static void run(String p, int v) {
        String testMapFile = workingDir+"/defects4j.results/mutation_log/"+p+"/dev/"+v+"f.0.testMap.csv";
        if(!IOUtil.isFile(testMapFile)) return;

        // find the list of tests that $v.tests - ${v}f.0.testMap.csv

        // Read $v.tests
        ArrayList<String> newTests = new ArrayList<>();
        Reader r = new Reader(workingDir+"/defects4j.projects/"+p+"/relevant_tests/"+v+".tests");
        String line;
        while((line = r.readLine()) != null) {
            if(line.trim().equals("")) continue;
            newTests.add(line);
        }
        r.close();

        // Read ${v}f.testMap.csv
        ArrayList<String> oldTests = new ArrayList<>();
        r = new Reader(testMapFile);
        while((line = r.readLine()) != null) {
            if(line.trim().equals("TestNo,TestName") || line.trim().equals("")) continue;
            String[] tokens = line.split(",");
            oldTests.add(tokens[1]);
        }
        r.close();

        // find the list of tests that $v.tests - ${v}f.0.testMap.csv
        newTests.removeAll(oldTests);

        // perform mutation analysis for the tests
        mutationAnalysis(newTests, p, v);


    }

    static void mutationAnalysis(ArrayList<String> tests, String p, int v) {
        System.out.println(p+"-"+v);
        if(tests.isEmpty()) return;

        // get the last testNo
        String line;
        String testMapFile = workingDir+"/defects4j.results/mutation_log/"+p+"/dev/"+v+"f.0.testMap.csv";
        Reader r = new Reader(testMapFile);
        int maxTestNo = 0;
        while((line = r.readLine()) != null) {
            if(line.trim().equals("TestNo,TestName") || line.trim().equals("")) continue;
            String[] tokens = line.split(",");
            int testNo = Integer.parseInt(tokens[0]);
            if(maxTestNo < testNo)
                maxTestNo = testNo;
        }
        r.close();

        // Checkout p-v
        String tmpDir = Terminal.checkout(D4J, p, v+"f", null);

        // for each test,
        for(String test: tests) {
            // perform mutation analysis
            String[] cmd = {D4J, "mutation", "-t", test};
            System.out.println("\n"+test);
            boolean isTimedOut = Terminal.bash(cmd, tmpDir, null);
            if(isTimedOut) continue;

            // increase the total testNo
            maxTestNo++;

            // read /tmpDir/killMap.csv
            String killMap = "\n";
            r = new Reader(tmpDir+"/killMap.csv");
            while((line = r.readLine()) != null) {
                if(line.contains("TestNo,MutantNo") || line.trim().equals("")) continue;
                String[] tokens = line.split(",");
                String mutantNo = tokens[1];
                killMap += maxTestNo+","+mutantNo+"\n";
            }
            r.close();

            // add killMap.csv with testNo,mutantNo
            String killMapFile = workingDir+"/defects4j.results/mutation_log/"+p+"/dev/"+v+"f.0.killMap.csv";
            try {
                Files.write(Paths.get(killMapFile), killMap.getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            // add testMap.csv as the last testNo
            String newLine = "\n"+maxTestNo+","+test;
            try {
                Files.write(Paths.get(testMapFile), newLine.getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        String[] cmd = {"rm", "-rf", tmpDir};
        Terminal.bash(cmd, null, null);
    }

}
