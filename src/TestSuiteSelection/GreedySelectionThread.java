package TestSuiteSelection;


import Core.TestSuite;
import IO.Writer;
import Matrix.InitMatrix;
import Matrix.MatrixObj;

import java.math.BigDecimal;

/**
 * To investigate test suite selection with additional-greedy algorithm
 */
public class GreedySelectionThread implements Runnable {

    String workingDir;
    String p;
    String v;
    String n;
    String testPool;
    boolean printAll;

    public GreedySelectionThread(String workingDir, String p, String v, String n, String testPool, boolean printAll) {
        this.workingDir = workingDir;
        this.p = p;
        this.v = v;
        this.n = n;
        this.testPool = testPool;
        this.printAll = printAll;
    }

    public void run() {

        String file = workingDir+"/testSuiteSelection_greedy/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".greedySuite.csv";
//        if(IOUtil.isFile(file)) return; // TODO: override the existing file

        Writer w = new Writer(file);
        w.write("tScore,p,v,n,testPoolName,aT,mutantPool,%M,aM,eff_kM,eff_dM,eff_sM,criterion,kScore,dScore,sScore,size,isDetect,trigTests\n");

        MatrixObj matrix = InitMatrix.readMatrix(workingDir, p, v, n, testPool);
        String[] mutantPools = new String[]{"all"};

        for (String mutantPool : mutantPools) {

            TestSuite kSuite, dSuite, ksSuite, rSuite;
            int count = 0;
            while (count < 100) {
                kSuite = TestSuiteSelection.greedy(matrix, mutantPool, 1, "k", 0);
                dSuite = TestSuiteSelection.greedy(matrix, mutantPool, 1, "d", 0);
                ksSuite = TestSuiteSelection.greedy(matrix, mutantPool, 1, "ks", dSuite.size());
                rSuite = TestSuiteSelection.greedy(matrix, mutantPool, 1, "r", dSuite.size());

                BigDecimal tScore = new BigDecimal("1");
                TestSuiteSelection.printResult(w, matrix, kSuite, "k", tScore, printAll);
                TestSuiteSelection.printResult(w, matrix, dSuite, "d", tScore, printAll);
                TestSuiteSelection.printResult(w, matrix, ksSuite, "ks", tScore, printAll);
                TestSuiteSelection.printResult(w, matrix, rSuite, "r", tScore, printAll);
                count++;
            }
        }

        w.close();
        System.out.println(p+"-"+v+": done");

    }

}
