package Matrix;

import IO.IOUtil;
import IO.Reader;
import IO.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Generate a matrix for each fault. (mutation results + fault detection results)
 *
 */

public class InitMatrix {

    public static void main(String[] args) {

        String workingDir = "resources";

        ArrayList<GenerateMatrixThread> threads = new ArrayList<>();

        for(int i=1; i<=26; i++)
            threads.add(new GenerateMatrixThread("Chart", i+"", new String[]{"0","1","2","3","4","5"}, workingDir));

        for(int i=1; i<=133; i++)
            threads.add(new GenerateMatrixThread("Closure", i+"", new String[]{"0","1","2","3","4","5"}, workingDir));

        for(int i=1; i<=65; i++)
            threads.add(new GenerateMatrixThread("Lang", i+"", new String[]{"0","1","2","3","4","5"}, workingDir));

        for(int i=1; i<=106; i++)
            threads.add(new GenerateMatrixThread("Math", i+"", new String[]{"0","1","2","3","4","5"}, workingDir));

        for(int i=1; i<=27; i++)
            threads.add(new GenerateMatrixThread("Time", i+"", new String[]{"0","1","2","3","4","5"}, workingDir));


//        threads.add(new GenerateMatrixThread("Closure", "10", new String[]{"0","1"}, workingDir));

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for(GenerateMatrixThread thread: threads) {
            executorService.submit(thread);
        }
        executorService.shutdown();

    }


    /**
     * Once a matrix object is generated, it can be used by simply read!
     * @param workingDir
     * @param p
     * @param v
     * @param n
     * @param testPool
     * @return
     */
    public static MatrixObj readMatrix(String workingDir, String p, String v, String n, String testPool) {

        if(IOUtil.isFile(workingDir+"/matrices/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".matrix"))
            return (MatrixObj) IOUtil.loadObject(workingDir+"/matrices/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".matrix");
        else {
            System.err.println("File not found: "+workingDir+"/matrices/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".matrix");
            return null;
        }
    }

}

class GenerateMatrixThread implements Runnable {

    String p, v, workingDir;
    String[] nList;

    public GenerateMatrixThread(String p, String v, String[] nList, String workingDir) {
        this.p = p;
        this.v = v;
        this.nList = nList;
        this.workingDir = workingDir;
    }

    public void run() {

        /**
         * Choose one or more of the following in the constructor below:
         * "dev": developer-written tests
         * "randoop": automatically generated tests by Randoop
         * "evosuite-branch": automatically generated tests by EvoSuite, maximizing branch coverage
         * "evosuite-strongmutation": the same as above, maximizing strong mutation score
         * "evosuite-weakmutation": the same as above, maximizing weak mutation score
         *
         * e.g., The following commands build matrices using dev, randoop, and evosuite-branch, together.
         * String[] testPools = new String[]{"dev", "randoop", "evosuite-branch"};
         *
         */
        String[] testPools = new String[]{"dev", "randoop", "evosuite-branch"};

        ArrayList<String> mutantSet = new ArrayList<>();
        ArrayList<String> testNames = new ArrayList<>();
        ArrayList<String> faultDetectionNames = new ArrayList<>();
        HashMap<String, ArrayList<Integer>> fullKillMap = new HashMap<>();


//        int completeCounter = 0;

        for(String testPool: testPools) {

//            System.out.print(p + "\t" + v + "\t" + testPoolName + "\t");

            boolean passed = false;
            for(String n: nList) {

                if(passed) break; // break and go out if we already have the information.
                if(testPool.equals("dev") && !n.equals("0")) continue;
                if(!testPool.equals("dev") && n.equals("0")) continue;

                /**
                 * ------------------------------------------------------------------------------------
                 * Read mutation logs
                 * ------------------------------------------------------------------------------------
                 */

                String mutation_dir = workingDir + "/defects4j.results/mutation_log/" + p + "/" + testPool;
                String line;

                if (!IOUtil.isFile(mutation_dir + "/" + v + "f." + n + ".killMap.csv")) {
                    System.out.println(p + "-" + v + "f-" + testPool + "-" + n+"\tNo mutation results. Try next.");
                    continue; // skip this iteration for the n-testPool
                } else {
                    passed = true;
                }


                // read mutants' information, only once
                if (mutantSet.size() == 0) {
                    Reader mutants = new Reader(mutation_dir + "/" + v + "f.mutants.log");
                    while ((line = mutants.readLine()) != null) {
                        String[] tokens = line.split(":");
                        mutantSet.add(tokens[1]);

                        // following assertion confirm the index of each mutant - mutation operator pair.
                        assert (mutantSet.size() == Integer.parseInt(tokens[0]));
                    }
                    mutants.close();
                }


                // read test names and ids
                Reader testMap = new Reader(mutation_dir + "/" + v + "f." + n + ".testMap.csv");
                HashMap<Integer, String> testNameMap = new HashMap<>();
                while ((line = testMap.readLine()) != null) {
                    if (line.contains("TestNo") || line.equals("")) continue;

                    String[] tokens = line.split(",");
                    String testName = testPool+"__"+n+"__"+tokens[1];
                    testNameMap.put(Integer.parseInt(tokens[0]), testName);
                    testNames.add(testName);
                }
                testMap.close();


                // read killMap
                Reader killMap = new Reader(mutation_dir + "/" + v + "f." + n + ".killMap.csv");
                while ((line = killMap.readLine()) != null) {
                    if (line.contains("TestNo") || line.equals("")) continue;

                    String[] tokens = line.split(",");
                    String testName = testNameMap.get(Integer.parseInt(tokens[0]));
                    if (!fullKillMap.containsKey(testName))
                        fullKillMap.put(testName, new ArrayList<Integer>());
                    fullKillMap.get(testName).add(Integer.parseInt(tokens[1]));
                }
                killMap.close();


                /**
                 * ------------------------------------------------------------------------------------
                 * Read fault detection logs
                 * ------------------------------------------------------------------------------------
                 */

                Reader trigTests = null;
                String split = null;
                String fileName;

                if (testPool.equals("dev")) {
                    fileName = workingDir + "/defects4j.projects/" + p + "/trigger_tests/" + v;
                    if (!IOUtil.isFile(fileName)) {
                        System.out.println(p + "-" + v + "f-" + testPool + ": No bug detection results");
                        continue; // skip this testPoolName
                    }
                    trigTests = new Reader(fileName);
                    split = "---";
                } else if (testPool.equals("randoop") || testPool.startsWith("evosuite-")) {
                    fileName = workingDir + "/defects4j.results/bug_detection_log/" + p + "/" + testPool + "/" + v + "b." + n + ".trigger.log";
                    if (!IOUtil.isFile(fileName)) {
                        System.out.println(p + "-" + v + "f-" + testPool + ": No bug detection results");
                        continue; // skip this testPoolName
                    }
                    trigTests = new Reader(fileName);
                    split = "---|:";
                }

                while ((line = trigTests.readLine()) != null) {
                    if (line.contains("---")) {
                        String[] token = line.split(split, -1);
                        String testName = testPool+"__"+n+"__"+token[1].trim();
                        faultDetectionNames.add(testName);
                    }
                }
                trigTests.close();
            }
        }

        if(mutantSet.size() == 0) {
            // not enough information for generating a matrix
            System.err.println("SKIP: "+p+"-"+v);
            return;
        }



        /**
         * ------------------------------------------------------------------------------------------
         * Build a matrix using faultDetectionNames, testNames, mutationOperators, fullKillMap.
         * ------------------------------------------------------------------------------------------
         */

        Writer matrix = new Writer(workingDir+"/matrices/"+p+"/"+p+"."+v+"f.big.all.Matrix.csv");

        // first line
        matrix.write("testType,test,bug");
        for(int i=1; i<=mutantSet.size(); i++) {
            matrix.write(",");
            matrix.write(i+"");
        }
        matrix.write("\n");

        // second line
        matrix.write("-,-,-");
        for(int i=1; i<=mutantSet.size(); i++) {
            matrix.write(",");
            matrix.write(mutantSet.get(i-1));
        }
        matrix.write("\n");

        // after third line
        for(String testName: testNames) {

            // testType
            matrix.write(testName.substring(0, testName.indexOf("__")));
            matrix.write(",");

            // testName
            matrix.write(testName);
            matrix.write(",");

            // bug detection
            if(faultDetectionNames.contains(testName))
                matrix.write("1");
            else
                matrix.write("0");

            // mutant kill
            if(fullKillMap.containsKey(testName)) {
                // the test kills at least one mutant.
                for(int i=1; i<=mutantSet.size(); i++) {
                    if(fullKillMap.get(testName).contains(i))
                        matrix.write(",1");
                    else
                        matrix.write(",0");
                }

            } else {
                // the test does not kill any mutants.
                for(int i=1; i<=mutantSet.size(); i++) {
                    matrix.write(",0");
                }
            }
            matrix.write("\n");
        }

        matrix.close();


        /**
         * ------------------------------------------------------------------------------------------
         * save the matrix as a class object.
         * ------------------------------------------------------------------------------------------
         */

        for(String testPool: testPools) {
            // generate matrices for each test pool
            IOUtil.saveObject(new MatrixObj(p, v, "big", testPool, mutantSet.size(), testNames, faultDetectionNames, fullKillMap),
                    workingDir + "/matrices/" + p + "/" + p + "." + v + "f.big." + testPool + ".matrix");
        }

        /**
         * Currently, it does not build matrices for all test pools.
         */
        IOUtil.saveObject(new MatrixObj(p, v, "big", "all", mutantSet.size(), testNames, faultDetectionNames, fullKillMap),
                workingDir + "/matrices/" + p + "/" + p + "." + v + "f.big." + "all" + ".matrix");

        System.out.println("DONE: "+p+"-"+v);

    }
}