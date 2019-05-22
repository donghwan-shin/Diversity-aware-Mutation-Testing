package TestSuiteSelection;

import IO.Reader;
import Matrix.MatrixInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * New class for the test suite selection experiments
 * using Defects4J results (mutation, bug_detection).
 *
 * This reads matrices from the working directory,
 * and manipulate it (i.e., count kMutants, dMutants, etc.)
 *
 */
public class TS_Experiment {


    public static void main(String[] args) {

        // you can specify a certain program and fault_id
        String p = null;
        String v = null;

        // you can specify a test pool type and the number of threads for running the experiments
        String POOL = "dev"; // select one among {"all", "dev", "randoop", "evosuite-branch"}
        int NO_THREAD = 1;

        /* CHOOSE one or more of the following:
        * score control: change score of a test suite from 0.05 to 1 in steps of 0.05
        * size control: change size of a test suite from 2.5% to 50% in steps of 2.5%
        * un-control: randomly select test suite from the pool
        *  */
        scoreControlExperiments(POOL, NO_THREAD);
//        sizeControlExperiments(POOL, NO_THREAD);
//        unControlExperiments(POOL, NO_THREAD);

    }


    /**
     * ------------------------------------------------------------------------------------
     * Experimental functions
     * ------------------------------------------------------------------------------------
     */


    static void unControlExperiments(String method, int numThreads) {

        String workingDir = "resources";

        ArrayList<UnControlThread> threads = new ArrayList<>();
        ArrayList<String> faultList = MatrixInfo.readMatrixInfo(workingDir+"/matrices/generatedMatrices_"+method+".csv", false);
        for(String fault: faultList) {
            String[] tokens = fault.split(",");
            String p = tokens[0];
            String v = tokens[1];
            String n = tokens[2];
            String pool = tokens[3];
            threads.add(new UnControlThread(workingDir, p, v, n, pool, false));
        }

        System.out.println("unControlExperiments()");
        System.out.println("workingDir:\t"+workingDir);
        System.out.println("Method:\t"+method);
        System.out.println("numThreads:\t"+numThreads);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for(UnControlThread thread: threads) {
            executorService.submit(thread);
        }
        executorService.shutdown();

    }


    static void sizeControlExperiments(String method, int numThreads) {

        String workingDir = "resources";

        ArrayList<SizeControlThread> threads = new ArrayList<>();
        ArrayList<String> faultList = MatrixInfo.readMatrixInfo(workingDir+"/matrices/generatedMatrices_"+method+".csv", false);
        for(String fault: faultList) {
            String[] tokens = fault.split(",");
            String p = tokens[0];
            String v = tokens[1];
            String n = tokens[2];
            String pool = tokens[3];
            threads.add(new SizeControlThread(workingDir, p, v, n, pool, false));
        }

        System.out.println("sizeControlExperiments()");
        System.out.println("workingDir:\t"+workingDir);
        System.out.println("Method:\t"+method);
        System.out.println("numThreads:\t"+numThreads);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for(SizeControlThread thread: threads) {
            executorService.submit(thread);
        }
        executorService.shutdown();

    }


    static void scoreControlExperiments(String method, int numThreads) {

        String workingDir = "resources";

        ArrayList<ScoreControlThread> threads = new ArrayList<>();

        Reader info = new Reader(workingDir+"/matrices/generatedMatrices_"+method+".csv");
        String line;
        while((line = info.readLine()) != null) {
            if(line.contains("p,v,")) continue;
            String[] tokens = line.split(",");
            String p = tokens[0];
            String v = tokens[1];
            String n = tokens[2];
            String testPool = tokens[3];
            int kM = Integer.parseInt(tokens[6]);
            int hM = Integer.parseInt(tokens[9]);
            int trigTests = Integer.parseInt(tokens[10]);
            if(hM==0 || trigTests==0) continue;

            threads.add(new ScoreControlThread(workingDir, p, v, n, testPool, true));
        }
        info.close();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for(ScoreControlThread thread: threads) {
            executorService.submit(thread);
        }
        executorService.shutdown();

    }



}
